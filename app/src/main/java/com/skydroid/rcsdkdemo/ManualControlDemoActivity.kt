package com.skydroid.rcsdkdemo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.skydroid.rcsdk.KeyManager
import com.skydroid.rcsdk.RCSDKManager
import com.skydroid.rcsdk.common.DeviceType
import com.skydroid.rcsdk.common.callback.CompletionCallbackWith
import com.skydroid.rcsdk.common.error.SkyException
import com.skydroid.rcsdk.common.remotecontroller.ChannelSettings
import com.skydroid.rcsdk.common.remotecontroller.ControlMode
import com.skydroid.rcsdk.common.remotecontroller.H12ChannelSettings
import com.skydroid.rcsdk.key.RemoteControllerKey
import java.io.InputStream
import java.io.OutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ManualControlDemoActivity : AppCompatActivity() {

    private val uiHandler = Handler(Looper.getMainLooper())
    private val sendIntervalMs = 100L
    private val heartbeatIntervalMs = 1000L
    private val encoder = MavlinkV1Encoder(systemId = 255, componentId = 190)
    private val defaultCalibration = AxisCalibration()
    private val transportExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var readRCButtonHelper: ReadRCButtonHelper? = null
    private var isStreaming = false
    private var pitchChannel = 2
    private var rollChannel = 1
    private var throttleChannel = 3
    private var yawChannel = 4
    private var targetSystemId = 1
    private var positiveThrottleMode = true
    private var transportMode = TransportMode.TCP
    private var controlMode: ControlMode = ControlMode.UNKNOWN
    private var calibrationByChannel: Map<Int, AxisCalibration> = emptyMap()
    private var latestSample: ManualControlSample? = null
    private var sentPackets = 0
    private var receivedPackets = 0
    private var lastHeartbeatElapsedMs = 0L
    private var transport: NetworkTransport? = null

    private lateinit var rgTransport: RadioGroup
    private lateinit var rbTcp: RadioButton
    private lateinit var rbUdp: RadioButton
    private lateinit var etLocalPort: EditText
    private lateinit var etRemoteHost: EditText
    private lateinit var etRemotePort: EditText
    private lateinit var etTargetSystem: EditText
    private lateinit var etPitchChannel: EditText
    private lateinit var etRollChannel: EditText
    private lateinit var etThrottleChannel: EditText
    private lateinit var etYawChannel: EditText
    private lateinit var cbPositiveThrottle: CheckBox
    private lateinit var btnToggleStreaming: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvPipeline: TextView
    private lateinit var tvPackets: TextView
    private lateinit var tvAxes: TextView
    private lateinit var tvRawChannels: TextView
    private lateinit var tvCalibration: TextView
    private lateinit var yawThrottleView: StickIndicatorView
    private lateinit var rollPitchView: StickIndicatorView

    private val sendLoop = object : Runnable {
        override fun run() {
            if (!isStreaming) {
                return
            }
            sendLatestManualControl()
            uiHandler.postDelayed(this, sendIntervalMs)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_control_demo)
        title = "MAVLink Manual Control Demo"

        bindViews()
        bindChannelInputs()
        btnToggleStreaming.setOnClickListener {
            toggleStreaming()
        }
        findViewById<View>(R.id.btn_refresh_metadata).setOnClickListener {
            refreshMetadata()
        }

        calibrationByChannel = buildDefaultCalibrationMap(16)
        refreshMetadata()
        startChannelReader()
        renderStatus("Waiting for RC channel samples.")
        renderPacketStats()
    }

    private fun bindViews() {
        rgTransport = findViewById(R.id.rg_transport)
        rbTcp = findViewById(R.id.rb_transport_tcp)
        rbUdp = findViewById(R.id.rb_transport_udp)
        etLocalPort = findViewById(R.id.et_local_port)
        etRemoteHost = findViewById(R.id.et_remote_host)
        etRemotePort = findViewById(R.id.et_remote_port)
        etTargetSystem = findViewById(R.id.et_target_system)
        etPitchChannel = findViewById(R.id.et_pitch_channel)
        etRollChannel = findViewById(R.id.et_roll_channel)
        etThrottleChannel = findViewById(R.id.et_throttle_channel)
        etYawChannel = findViewById(R.id.et_yaw_channel)
        cbPositiveThrottle = findViewById(R.id.cb_positive_throttle)
        btnToggleStreaming = findViewById(R.id.btn_toggle_streaming)
        tvStatus = findViewById(R.id.tv_manual_status)
        tvPipeline = findViewById(R.id.tv_pipeline_status)
        tvPackets = findViewById(R.id.tv_packet_stats)
        tvAxes = findViewById(R.id.tv_axes)
        tvRawChannels = findViewById(R.id.tv_raw_channels)
        tvCalibration = findViewById(R.id.tv_calibration)
        yawThrottleView = findViewById(R.id.view_yaw_throttle)
        rollPitchView = findViewById(R.id.view_roll_pitch)
    }

    private fun bindChannelInputs() {
        rgTransport.setOnCheckedChangeListener { _, checkedId ->
            transportMode = if (checkedId == R.id.rb_transport_udp) TransportMode.UDP else TransportMode.TCP
            applyTransportDefaults()
        }
        etPitchChannel.attachChannelWatcher { pitchChannel = it }
        etRollChannel.attachChannelWatcher { rollChannel = it }
        etThrottleChannel.attachChannelWatcher { throttleChannel = it }
        etYawChannel.attachChannelWatcher { yawChannel = it }
        etTargetSystem.attachChannelWatcher { targetSystemId = it.coerceIn(1, 255) }
        positiveThrottleMode = cbPositiveThrottle.isChecked
        cbPositiveThrottle.setOnCheckedChangeListener { _, checked ->
            positiveThrottleMode = checked
            latestSample?.let { renderSample(it) }
        }
        rbTcp.isChecked = true
        applyTransportDefaults()
    }

    private fun startChannelReader() {
        val helper = ReadRCButtonHelper(RCSDKManager.getDeviceType())
        helper.setListener { channels ->
            val sample = buildManualControlSample(channels)
            latestSample = sample
            runOnUiThread {
                renderSample(sample)
            }
        }
        helper.start()
        readRCButtonHelper = helper
    }

    private fun refreshMetadata() {
        KeyManager.get(RemoteControllerKey.KeyControlMode, object : CompletionCallbackWith<ControlMode> {
            override fun onSuccess(value: ControlMode) {
                controlMode = value
                updateCalibrationText()
            }

            override fun onFailure(e: SkyException) {
                controlMode = ControlMode.UNKNOWN
                renderStatus("Failed to read control mode: $e")
                updateCalibrationText()
            }
        })

        when (RCSDKManager.getDeviceType()) {
            DeviceType.H12 -> {
                KeyManager.get(RemoteControllerKey.KeyH12ChannelSettings, object : CompletionCallbackWith<H12ChannelSettings> {
                    override fun onSuccess(value: H12ChannelSettings) {
                        calibrationByChannel = value.channels.associate { item ->
                            item.index to AxisCalibration(
                                min = item.min,
                                middle = item.middle,
                                max = item.max,
                                reverse = item.reverse,
                            )
                        }
                        updateCalibrationText()
                    }

                    override fun onFailure(e: SkyException) {
                        calibrationByChannel = buildDefaultCalibrationMap(12)
                        renderStatus("Using default channel calibration: $e")
                        updateCalibrationText()
                    }
                })
            }
            else -> {
                KeyManager.get(RemoteControllerKey.KeyChannelSettings, object : CompletionCallbackWith<ChannelSettings> {
                    override fun onSuccess(value: ChannelSettings) {
                        calibrationByChannel = value.channels.associate { item ->
                            item.index to AxisCalibration(
                                min = item.min,
                                middle = item.middle,
                                max = item.max,
                                reverse = item.reverse,
                            )
                        }
                        updateCalibrationText()
                    }

                    override fun onFailure(e: SkyException) {
                        calibrationByChannel = buildDefaultCalibrationMap(16)
                        renderStatus("Using default channel calibration: $e")
                        updateCalibrationText()
                    }
                })
            }
        }
    }

    private fun buildManualControlSample(channels: IntArray): ManualControlSample {
        val pitchRaw = getChannelValue(channels, pitchChannel)
        val rollRaw = getChannelValue(channels, rollChannel)
        val throttleRaw = getChannelValue(channels, throttleChannel)
        val yawRaw = getChannelValue(channels, yawChannel)

        val pitch = normalizeCenteredAxis(pitchRaw, calibrationFor(pitchChannel))
        val roll = normalizeCenteredAxis(rollRaw, calibrationFor(rollChannel))
        val throttle = if (positiveThrottleMode) {
            normalizePositiveThrottle(throttleRaw, calibrationFor(throttleChannel))
        } else {
            normalizeCenteredAxis(throttleRaw, calibrationFor(throttleChannel))
        }
        val yaw = normalizeCenteredAxis(yawRaw, calibrationFor(yawChannel))

        return ManualControlSample(
            targetSystem = targetSystemId,
            pitchRaw = pitchRaw,
            rollRaw = rollRaw,
            throttleRaw = throttleRaw,
            yawRaw = yawRaw,
            x = pitch,
            y = roll,
            z = throttle,
            r = yaw,
            channels = channels.copyOf(),
        )
    }

    private fun renderSample(sample: ManualControlSample) {
        val throttleDisplayValue = if (positiveThrottleMode) (sample.z * 2) - 1000 else sample.z
        yawThrottleView.setAxes(sample.r, throttleDisplayValue)
        rollPitchView.setAxes(sample.y, sample.x)
        tvAxes.text = buildString {
            append("x / pitch: ")
            append(sample.x)
            append("   (ch")
            append(pitchChannel)
            append(" raw ")
            append(sample.pitchRaw)
            append(")\n")
            append("y / roll: ")
            append(sample.y)
            append("   (ch")
            append(rollChannel)
            append(" raw ")
            append(sample.rollRaw)
            append(")\n")
            append("z / thrust: ")
            append(sample.z)
            append(if (positiveThrottleMode) " [0..1000]" else " [-1000..1000]")
            append("   (ch")
            append(throttleChannel)
            append(" raw ")
            append(sample.throttleRaw)
            append(")\n")
            append("r / yaw: ")
            append(sample.r)
            append("   (ch")
            append(yawChannel)
            append(" raw ")
            append(sample.yawRaw)
            append(")")
        }
        tvRawChannels.text = "Channels: ${sample.channels.contentToString()}"
    }

    private fun toggleStreaming() {
        if (isStreaming) {
            stopStreaming()
            return
        }

        val config = currentTransportConfig() ?: return
        openTransport(config)
        isStreaming = true
        btnToggleStreaming.text = "Stop Sending"
        lastHeartbeatElapsedMs = 0L
        renderStatus("Streaming MANUAL_CONTROL every ${sendIntervalMs} ms over ${config.mode.name}.")
        uiHandler.post(sendLoop)
    }

    private fun stopStreaming() {
        isStreaming = false
        btnToggleStreaming.text = "Start Sending"
        uiHandler.removeCallbacks(sendLoop)
        closeTransport()
        renderPipeline("Link idle")
        renderStatus("Streaming stopped.")
    }

    private fun currentTransportConfig(): TransportConfig? {
        val localPort = parseInt(etLocalPort.text?.toString(), 14550)
        val remoteHost = etRemoteHost.text?.toString()?.trim().orEmpty()
        val remotePort = parseInt(etRemotePort.text?.toString(), transportMode.defaultPort)
        if (remoteHost.isBlank()) {
            Toast.makeText(this, "Enter a host/IP first.", Toast.LENGTH_SHORT).show()
            return null
        }
        return TransportConfig(
            mode = transportMode,
            host = remoteHost,
            remotePort = remotePort,
            localPort = localPort,
        )
    }

    private fun openTransport(config: TransportConfig) {
        closeTransport()
        val transport = when (config.mode) {
            TransportMode.TCP -> TcpTransport(config)
            TransportMode.UDP -> UdpTransport(config)
        }
        this.transport = transport
        transportExecutor.execute {
            try {
                transport.open()
                postPipelineStatus(transport.connectedLabel())
            } catch (e: Exception) {
                postPipelineStatus("Open failed: ${e.message ?: e.javaClass.simpleName}")
            }
        }
    }

    private fun sendLatestManualControl() {
        val sample = latestSample ?: return
        val transport = transport ?: return
        val heartbeatDue = SystemClock.elapsedRealtime() - lastHeartbeatElapsedMs >= heartbeatIntervalMs
        val manualFrame = encoder.encodeManualControl(
            ManualControlAxes(
                targetSystem = sample.targetSystem,
                x = sample.x,
                y = sample.y,
                z = sample.z,
                r = sample.r,
            )
        )
        transportExecutor.execute {
            try {
                if (heartbeatDue) {
                    transport.send(encoder.encodeHeartbeatGcs())
                    lastHeartbeatElapsedMs = SystemClock.elapsedRealtime()
                }
                transport.send(manualFrame)
                sentPackets += 1
                runOnUiThread {
                    renderPacketStats()
                    renderPipeline(transport.connectedLabel())
                }
            } catch (e: Exception) {
                runOnUiThread {
                    renderPipeline("Send failed: ${e.message ?: e.javaClass.simpleName}")
                }
            }
        }
    }

    private fun renderStatus(status: String) {
        tvStatus.text = status
    }

    private fun renderPipeline(status: String) {
        tvPipeline.text = "Link: $status"
    }

    private fun renderPacketStats() {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        tvPackets.text = "TX packets: $sentPackets   RX packets: $receivedPackets   Updated: $timestamp"
    }

    private fun updateCalibrationText() {
        tvCalibration.text = buildString {
            append("Device: ")
            append(RCSDKManager.getDeviceType().name)
            append("   Control mode: ")
            append(controlMode.name)
            append("\nDefault MANUAL_CONTROL mapping uses AETR channels:")
            append(" roll=1, pitch=2, throttle=3, yaw=4.")
            append("\nPer-channel min/mid/max/reverse is pulled from RC settings when available.")
        }
    }

    private fun applyTransportDefaults() {
        val currentHost = etRemoteHost.text?.toString()?.trim().orEmpty()
        if (currentHost.isBlank() || currentHost == "192.168.144.10" || currentHost == "192.168.68.77") {
            etRemoteHost.setText("Indoor.local")
        }
        val desiredPort = transportMode.defaultPort.toString()
        if (etRemotePort.text?.toString() != desiredPort) {
            etRemotePort.setText(desiredPort)
        }
        etLocalPort.isEnabled = transportMode == TransportMode.UDP
    }

    private fun closeTransport() {
        val current = transport
        transport = null
        if (current != null) {
            transportExecutor.execute {
                current.close()
            }
        }
    }

    private fun postPipelineStatus(status: String) {
        runOnUiThread {
            renderPipeline(status)
        }
    }

    private fun buildDefaultCalibrationMap(channelCount: Int): Map<Int, AxisCalibration> {
        return (1..channelCount).associateWith { defaultCalibration }
    }

    private fun calibrationFor(channelNumber: Int): AxisCalibration {
        return calibrationByChannel[channelNumber] ?: defaultCalibration
    }

    private fun getChannelValue(channels: IntArray, channelNumber: Int): Int {
        val index = channelNumber - 1
        return if (index in channels.indices) channels[index] else defaultCalibration.middle
    }

    private fun parseInt(value: String?, defaultValue: Int): Int {
        return value?.trim()?.toIntOrNull() ?: defaultValue
    }

    override fun onDestroy() {
        super.onDestroy()
        stopStreaming()
        readRCButtonHelper?.stop()
        readRCButtonHelper = null
        closeTransport()
        transportExecutor.shutdownNow()
    }

    private fun EditText.attachChannelWatcher(onChanged: (Int) -> Unit) {
        onChanged(parseInt(text?.toString(), 1).coerceAtLeast(1))
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                onChanged(parseInt(s?.toString(), 1).coerceAtLeast(1))
            }
        })
    }

    private data class ManualControlSample(
        val targetSystem: Int,
        val pitchRaw: Int,
        val rollRaw: Int,
        val throttleRaw: Int,
        val yawRaw: Int,
        val x: Int,
        val y: Int,
        val z: Int,
        val r: Int,
        val channels: IntArray,
    )

    private enum class TransportMode(val defaultPort: Int) {
        TCP(5760),
        UDP(14550),
    }

    private data class TransportConfig(
        val mode: TransportMode,
        val host: String,
        val remotePort: Int,
        val localPort: Int,
    )

    private interface NetworkTransport {
        fun open()
        fun send(data: ByteArray)
        fun close()
        fun connectedLabel(): String
    }

    private inner class TcpTransport(
        private val config: TransportConfig,
    ) : NetworkTransport {
        private var socket: Socket? = null
        private var output: OutputStream? = null
        private var input: InputStream? = null
        private var readerThread: Thread? = null

        override fun open() {
            val socket = Socket(config.host, config.remotePort)
            socket.soTimeout = 1000
            this.socket = socket
            output = socket.getOutputStream()
            input = socket.getInputStream()
            readerThread = Thread {
                val buffer = ByteArray(1024)
                val input = input ?: return@Thread
                while (!Thread.currentThread().isInterrupted && !socket.isClosed) {
                    try {
                        val count = input.read(buffer)
                        if (count <= 0) {
                            break
                        }
                        receivedPackets += 1
                        runOnUiThread {
                            renderPacketStats()
                            renderPipeline("TCP RX $count bytes from ${config.host}:${config.remotePort}")
                        }
                    } catch (_: SocketTimeoutException) {
                    } catch (_: Exception) {
                        break
                    }
                }
            }.apply {
                name = "manual-control-tcp-reader"
                start()
            }
        }

        override fun send(data: ByteArray) {
            val output = output ?: throw IllegalStateException("TCP socket not open")
            output.write(data)
            output.flush()
        }

        override fun close() {
            readerThread?.interrupt()
            readerThread = null
            try {
                input?.close()
            } catch (_: Exception) {
            }
            input = null
            try {
                output?.close()
            } catch (_: Exception) {
            }
            output = null
            try {
                socket?.close()
            } catch (_: Exception) {
            }
            socket = null
        }

        override fun connectedLabel(): String {
            return "TCP ${config.host}:${config.remotePort}"
        }
    }

    private inner class UdpTransport(
        private val config: TransportConfig,
    ) : NetworkTransport {
        private var socket: DatagramSocket? = null
        private var address: InetAddress? = null
        private var readerThread: Thread? = null

        override fun open() {
            address = InetAddress.getByName(config.host)
            socket = DatagramSocket(config.localPort).apply {
                soTimeout = 1000
            }
            val localSocket = socket ?: return
            readerThread = Thread {
                val buffer = ByteArray(2048)
                while (!Thread.currentThread().isInterrupted && !localSocket.isClosed) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        localSocket.receive(packet)
                        receivedPackets += 1
                        runOnUiThread {
                            renderPacketStats()
                            renderPipeline("UDP RX ${packet.length} bytes on ${config.localPort}")
                        }
                    } catch (_: SocketTimeoutException) {
                    } catch (_: Exception) {
                        break
                    }
                }
            }.apply {
                name = "manual-control-udp-reader"
                start()
            }
        }

        override fun send(data: ByteArray) {
            val socket = socket ?: throw IllegalStateException("UDP socket not open")
            val address = address ?: throw IllegalStateException("UDP address not resolved")
            socket.send(DatagramPacket(data, data.size, address, config.remotePort))
        }

        override fun close() {
            readerThread?.interrupt()
            readerThread = null
            socket?.close()
            socket = null
            address = null
        }

        override fun connectedLabel(): String {
            return "UDP ${config.host}:${config.remotePort} from local ${config.localPort}"
        }
    }
}
