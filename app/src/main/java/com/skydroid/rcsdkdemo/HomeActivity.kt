package com.skydroid.rcsdkdemo

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.skydroid.rcsdk.KeyManager
import com.skydroid.rcsdk.PipelineManager.connectPipeline
import com.skydroid.rcsdk.PipelineManager.createPipeline
import com.skydroid.rcsdk.PipelineManager.disconnectPipeline
import com.skydroid.rcsdk.RCSDKManager
import com.skydroid.rcsdk.SDKManagerCallBack
import com.skydroid.rcsdk.comm.CommListener
import com.skydroid.rcsdk.common.DeviceType
import com.skydroid.rcsdk.common.Uart
import com.skydroid.rcsdk.common.callback.CompletionCallbackWith
import com.skydroid.rcsdk.common.callback.KeyListener
import com.skydroid.rcsdk.common.error.SkyException
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        tvInfo = findViewById(R.id.tv_info)
        infoLiveData.observe(this) { str -> tvInfo?.text = str }
        //初始化SDK
        RCSDKManager.initSDK(this, object : SDKManagerCallBack {
            override fun onRcConnected() {
                //创建通讯管道(内部有断开重连机制，只需要调用一次连接即可)
                val pipeline = createPipeline(Uart.UART0)
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
                connectPipeline(pipeline)
                this@HomeActivity.pipeline = pipeline
            }

            override fun onRcConnectFail(e: SkyException?) {}
            override fun onRcDisconnect() {}
        })
        //连接到遥控器
        RCSDKManager.connectToRC()
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
                DeviceType.H12Pro -> {
                    //防止反复监听
                    KeyManager.cancelListen(keySignalQualityListener)
                    //H12Pro的信号强度为LISTEN方式,设置监听器后，会一直回调，直到取消监听
                    KeyManager.listen(
                        AirLinkKey.KeySignalQuality,
                        keySignalQualityListener
                    )
                }
                DeviceType.H16 -> {
                    //防止反复监听
                    KeyManager.cancelListen(keySignalQualityListener)
                    //H16的信号强度为LISTEN方式,设置监听器后，会一直回调，直到取消监听
                    KeyManager.listen(
                        AirLinkKey.KeyH16SignalQuality,
                        keySignalQualityListener
                    )
                }
                DeviceType.H30 -> {
                    //防止反复监听
                    KeyManager.cancelListen(keySignalQualityListener)
                    //H16的信号强度为LISTEN方式,设置监听器后，会一直回调，直到取消监听
                    KeyManager.listen(
                        AirLinkKey.KeyH30SignalQuality,
                        keySignalQualityListener
                    )
                }
            }
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
            disconnectPipeline(p)
        }
    }

    internal enum class InfoKey {
        Signal, H16Channels, Other
    }
}