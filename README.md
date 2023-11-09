更新日志
```
v0.8.5
1.修复同时多个TCP连接时阻塞问题
2.修复PipelineManager,PayloadManager连接不上时无法关闭问题

v0.8.3
1.新增云卓配件管理（C10、三体相机等）
2.修复UDPPipeline连接状态错误问题
3.新增Key:
    AirLinkKey.KeyH16RawSignalQuality(获取H16原始信号值DBM)

v0.7.1
新增H30支持

v0.6
新增H16支持

v0.1
发布第一版
```

# Demo 工程

下载或者克隆Git上的Android示例代码工程:https://gitee.com/skydroid/rcsdk-demo

# RCSDK目前支持的遥控器产品
H12、H12Pro、H16/H16Pro、H30

# RCSDK架构体系概述
移动应用程序一般通过下图所示的几个主要类来访问RCSDK：
![image](https://gitee.com/skydroid/rcsdk-demo/raw/master/image/rcsdk.png)

- RCSDKManager： RCSDK工具包的入口类，管理RCSDK的初始化，反初始化，连接，以及监听硬件产品的连接事件。
- KeyManager： RCSDK使用了以Key为基础元素的参数设置和参数获取功能接口
- PipelineManager：与第三方设备数据传输的入口
- PayloadManager：控制云卓相关配件(C10、三体相机等)的入口


# 空白项目集成 SDK
本指引介绍如何将 RCSDK-Demo 中的 RCSDK包移植到用户的空白项目中

```
本指引中使用的 Android Studio 版本为 Android Studio Chipmunk | 2021.2.1 Patch 1

SDK所需权限
<uses-permission android:name="android.permission.INTERNET" />

Kotlin版本为：1.6.10

混淆
-keep class com.skydroid.**{*;}
```
- ### 导入SDK AAR包

```
rcsdk-v0.8.5-alpha.aar
h16_airlink.aar //H16图传模块 minSdk 24
```

- ### 修改build.gradle(app) 文件
在 dependencies 项里添加SDK包
```
    implementation files("libs/rcsdk-v0.8.5-alpha.aar")
    implementation files('libs/h16_airlink.aar')//可选,H16遥控器图传模块,如果不是H16遥控器,无需导入,该模块minSdk为24
```

- ### 修改 AndroidManifest.xml 文件

参照 Demo 的 AndroidManifest.xml添加SDK 需要的最基础权限
```
<uses-permission android:name="android.permission.INTERNET" />
```

- ### 初始化RCSDK
```
RCSDKManager.initSDK(this,object :SDKManagerCallBack{
            override fun onRcConnectFail(e: SkyException?) {
                //连接失败
            }

            override fun onRcConnected() {
                //设备连接
            }

            override fun onRcDisconnect() {
                //设备断开连接
            }
        })
```

- ### 连接遥控器
```
RCSDKManager.connectToRC()
```

- ### 断开遥控器
注意：不使用时需要断开连接，否则会一直占用端口
```
RCSDKManager.disconnectRC()
```

# KeyManager
遥控器参数设置、获取功能接口

- ### SET
```
//设置遥控器控制模式
KeyManager.set(RemoteControllerKey.KeyControlMode, ControlMode.JP) {
                        e ->
                    if (e == null){
                        log("设置摇杆模式成功") //success
                    }else{
                        log("设置摇杆模式失败：${e}") } //fail
                    }
```

- ### GET
```
获取遥控器控制模式
KeyManager.get(RemoteControllerKey.KeyControlMode,object :
                    CompletionCallbackWith<ControlMode> {
                    override fun onSuccess(result: ControlMode?) {
                        //获取成功
                        log(result)
                    }

                    override fun onFailure(e: SkyException?) {
                        //获取失败
                        log(e)
                    }
                })
```

- ### ACTION
```
遥控器对频
KeyManager.action(RemoteControllerKey.KeyRequestPairing){
                e ->
                if (e == null){
                    log("对频成功") //success
                }else{
                    log("对频失败：${e}") //fail
                }
            }
```

- ### LISTEN
```
var keySignalQualityListener = KeyListener<Int>{
        oldValue, newValue ->
        Log.e(TAG,"信号强度:${oldValue},${newValue}")
    }
    
//监听H12Pro信号强度
KeyManager.listen(AirLinkKey.KeySignalQuality,keySignalQualityListener)

//取消监听H12Pro信号强度
KeyManager.cancelListen(keySignalQualityListener)
```

# PipelineManager
与第三方设备通讯接口

- ### 与第三方设备(例如飞控)通讯
```
//创建通讯管道
pipeline = PipelineManager.createPipeline()
pipeline?.let {
    //设置监听
    it.onCommListener = object : CommListener{
        override fun onConnectSuccess() {
            log("管道连接成功")
        }

        override fun onConnectFail(e: SkyException?) {
            log("管道连接失败${e}")
        }

        override fun onDisconnect() {
            log("管道断开连接")
        }

        override fun onReadData(data: ByteArray?) {
            //第三方设备发送的数据
        }

    }
    //连接通讯管道
    PipelineManager.connectPipeline(it)
}

//发送数据到第三方设备
pipeline?.let {
    it.writeData(bytes)
}

//断开通讯管道
pipeline?.let {
    PipelineManager.disconnectPipeline(it)
}
```

自定义创建通讯管道方法
```
//根据遥控器类型创建通讯管道
PipelineManager.createPipeline(DeviceType.H12Pro)

//创建自定义串口通讯管道
PipelineManager.createSerialPipeline("/dev/ttyHS1",921600)

//创建UDP通讯管道
//参数1:本地端口号;参数2:远程接收端IP;参数3:远程接收端端口号
PipelineManager.createUDPPipeline(14550,"192.168.144.10",14550)

//创建TCP通讯管道
PipelineManager.createTCPPipeline("192.168.144.101",14550)

//创建串口0通讯管道
PipelineManager.createPipeline(Uart.UART0)

//创建串口1通讯管道
PipelineManager.createPipeline(Uart.UART1)
```

# Key
### RemoteControllerKey
- ##### KeyControlMode
```
    /**
     * 遥控器摇杆模式
     * 访问方式
     * SET,GET
     * 支持ALL
     */
    val KeyControlMode: KeyInfo<ControlMode> = KeyInfo.Builder<ControlMode>()
        .canSet(true)
        .canGet(true)
```

- ##### KeyH12ChannelSettings
```
    /**
     * H12通道
     * 访问方式
     * SET,GET
     * 支持H12
     */
    val KeyH12ChannelSettings: KeyInfo<H12ChannelSettings> = KeyInfo.Builder<H12ChannelSettings>()
        .canSet(true)
        .canGet(true)
```

- ##### KeyChannelSettings
```
    /**
     * 通道设置
     * 访问方式
     * SET,GET
     * 支持H12Pro/H16/H30
     */
    val KeyChannelSettings: KeyInfo<ChannelSettings> = KeyInfo.Builder<ChannelSettings>()
        .canSet(true)
        .canGet(true)
```

- ##### KeyRequestPairing
```
    /**
     * 遥控器对频
     * 访问方式
     * ACTION
     * 支持ALL
     */
    val KeyRequestPairing: KeyInfo<EmptyMsg> = KeyInfo.Builder<EmptyMsg>()
        .canAction(true)
```

- ##### KeySerialNumber
```
    /**
     * 遥控器序列号
     * 访问方式
     * GET
     * 支持ALL
     */
    val KeySerialNumber: KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)
```

- ##### KeyChannels
```
    /**
     * 遥控器通道值
     * 访问方式
     * GET
     * 支持H12/H12Pro/H30
     */
    val KeyChannels: KeyInfo<IntArray> = KeyInfo.Builder<IntArray>()
        .canGet(true)
```

- ##### KeyH16Channels
```
    /**
     * H16遥控器通道值
     * 访问方式
     * LISTEN
     * 支持H16
     */
    val KeyH16Channels: KeyInfo<IntArray> = KeyInfo.Builder<IntArray>()
        .canListen(true)
```

- ##### KeyCoachMode
```
    /**
     * 教练模式
     * 访问方式
     * SET,GET
     * 支持H12Pro/H16/H30
     */
    val KeyCoachMode: KeyInfo<CoachMode> = KeyInfo.Builder<CoachMode>()
        .canSet(true)
        .canGet(true)
```

- ##### KeyCustomData
```
    /**
     * 自定义数据 200byte
     * 访问方式
     * GET,SET
     * 支持ALL
     */
    val KeyCustomData: KeyInfo<ByteArray> = KeyInfo.Builder<ByteArray>()
        .canGet(true)
        .canSet(true)
```

### AirLinkKey

- ##### KeyUart0BaudRate
```
    /**
     * 图传接收机串口0波特率
     * 访问方式
     * SET,GET
     * 支持H12Pro
     */
    val KeyUart0BaudRate:KeyInfo<UartBaudRate> = KeyInfo.Builder<UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### KeyLostSBUSValues
```
    /**
     * 图传接收机RC通道失控保护值
     * 访问方式
     * SET,GET
     * 支持H12Pro
     */
    val KeyLostSBUSValues:KeyInfo<LostSBUSValues> = KeyInfo.Builder<LostSBUSValues>()
        .canSet(true)
        .canGet(true)
```

- ##### KeySignalQuality
```
    /**
     * 图传接收机信号质量
     * 访问方式
     * LISTEN
     * 支持H12Pro
     */
    val KeySignalQuality:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canListen(true)
```

- ##### KeyH12SignalQuality
```
    /**
     * H12图传接收机信号质量
     * 访问方式
     * GET
     * 仅支持H12
     */
    val KeyH12SignalQuality:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canGet(true)
```

- ##### KeyReceiverOptions
```
    /**
     * 接收机选项设置
     * 访问方式
     * SET,GET
     * 仅支持H12
     */
    val KeyReceiverOptions: KeyInfo<ReceiverOptions> = KeyInfo.Builder<ReceiverOptions>()
        .canSet(true)
        .canGet(true)
```

- ##### KeyH16Uart0BaudRate
```
    /**
     * H16图传接收机串口0波特率
     * 访问方式
     * SET,GET
     * 仅支持H16
     */
    val KeyH16Uart0BaudRate:KeyInfo<UartBaudRate> = KeyInfo.Builder<UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### KeyH16Uart1BaudRate
```
    /**
     * H16图传接收机串口1波特率
     * 访问方式
     * SET,GET
     * 仅支持H16
     */
    val KeyH16Uart1BaudRate:KeyInfo<UartBaudRate> = KeyInfo.Builder<UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### KeyH16SignalQuality
```
    /**
     * H16图传接收机信号质量
     * 访问方式
     * LISTEN
     * 仅支持H16
     */
    val KeyH16SignalQuality:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canListen(true)
```

- ##### KeyH16RawSignalQuality
```
    /**
     * H16图传接收机信号质量(原始数据)
     * 访问方式
     * LISTEN
     * 仅支持H16
     */
    val KeyH16RawSignalQuality:KeyInfo<String> = KeyInfo.Builder<String>()
        .canListen(true)
```

- ##### KeyH30SignalQuality
```
    /**
     * H30图传接收机信号质量
     * 访问方式
     * LISTEN
     * 仅支持H30
     */
    val KeyH30SignalQuality:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canListen(true)
```

- ##### KeyH30UartBaudRate
```
    /**
     * H30图传接收机串口波特率
     * 访问方式
     * SET,GET
     * 仅支持H30
     */
    val KeyH30UartBaudRate:KeyInfo<H30UartBaudRate> = KeyInfo.Builder<H30UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

# PayloadManager
云卓相关配件通讯接口
```
//C10相机控制
val c10 = PayloadManager.getTCPPayload(PayloadType.C10, "192.168.144.108", 5000) as C10?
//内部已经实现重连机制，无需再实现
if (c10 != null) {
    c10.setCommListener(object : CommListener {
        override fun onConnectSuccess() {
             log("C10连接成功")
        }

        override fun onConnectFail(e: SkyException) {

        }

        override fun onDisconnect() {
            log("C10断开连接")
        }

        override fun onReadData(bytes: ByteArray) {

        }
    })
    
    //连接C10相机
    PayloadManager.connectPayload(c10)
}

//控制C10一键回中
c10.akey(AKey.MID)

//断开C10相机连接
PayloadManager.disconnectPayload(c10)
```

### 三体相机(串口版)控制
```
//获取三体相机(串口版)
//获取实例后需要调用连接方法才能控制
val threeBodyCamera = PayloadManager.getSerialPortPayload(PayloadType.THREE_BODY_CAMERA, "/dev/ttyHS0", 4000000) as ThreeBodyCamera?
 
//拍照
threeBodyCamera?.snapshot()

//开始录像
threeBodyCamera?.toggleReCord(true)

//结束录像
threeBodyCamera?.toggleReCord(false)

//切换LED
threeBodyCamera?.toggleLED()

//同步时间（要在收到帧数据后再调用才有效）
threeBodyCamera?.setTime(System.currentTimeMillis())
```

### 三体相机(网口版)控制
```
//获取三体相机(网口版)
//获取实例后需要调用连接方法才能控制
val threeBodyCamera2 = PayloadManager.getTCPPayload(PayloadType.THREE_BODY_CAMERA2, "192.168.144.108", 5001) as ThreeBodyCamera2?

//切换LED
threeBodyCamera2?.toggleLED()
```

### C10相机控制
```
//获取C10相机
//获取实例后需要调用连接方法才能控制
val c10 = PayloadManager.getTCPPayload(PayloadType.C10, "192.168.144.108", 5000) as C10?

//一键控制
//向下
c10?.akey(AKey.DOWN)
//回中
c10?.akey(AKey.MID)
//向上
c10?.akey(AKey.TOP)
        
//拍照
c10?.takePicture()

//开始录像
c10?.startRecordVideo()
        
//停止录像
c10?.stopRecordVideo()

//控制偏航，-127 ~ +127，负数向左，正数向右
c10?.controlYaw(50)
        
//控制俯仰，-127 ~ +127，负数向下，正数向上
c10?.controlPitch(-50)

```
