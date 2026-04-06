package com.skydroid.rcsdkdemo

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.skydroid.rcsdk.*
import com.skydroid.rcsdk.comm.CommListener
import com.skydroid.rcsdk.common.DeviceType
import com.skydroid.rcsdk.common.Uart
import com.skydroid.rcsdk.common.callback.CompletionCallbackWith
import com.skydroid.rcsdk.common.callback.KeyListener
import com.skydroid.rcsdk.common.error.SkyException
import com.skydroid.rcsdk.common.payload.*
import com.skydroid.rcsdk.common.pipeline.Pipeline
import com.skydroid.rcsdk.common.remotecontroller.ChannelSettings
import com.skydroid.rcsdk.common.remotecontroller.ControlMode
import com.skydroid.rcsdk.common.remotecontroller.H12ChannelSettings
import com.skydroid.rcsdk.key.AirLinkKey
import com.skydroid.rcsdk.key.RemoteControllerKey
import com.skydroid.rcsdk.utils.RCSDKUtils
import com.skydroid.rcsdkdemo.other.AppUtils
import com.skydroid.rcsdkdemo.other.EnumInfoKey
import com.skydroid.rcsdkdemo.other.ReceiveInfo
import java.util.*

/**
 * @author skydroid
 * @date 2023/5/31 14:24
 * @email 1501020210@qq.com
 * @describe
 * <p>
 * Added C10Pro camera control for old/new firmware and refreshed UI; by ljb on 2024.06.13.
 */
class HomeActivity: AppCompatActivity() {

    val TAG = "HomeActivity"
    private val mReceiveInfo = ReceiveInfo()
    private val keySignalQualityListener =
        KeyListener<Int> { oldValue, newValue ->
            printInfo(EnumInfoKey.Signal, "Signal strength:$newValue %")
        }

    private val keyH16ChannelsListener: KeyListener<IntArray> =
        KeyListener { oldValue, newValue ->
            printInfo(EnumInfoKey.H16Channels, Arrays.toString(newValue))
        }

    private val infoLiveData = MutableLiveData<String>()
    private var tvInfo: TextView? = null
    private var etData: EditText? = null

    private var pipeline: Pipeline? = null
    private var c10Pro: C10Pro? = null// For firmware 0.2.7+, camera control + gimbal control for all versions
    private var c10ProCamera: C10ProCamera? = null// For firmware below 0.2.7, camera control
    private var isDataHex = false
    // TODO Notes:
    // TODO Ensure other apps (including assistant/ground station) are closed, or occupied ports can break the link;
    // TODO RC channel values are not push-based; request once per read and use at least 100ms interval;
    // TODO Telemetry pipeline fails to connect when no receiver is connected;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        tvInfo = findViewById(R.id.tv_info)
        etData = findViewById(R.id.et_data)
        infoLiveData.observe(this) { str -> tvInfo?.text = str }
        // TODO Initialize SDK once.
        RCSDKManager.initSDK(this, object : SDKManagerCallBack {
            override fun onRcConnected() {
                // Create communication pipeline (with reconnect retry built in; call connect once)
                // Telemetry pipeline fails to connect when no receiver is connected;
                val pipeline = PipelineManager.createPipeline(Uart.UART0)
                pipeline!!.onCommListener = getCommListener(0, "Telemetry pipeline")
                // Connect communication pipeline
                PipelineManager.connectPipeline(pipeline)
                this@HomeActivity.pipeline = pipeline
            }

            override fun onRcConnectFail(e: SkyException?) {}
            override fun onRcDisconnect() {}
        })
        RCSDKManager.setMainThreadCallBack(true) // callback on main thread
        // Connect to remote controller
        RCSDKManager.connectToRC()

