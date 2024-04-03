package com.skydroid.rcsdkdemo

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
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
import java.util.*

/**
 * @author 咔一下
 * @date 2023/5/31 14:24
 * @email 1501020210@qq.com
 * @describe
 */
class HomeActivity: AppCompatActivity() {

    val TAG = "HomeActivity"

    private val keySignalQualityListener =
        KeyListener<Int> { oldValue, newValue ->
            strSignalValue = "信号强度:$newValue"
            printInfo(InfoKey.Signal, strSignalValue)
        }

    private val keyH16ChannelsListener: KeyListener<IntArray> =
        KeyListener { oldValue, newValue ->
            strH16ChannelsValue = Arrays.toString(newValue)
            printInfo(InfoKey.H16Channels, strH16ChannelsValue)
        }

    private val infoLiveData = MutableLiveData<String>()
    private var tvInfo: TextView? = null
    private var strSignalValue = ""
    private var strH16ChannelsValue = ""
    private var strOtherValue = ""

    private var pipeline: Pipeline? = null
    private var c10p: C10Pro? = null
    private var btn_akey_click_count = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        tvInfo = findViewById(R.id.tv_info)
        infoLiveData.observe(this) { str -> tvInfo?.text = str }
        //初始化SDK
        RCSDKManager.initSDK(this, object : SDKManagerCallBack {
            override fun onRcConnected() {
                //创建通讯管道(内部有断开重连机制，只需要调用一次连接即可)
                val pipeline = PipelineManager.createPipeline(Uart.UART0)
                pipeline!!.onCommListener = object : CommListener {
                    override fun onConnectSuccess() {
                        log("管道连接成功")
                    }

                    override fun onConnectFail(e: SkyException) {
                        log("管道连接失败$e")
                    }

                    override fun onDisconnect() {
                        log("管道断开连接")
                    }

                    override fun onReadData(bytes: ByteArray) {}
                }
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
        val c10p = PayloadManager.getUDPPayload(PayloadType.C10PRO, 5000,"192.168.144.108", 5000) as C10Pro?
        //内部已经实现重连机制，无需再实现
        if (c10p != null) {
            c10p.setCommListener(object : CommListener {
                override fun onConnectSuccess() {
                    log("C10Pro连接成功")
                }

                override fun onConnectFail(e: SkyException) {

                }

                override fun onDisconnect() {
                    log("C10Pro断开连接")
                }

                override fun onReadData(bytes: ByteArray) {

                }
            })
            PayloadManager.connectPayload(c10p)
        }
        this.c10p = c10p

        initTestView()
    }

    private fun initTestView() {
        findViewById<View>(R.id.btn_pairing).setOnClickListener {
            KeyManager.action(RemoteControllerKey.KeyRequestPairing) { e ->
                if (e == null) {
                    printInfo(InfoKey.Other, "对频成功")
                } else {
                    printInfo(InfoKey.Other, "对频失败：$e")
                }
            }
        }
        findViewById<View>(R.id.btn_set_control_mode).setOnClickListener {
            KeyManager.set(RemoteControllerKey.KeyControlMode, ControlMode.USA) { e ->
                if (e == null) {
                    printInfo(InfoKey.Other, "设置摇杆模式成功")
                } else {
                    printInfo(InfoKey.Other, "设置摇杆模式失败：$e")
                }
            }
        }
        findViewById<View>(R.id.btn_get_control_mode).setOnClickListener { //获取遥控器手型模式
            KeyManager.get(RemoteControllerKey.KeyControlMode, object : CompletionCallbackWith<ControlMode> {
                    override fun onSuccess(controlMode: ControlMode) {
                        printInfo(InfoKey.Other, "获取摇杆模式：" + controlMode.name)
                    }

                    override fun onFailure(e: SkyException) {
                        printInfo(InfoKey.Other, "获取摇杆模式失败：$e")
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
                            printInfo(InfoKey.Other, "获取摇杆杆量：" + Arrays.toString(value))
                        }

                        override fun onFailure(e: SkyException) {
                            printInfo(InfoKey.Other, "获取摇杆失败：$e")
                        }
                    })
                }

            }
        }
        findViewById<View>(R.id.btn_get_signal).setOnClickListener {
            when (RCSDKManager.getDeviceType()) {
                DeviceType.H12 ->                         //H12的信号强度为GET方式，需要主动请求，请求一次获取一次
                    KeyManager.get(AirLinkKey.KeyH12SignalQuality, object : CompletionCallbackWith<Int> {
                            override fun onSuccess(integer: Int) {
                                printInfo(InfoKey.Other, "H12信号强度：$integer")
                            }

                            override fun onFailure(e: SkyException) {
                                printInfo(InfoKey.Other, "H12信号强度获取失败：$e")
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
            val localC10p = c10p
            if (localC10p != null) {
                btn_akey_click_count++
                when (btn_akey_click_count % 3) {
                    0 -> localC10p.akey(AKey.DOWN)
                    1 -> localC10p.akey(AKey.MID)
                    2 -> localC10p.akey(AKey.TOP)
                }
            }
        }

        findViewById<View>(R.id.btn_rc_buttons).setOnClickListener {
            startActivity(Intent(this,CustomRCButtonsActivity::class.java))
        }
    }


    private fun printInfo(key: InfoKey, obj: Any?) {
        obj ?: return
        when (key) {
            InfoKey.Signal -> strSignalValue = obj.toString()
            InfoKey.H16Channels -> strH16ChannelsValue = obj.toString()
            InfoKey.Other -> strOtherValue = obj.toString()
        }
        val sb = StringBuffer()
        if (!TextUtils.isEmpty(strSignalValue)) {
            sb.append(strSignalValue)
            sb.append("\n")
        }
        if (!TextUtils.isEmpty(strH16ChannelsValue)) {
            sb.append(strH16ChannelsValue)
            sb.append("\n")
        }
        if (!TextUtils.isEmpty(strOtherValue)) {
            sb.append(strOtherValue)
        }
        infoLiveData.postValue(sb.toString())
        Log.e(TAG, obj.toString())
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
        val localC10p = c10p
        if (localC10p != null) {
            PayloadManager.disconnectPayload(localC10p)
        }
    }

    internal enum class InfoKey {
        Signal, H16Channels, Other
    }
}