package com.skydroid.rcsdkdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
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
import com.skydroid.rcsdk.common.remotecontroller.ControlMode
import com.skydroid.rcsdk.key.AirLinkKey
import com.skydroid.rcsdk.key.RemoteControllerKey
import com.skydroid.rcsdk.utils.RCSDKUtils
import com.skydroid.rcsdkdemo.other.AppUtils
import com.skydroid.rcsdkdemo.other.EnumInfoKey
import com.skydroid.rcsdkdemo.other.ReceiveInfo
import java.util.*

/**
 * @author 咔一下
 * @date 2023/5/31 14:24
 * @email 1501020210@qq.com
 * @describe
 * <p>
 * 加入C10Pro 新旧固件 相机控制;UI重新整理; by ljb on 2024.06.13.
 */
class HomeActivity: AppCompatActivity() {

    val TAG = "HomeActivity"
    private val mReceiveInfo = ReceiveInfo()
    private val keySignalQualityListener =
        KeyListener<Int> { oldValue, newValue ->
            printInfo(EnumInfoKey.Signal, "信号强度:$newValue %")
        }

    private val keyH16ChannelsListener: KeyListener<IntArray> =
        KeyListener { oldValue, newValue ->
            printInfo(EnumInfoKey.H16Channels, Arrays.toString(newValue))
        }

    private val infoLiveData = MutableLiveData<String>()
    private var tvInfo: TextView? = null

    private var pipeline: Pipeline? = null
    private var c10Pro: C10Pro? = null// 适用于0.2.7及以上固件 相机控制 + 全版本的云台控制
    private var c10ProCamera: C10ProCamera? = null// 适用于0.2.7以下固件 相机控制
    // TODO 注意:
    // TODO 使用时,请确保其他应用(包含助手、地面站)处于停止关闭状态,避免端口占用导致数据链路失败;
    // TODO 获取摇杆杆量值,无法主动上报,请求一次获取一次,推荐至少100ms读取一次;
    // TODO 数传管道,未连接 接收机 时,数传管道 连接失败;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        tvInfo = findViewById(R.id.tv_info)
        infoLiveData.observe(this) { str -> tvInfo?.text = str }
        // TODO 初始化SDK,初始化一次即可;
        RCSDKManager.initSDK(this, object : SDKManagerCallBack {
            override fun onRcConnected() {
                //创建通讯管道(内部有断开重连机制，只需要调用一次连接即可)
                // 数传管道,未连接 接收机 时,数传管道 连接失败;
                val pipeline = PipelineManager.createPipeline(Uart.UART0)
                pipeline!!.onCommListener = getCommListener(0, "数传管道")
                //连接通讯管道
                PipelineManager.connectPipeline(pipeline)
                this@HomeActivity.pipeline = pipeline
            }

            override fun onRcConnectFail(e: SkyException?) {}
            override fun onRcDisconnect() {}
        })
        //连接到遥控器
        RCSDKManager.connectToRC()

        //三体相机网口版
//        val threeBodyCamera2 = PayloadManager.getTCPPayload(PayloadType.THREE_BODY_CAMERA2, "192.168.144.108", 5001) as ThreeBodyCamera2?
        //三体相机串口版
//        val threeBodyCamera = PayloadManager.getSerialPortPayload(PayloadType.THREE_BODY_CAMERA, "/dev/ttyHS0", 4000000) as ThreeBodyCamera?
        //C20相机
        val c20Camera = PayloadManager.getTCPPayload(PayloadType.C20_CAMERA, "192.168.144.108", 8100) as C20Camera?
        //C20云台
//        val c20Gimbal = PayloadManager.getTCPPayload(PayloadType.C20_GIMBAL, "192.168.144.108", 5000) as C20Gimbal?