        // Legacy wired three-body camera
//        val threeBodyCamera2 = PayloadManager.getTCPPayload(PayloadType.THREE_BODY_CAMERA2, "192.168.144.108", 5001) as ThreeBodyCamera2?
        // Legacy serial three-body camera
//        val threeBodyCamera = PayloadManager.getSerialPortPayload(PayloadType.THREE_BODY_CAMERA, "/dev/ttyHS0", 4000000) as ThreeBodyCamera?
        // C20 camera
        val c20Camera = PayloadManager.getTCPPayload(PayloadType.C20_CAMERA, "192.168.144.108", 8100) as C20Camera?
        // C20 gimbal
//        val c20Gimbal = PayloadManager.getTCPPayload(PayloadType.C20_GIMBAL, "192.168.144.108", 5000) as C20Gimbal?

        // C10Pro camera control (or new Ethernet three-body camera)
        c10Pro = PayloadManager.getUDPPayload(PayloadType.C10PRO, 5000, "192.168.144.108", 5000) as C10Pro?
        // Internal reconnection already implemented
        c10Pro?.let {
            it.setCommListener(getCommListener(1, "C10Pro"))
            PayloadManager.connectPayload(it)
        }
        c10ProCamera = PayloadManager.getUDPPayload(PayloadType.C10PRO_CAMERA, 12580, "192.168.144.108", 12580) as C10ProCamera?
        // Internal reconnection already implemented
        c10ProCamera?.let {
            it.setCommListener(getCommListener(2, "C10pCamera"))
            PayloadManager.connectPayload(it)
        }
        initTestView()
        setTitle("RCSDK_Demo_V${RCSDKUtils.getVersion()}  Device:${RCSDKUtils.getDeviceType()}")
        val rg_data = findViewById<RadioGroup>(R.id.rg_data)
        rg_data?.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.rb_data_txt -> {
                    isDataHex = false
                }
                R.id.rb_data_hex -> {
                    isDataHex = true
                }
            }
        }
        rg_data?.check(R.id.rb_data_txt)
    }

    private fun getCommListener(type: Int, tag: String): CommListener {
        return object : CommListener {
            override fun onConnectSuccess() {
                log("$tag connected")
            }

            override fun onConnectFail(e: SkyException) {
                log("$tag  connect failed$e")
            }

            override fun onDisconnect() {
                log("$tag disconnected")
            }

            override fun onReadData(bytes: ByteArray) {
                if(type == 0){
                    log("$tag received length ${bytes.size}, data: ${String(bytes)}")
                    // Telemetry pipeline
                    printInfo(EnumInfoKey.DataTransmission, "Telemetry data：${if (isDataHex) String2ByteArrayUtils.bytes2Hex(bytes) else String(bytes)}")
                }
            }
        }
    }

    private fun initTestView() {
        findViewById<View>(R.id.btn_pairing).setOnClickListener {
            KeyManager.action(RemoteControllerKey.KeyRequestPairing) { e ->
                printInfo(EnumInfoKey.Other, AppUtils.getSkyExceptionInfo("Pairing", e, ""));
            }
        }
        findViewById<View>(R.id.btn_set_control_mode).setOnClickListener {
            KeyManager.set(RemoteControllerKey.KeyControlMode, ControlMode.USA) { e ->
                printInfo(EnumInfoKey.SetControlMode, AppUtils.getSkyExceptionInfo("Set stick mode", e, ""));
            }
        }
        findViewById<View>(R.id.btn_get_control_mode).setOnClickListener { // Get RC stick mode
            KeyManager.get(RemoteControllerKey.KeyControlMode, object : CompletionCallbackWith<ControlMode> {
                override fun onSuccess(controlMode: ControlMode) {
                    printInfo(EnumInfoKey.GetControlMode, "Get stick mode: " + controlMode.name)
                }

                override fun onFailure(e: SkyException) {
                    printInfo(EnumInfoKey.GetControlMode, "Get stick mode failed: $e")
                }
            })
        }
        findViewById<View>(R.id.btn_get_channels).setOnClickListener {
            //Get RC channel values
            when (RCSDKManager.getDeviceType()) {
                DeviceType.H16 -> {
                    // Avoid registering repeated listeners
                    KeyManager.cancelListen(keyH16ChannelsListener)
                    // H16/H16Pro channel values are LISTEN mode; listener will keep receiving updates until removed
                    KeyManager.listen(RemoteControllerKey.KeyH16Channels, keyH16ChannelsListener)
                }
                //H12/H12Pro/H30 channel values use GET mode and need explicit request each time
                else ->{
                    KeyManager.get(RemoteControllerKey.KeyChannels,object : CompletionCallbackWith<IntArray> {
                        override fun onSuccess(value: IntArray?) {
                            printInfo(EnumInfoKey.Channels, "Get RC channel values：" + Arrays.toString(value))
                        }

                        override fun onFailure(e: SkyException) {
                            printInfo(EnumInfoKey.Channels, "Failed to get channels: $e")
                        }
                    })
                }

            }
        }
        findViewById<View>(R.id.btn_get_channels_settings).setOnClickListener {

            when (RCSDKManager.getDeviceType()) {
                DeviceType.H12 ->
                    KeyManager.get(RemoteControllerKey.KeyH12ChannelSettings, object : CompletionCallbackWith<H12ChannelSettings> {
                        override fun onSuccess(settings: H12ChannelSettings) {
                            printInfo(EnumInfoKey.Other, "H12 channel settings: ${settings.channels.contentToString()}")
                        }

                        override fun onFailure(e: SkyException) {
                            printInfo(EnumInfoKey.Other, "H12 channel settings: $e")
                        }
                    })
                else -> {
                    KeyManager.get(RemoteControllerKey.KeyChannelSettings,object : CompletionCallbackWith<ChannelSettings> {
                        override fun onSuccess(settings: ChannelSettings?) {
                            printInfo(EnumInfoKey.Other, "Channel settings: ${settings?.channels.contentToString()}")
                        }

                        override fun onFailure(e: SkyException) {
                            printInfo(EnumInfoKey.Other, "Channel settings: $e")
                        }
                    })
                }
            }
        }
        // Signal strength range: 0-100%
        findViewById<View>(R.id.btn_get_signal).setOnClickListener {
            when (RCSDKManager.getDeviceType()) {
                DeviceType.H12 ->                         // H12 signal strength uses GET mode and needs explicit request each time
                    KeyManager.get(AirLinkKey.KeyH12SignalQuality, object : CompletionCallbackWith<Int> {
                        override fun onSuccess(integer: Int) {
                            printInfo(EnumInfoKey.Signal, "H12 signal strength: $integer %")
                        }

                        override fun onFailure(e: SkyException) {
                            printInfo(EnumInfoKey.Signal, "Failed to get H12 signal strength: $e")
                        }
                    })
                else -> {
                    // Avoid registering repeated listeners
                    KeyManager.cancelListen(keySignalQualityListener)
                    // Other RC signal values are LISTEN mode and keep streaming after listener registration until removed
                    KeyManager.listen(
                            AirLinkKey.KeySignalQuality,
                            keySignalQualityListener
                    )
                }
            }
        }
        findViewById<View>(R.id.btn_akey).setOnClickListener {
            c10pCameraControl(false)
        }
        findViewById<View>(R.id.btn_akey_027).setOnClickListener {
            c10pCameraControl(true)
        }
        findViewById<View>(R.id.btn_rc_buttons).setOnClickListener {
            startActivity(Intent(this,CustomRCButtonsActivity::class.java))
        }
        findViewById<View>(R.id.btn_manual_control_demo).setOnClickListener {
            startActivity(Intent(this, ManualControlDemoActivity::class.java))
        }
        findViewById<View>(R.id.btn_clear).setOnClickListener {
            mReceiveInfo.cleatInfo()
            tvInfo?.text = ""
        }
        findViewById<View>(R.id.btn_send).setOnClickListener {
            val temp = etData?.text?.toString() ?: ""
            if (TextUtils.isEmpty(temp)) {
                Toast.makeText(applicationContext, "Please input data to send!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            this@HomeActivity.pipeline?.writeData(temp.toByteArray())
            Toast.makeText(applicationContext, "Sent $temp", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btn_led).setOnClickListener {
            ledCameraControl(false)
        }
        findViewById<View>(R.id.btn_led_027).setOnClickListener {
            ledCameraControl(true)
        }
    }

    /**
     * New Ethernet C12 camera LED control
     */
    private fun ledCameraControl(isCameraVer027AndAbove: Boolean){
        AppUtils.showLEDCameraControlDialog(this){
            _, p1 ->
            when(p1){
                0 -> {
                    if (isCameraVer027AndAbove){
                        c10Pro?.setLed(true){
                            printInfo(EnumInfoKey.LED, AppUtils.getSkyExceptionInfo("LED On", it, "FW 0.2.7+"));
                        }
                    }else{
                        c10ProCamera?.setLED(true){
                            printInfo(EnumInfoKey.LED, AppUtils.getSkyExceptionInfo("LED On", it, "Legacy FW"));
                        }
                    }
                }
                1 -> {
                    if (isCameraVer027AndAbove){
                        c10Pro?.setLed(false){
                            printInfo(EnumInfoKey.LED, AppUtils.getSkyExceptionInfo("LED Off", it, "FW 0.2.7+"));
                        }
                    }else{
                        c10ProCamera?.setLED(false){
                            printInfo(EnumInfoKey.LED, AppUtils.getSkyExceptionInfo("LED Off", it, "Legacy FW"));
                        }
                    }
                }
            }
        }
    }

    /**
     * Gimbal control + camera control
     */
    private fun c10pCameraControl(isCameraVer027AndAbove: Boolean) {
        AppUtils.showC10pCameraControlDialog(this@HomeActivity) { _, p1 ->
            when (p1) {
                0 -> {
                    // Method 1: API
                    c10ProCamera?.getVersion(object : CompletionCallbackWith<String> {
                        override fun onSuccess(version: String?) {
                            printInfo(EnumInfoKey.CameraVersion, "Camera version: ${version}")
                        }

                        override fun onFailure(p0: SkyException?) {
                            printInfo(EnumInfoKey.CameraVersion, "Camera version: ${p0}")
                        }
                    })
                    // Method 2: protocol
                    //c10ProCamera?.writeData("AT+INFO\r\n".toByteArray())
                }
                1 -> {
                    c10Pro?.akey(AKey.DOWN)
                }
                2 -> {
                    c10Pro?.akey(AKey.MID)
                }
                3 -> {
                    c10Pro?.akey(AKey.TOP)
                }
                4 -> {
                    if (isCameraVer027AndAbove) {
                        // Method 1: API
                        c10Pro?.takePicture {
                            printInfo(EnumInfoKey.TakePicture, AppUtils.getSkyExceptionInfo("Take Picture", it, "FW 0.2.7+"));
                        }
                        // Method 2: protocol
                        //c10Pro?.writeData("#TPUD2wCAP013E".toByteArray())
                    } else {
                        // Method 1: API
                        c10ProCamera?.takePicture {
                            printInfo(EnumInfoKey.TakePicture, AppUtils.getSkyExceptionInfo("Take Picture", it, "Legacy FW"));
                        }
                        // Method 2: protocol
                        //c10ProCamera?.writeData("AT+AZ -p2\r\n".toByteArray())
                    }
                }
                5 -> {
                    if (isCameraVer027AndAbove) {
                        // Method 1: API
                        c10Pro?.startRecordVideo {
                            printInfo(EnumInfoKey.RecordVideo, AppUtils.getSkyExceptionInfo("Start recording", it, "FW 0.2.7+"));
                        }
                        // Method 2: protocol
                        // c10Pro?.writeData("#TPUD2wREC0144".toByteArray())
                    } else {
                        // Method 1: API
                        c10ProCamera?.startRecordVideo {
                            printInfo(EnumInfoKey.RecordVideo, AppUtils.getSkyExceptionInfo("Start recording", it, "Legacy FW"));
                        }
                        // Method 2: protocol
                        // c10ProCamera?.writeData("AT+AZ -p0\r\n".toByteArray())
                    }
                }
                6 -> {
                    if (isCameraVer027AndAbove) {
                        // Method 1: API
                        c10Pro?.stopRecordVideo {
                            printInfo(EnumInfoKey.RecordVideo, AppUtils.getSkyExceptionInfo("Stop recording", it, "FW 0.2.7+"));
                        }
                        // Method 2: protocol
                        // c10Pro?.writeData("#TPUD2wREC0043".toByteArray())
                    } else {
                        // Method 1: API
                        c10ProCamera?.stopRecordVideo {
                            printInfo(EnumInfoKey.RecordVideo, AppUtils.getSkyExceptionInfo("Stop recording", it, "Legacy FW"));
                        }
                        // Method 2: protocol
                        // c10ProCamera?.writeData("AT+AZ -p1\r\n".toByteArray())
                    }
                }
                7 -> {
                    if (isCameraVer027AndAbove) {
                        c10Pro?.setTime(System.currentTimeMillis()) {
                            printInfo(EnumInfoKey.CameraTime, AppUtils.getSkyExceptionInfo("Set time", it, "FW 0.2.7+"));
                        }
                    } else {
                        c10ProCamera?.setTime(System.currentTimeMillis()) {
                            printInfo(EnumInfoKey.CameraTime, AppUtils.getSkyExceptionInfo("Set time", it, "Legacy FW"));
                        }
                    }
                }
                8 -> {// Yaw command, right, speed 30
                    // c10Pro?.controlYaw(3f)// Method 1: API
                    // c10Pro?.writeData("#TPUG2wGSY1E75".toByteArray())// Method 2: protocol

                    c10Pro?.writeData("#TPUG2wGSY6469".toByteArray())// Method 2: protocol speed 100
                }
                9 -> {// Yaw command, left, speed -30
                    // c10Pro?.controlYaw(-3f)// Method 1: API
                    //  c10Pro?.writeData("#TPUG2wGSYE276".toByteArray())// Method 2: protocol
                    c10Pro?.writeData("#TPUG2wGSY9C7B".toByteArray())// Method 2: protocol speed 100
                }
                10 -> {// Pitch command, up, speed 30
                    // c10Pro?.controlPitch(3f)// Method 1: API
                    // c10Pro?.writeData("#TPUG2wGSP1E6C".toByteArray())// Method 2: protocol
                    c10Pro?.writeData("#TPUG2wGSP6460".toByteArray())// Method 2: protocol speed 100
                }
                11 -> {// Pitch command, down, speed -30
                    // c10Pro?.controlPitch(-3f)// Method 1: API
                    // c10Pro?.writeData("#TPUG2wGSPE26D".toByteArray())// Method 2: protocol
                    c10Pro?.writeData("#TPUG2wGSP9C72".toByteArray())// Method 2: protocol speed 100
                }
            }
        }

    }

    private fun printInfo(key: EnumInfoKey, obj: Any?) {
        obj ?: return
        val sb = mReceiveInfo.updateInfo(key, obj)
        sb?.let {
            infoLiveData.postValue(sb)
        }
        log("printInfo -------key $key,,,obj $obj")
    }

    private fun log(obj: Any?) {
        if (obj == null) {
            return
        }
        Log.e(TAG, obj.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        infoLiveData.removeObservers(this)
        // Disconnect remote controller connect (if not disconnected, other apps may fail to open needed ports)
        RCSDKManager.disconnectRC()
        KeyManager.cancelListen(keySignalQualityListener)
        val p = pipeline
        if (p != null) {
            PipelineManager.disconnectPipeline(p)
        }
        val localC10p = c10Pro
        if (localC10p != null) {
            PayloadManager.disconnectPayload(localC10p)
        }
    }
}