        //C10Pro相机控制
        c10Pro = PayloadManager.getUDPPayload(PayloadType.C10PRO, 5000, "192.168.144.108", 5000) as C10Pro?
        //内部已经实现重连机制，无需再实现
        c10Pro?.let {
            it.setCommListener(getCommListener(1, "C10Pro"))
            PayloadManager.connectPayload(it)
        }
        c10ProCamera = PayloadManager.getUDPPayload(PayloadType.C10PRO_CAMERA, 12580, "192.168.144.108", 12580) as C10ProCamera?
        //内部已经实现重连机制，无需再实现
        c10ProCamera?.let {
            it.setCommListener(getCommListener(2, "C10pCamera"))
            PayloadManager.connectPayload(it)
        }
        initTestView()
        setTitle("RCSDK_Demo_V${RCSDKUtils.getVersion()}")
    }

    private fun getCommListener(type: Int, tag: String): CommListener {
        return object : CommListener {
            override fun onConnectSuccess() {
                log("$tag 连接成功")
            }

            override fun onConnectFail(e: SkyException) {
                log("$tag  连接失败$e")
            }

            override fun onDisconnect() {
                log("$tag 断开连接")
            }

            override fun onReadData(bytes: ByteArray) {
                if(type == 0){
                    log("$tag 收到长度${bytes.size},,, 数据 "+ String(bytes))
                }
            }
        }
    }

    private fun initTestView() {
        findViewById<View>(R.id.btn_pairing).setOnClickListener {
            KeyManager.action(RemoteControllerKey.KeyRequestPairing) { e ->
                printInfo(EnumInfoKey.Other, AppUtils.getSkyExceptionInfo("对频", e, ""));
            }
        }
        findViewById<View>(R.id.btn_set_control_mode).setOnClickListener {
            KeyManager.set(RemoteControllerKey.KeyControlMode, ControlMode.USA) { e ->
                printInfo(EnumInfoKey.SetControlMode, AppUtils.getSkyExceptionInfo("设置摇杆模式", e, ""));
            }
        }
        findViewById<View>(R.id.btn_get_control_mode).setOnClickListener { //获取遥控器手型模式
            KeyManager.get(RemoteControllerKey.KeyControlMode, object : CompletionCallbackWith<ControlMode> {
                override fun onSuccess(controlMode: ControlMode) {
                    printInfo(EnumInfoKey.GetControlMode, "获取摇杆模式：" + controlMode.name)
                }

                override fun onFailure(e: SkyException) {
                    printInfo(EnumInfoKey.GetControlMode, "获取摇杆模式失败：$e")
                }
            })
        }
        findViewById<View>(R.id.btn_get_channels).setOnClickListener {
            //获取摇杆杆量
            when (RCSDKManager.getDeviceType()) {
                DeviceType.H16 -> {
                    //防止反复监听
                    KeyManager.cancelListen(keyH16ChannelsListener)
                    //H16/H16Pro的摇杆杆量为LISTEN方式,设置监听器后，会一直回调，直到取消监听
                    KeyManager.listen(RemoteControllerKey.KeyH16Channels, keyH16ChannelsListener)
                }
                //H12/H12Pro/H30摇杆杆量为GET方式，需要主动请求，请求一次获取一次
                else ->{
                    KeyManager.get(RemoteControllerKey.KeyChannels,object : CompletionCallbackWith<IntArray> {
                        override fun onSuccess(value: IntArray?) {
                            printInfo(EnumInfoKey.Channels, "获取摇杆杆量：" + Arrays.toString(value))
                        }

                        override fun onFailure(e: SkyException) {
                            printInfo(EnumInfoKey.Channels, "获取摇杆失败：$e")
                        }
                    })
                }

            }
        }
        // 信号强度 取值范围: 0-100%
        findViewById<View>(R.id.btn_get_signal).setOnClickListener {
            when (RCSDKManager.getDeviceType()) {
                DeviceType.H12 ->                         //H12的信号强度为GET方式，需要主动请求，请求一次获取一次
                    KeyManager.get(AirLinkKey.KeyH12SignalQuality, object : CompletionCallbackWith<Int> {
                        override fun onSuccess(integer: Int) {
                            printInfo(EnumInfoKey.Signal, "H12信号强度：$integer %")
                        }

                        override fun onFailure(e: SkyException) {
                            printInfo(EnumInfoKey.Signal, "H12信号强度获取失败：$e")
                        }
                    })
                DeviceType.H12Pro,DeviceType.H16,DeviceType.H30,DeviceType.H20 -> {
                    //防止反复监听
                    KeyManager.cancelListen(keySignalQualityListener)
                    //H12Pro/H16/H30/H20的信号强度为LISTEN方式,设置监听器后，会一直回调，直到取消监听
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
        findViewById<View>(R.id.btn_clear).setOnClickListener {
            mReceiveInfo.cleatInfo()
            tvInfo?.text = ""
        }
    }

    /**
     * 云台控制_相机控制
     */
    private fun c10pCameraControl(isCameraVer027AndAbove: Boolean) {
        AppUtils.showC10pCameraControlDialog(this@HomeActivity) { _, p1 ->
            when (p1) {
                0 -> {
                    c10ProCamera?.getVersion(object : CompletionCallbackWith<String> {
                        override fun onSuccess(version: String?) {
                            printInfo(EnumInfoKey.CameraVersion, "获取到相机版本号:${version}")
                        }

                        override fun onFailure(p0: SkyException?) {
                            printInfo(EnumInfoKey.CameraVersion, "获取到相机版本号:${p0}")
                        }
                    })
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
                        c10Pro?.takePicture {
                            printInfo(EnumInfoKey.TakePicture, AppUtils.getSkyExceptionInfo("拍照", it, "新固件"));
                        }
                    } else {
                        c10ProCamera?.takePicture {
                            printInfo(EnumInfoKey.TakePicture, AppUtils.getSkyExceptionInfo("拍照", it, "旧固件"));
                        }
                    }
                }
                5 -> {
                    if (isCameraVer027AndAbove) {
                        c10Pro?.startRecordVideo {
                            printInfo(EnumInfoKey.RecordVideo, AppUtils.getSkyExceptionInfo("开始录像", it, "新固件"));
                        }
                    } else {
                        c10ProCamera?.startRecordVideo {
                            printInfo(EnumInfoKey.RecordVideo, AppUtils.getSkyExceptionInfo("开始录像", it, "旧固件"));
                        }
                    }
                }
                6 -> {
                    if (isCameraVer027AndAbove) {
                        c10Pro?.stopRecordVideo {
                            printInfo(EnumInfoKey.RecordVideo, AppUtils.getSkyExceptionInfo("停止录像", it, "新固件"));
                        }
                    } else {
                        c10ProCamera?.stopRecordVideo {
                            printInfo(EnumInfoKey.RecordVideo, AppUtils.getSkyExceptionInfo("停止录像", it, "旧固件"));
                        }
                    }
                }
                7 -> {
                    if (isCameraVer027AndAbove) {
                        c10Pro?.setTime(System.currentTimeMillis()) {
                            printInfo(EnumInfoKey.CameraTime, AppUtils.getSkyExceptionInfo("时间设置", it, "新固件"));
                        }
                    } else {
                        c10ProCamera?.setTime(System.currentTimeMillis()) {
                            printInfo(EnumInfoKey.CameraTime, AppUtils.getSkyExceptionInfo("时间设置", it, "旧固件"));
                        }
                    }
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
        //断开遥控器连接（如果不断开，程序还在运行的时候，其他程序会出端口占用情况）
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