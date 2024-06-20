更新日志
```
v1.5.0
1.新增SDK工具类获取遥控器型号方法
com.skydroid.rcsdk.utils.RCSDKUtils

v1.4.9 
1.修复部分情况下拍照阻塞的bug
2.修复H20串口0、串口1相反问题
3.H30数传通讯默认使用UDP通讯

v1.4.7
1.修复部分H16数传接收多次问题
2.C10Pro OSD设置

v1.4.5
1.C12云台相机
2.C10Pro云台相机（建议使用C10Pro类代替C10ProCamera类与C10ProGimbal类，C10Pro相机固件需要v0.2.7及以上才支持C10Pro类，v0.2.7以前使用C10ProCamera类与C10ProGimbal类）
3.新增遥控器自定义按钮事件工具类（详细使用方法请查阅相关代码：CustomRCButtonsActivity）
4.修复Bug
    disconnectRC崩溃问题等

v1.3.4
1.新增Key：
    AirLinkKey.KeyH20Bandwidth(设置/获取H20遥控器带宽)

v1.3.3
1.EC10遥控器

v1.3.2
1.C20 LED开关

v1.3.1
1.优化TCP通讯重连机制
2.调整遥控器协议超时时间（100ms）

v1.3.0
1.新增Key：
    RemoteControllerKey.KeyModel(获取遥控器固件型号)
    RemoteControllerKey.KeyVersion(获取遥控器固件版本号)
2.优化CPU占用
3.优化UDP通讯过滤规则
4.优化混淆规则,防止与其他第三方库冲突

v1.2.1
1.修复H20信号强度错误问题
2.C10/C10Pro/C20云台控制新增角度控制方法
3.调整C10/C10Pro/C20云台速度控制参数(请参考文档C10/C10Pro/C20云台控制章节)
4.支持串口双轴云台相机控制

v1.1.0
支持H20遥控器

v0.9.7
支持C20云台相机

v0.8.5
1.修复同时多个TCP连接时阻塞问题
2.修复PipelineManager,PayloadManager连接不上时无法关闭问题

v0.8.3
1.新增云卓配件管理（C10、三体相机等）
2.修复UDPPipeline连接状态错误问题
3.新增Key:
    AirLinkKey.KeyH16RawSignalQuality(获取H16原始信号值DBM)

v0.7.1
支持H30遥控器

v0.6
支持H16遥控器

v0.1
发布第一版
```

# Demo 工程

下载或者克隆Git上的Android示例代码工程:https://gitee.com/skydroid/rcsdk-demo

<font color=blue>
使用注意事项:<br>
1.请确保其他应用(包含助手、地面站)处于停止关闭状态,避免端口占用导致数据链路失败;<br>
2.获取摇杆杆量值,无法主动上报,请求一次获取一次,推荐至少100ms读取一次;<br>
3.数传管道,未连接 接收机 时,数传管道 连接失败;<br>
</font>

<br>
<br>
如下是H12Pro+S1pro+C12Pro的测试效果图:

![image](https://gitee.com/skydroid/rcsdk-demo/raw/master/image/rcsdk_demo_1.png)
![image](https://gitee.com/skydroid/rcsdk-demo/raw/master/image/rcsdk_demo_2.png)


# RCSDK目前支持的遥控器产品
H12、H12Pro、H16/H16Pro、H30、H20

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
rcsdk-v1.5.0.aar
h16_airlink.aar //H16图传模块 minSdk 24
```

- ### 修改build.gradle(app) 文件
在 dependencies 项里添加SDK包
```
    implementation files("libs/rcsdk-v1.5.0.aar")
    implementation files('libs/h16_airlink.aar')//可选,H16遥控器图传模块,如果不是H16遥控器,无需导入,该模块minSdk为24
```

- ### 修改 AndroidManifest.xml 文件

参照 Demo 的 AndroidManifest.xml添加SDK 需要的最基础权限
```
<uses-permission android:name="android.permission.INTERNET" />
```

- ### 初始化RCSDK
在使用SDK各组件之前初始化context信息;  
初始化一次即可;  
推荐在Application中初始化;  
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
    
//监听H12Pro信号强度 (取值范围: 0-100%)
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
- ##### 遥控器摇杆模式
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

- ##### H12通道
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

- ##### 通道设置
```
    /**
     * 通道设置
     * 访问方式
     * SET,GET
     * 支持H12Pro/H16/H30/H20
     */
    val KeyChannelSettings: KeyInfo<ChannelSettings> = KeyInfo.Builder<ChannelSettings>()
        .canSet(true)
        .canGet(true)
```

- ##### 遥控器对频
```
    /**
     * 遥控器对频
     * 访问方式
     * ACTION
     * 支持ALL(暂不支持H20)
     */
    val KeyRequestPairing: KeyInfo<EmptyMsg> = KeyInfo.Builder<EmptyMsg>()
        .canAction(true)
```

- ##### 遥控器序列号
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

- ##### 遥控器通道值
```
    /**
     * 遥控器通道值
     * 访问方式
     * GET
     * 支持H12/H12Pro/H30/H20
     */
    val KeyChannels: KeyInfo<IntArray> = KeyInfo.Builder<IntArray>()
        .canGet(true)
```

- ##### H16遥控器通道值
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

- ##### 教练模式
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

- ##### 自定义数据
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

- ##### 遥控器型号
```
    /**
     * 遥控器型号
     * 访问方式
     * GET
     * 支持ALL
     */
    val KeyModel:KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)
```

- ##### 遥控器版本
```
    /**
     * 遥控器版本
     * 访问方式
     * GET
     * 支持ALL
     */
    val KeyVersion:KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)
```

### AirLinkKey

- ##### H12Pro图传接收机串口0波特率
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

- ##### H12Pro图传接收机RC通道失控保护值
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

- ##### 图传接收机信号质量
```
    /**
     * 图传接收机信号质量
     * 访问方式
     * LISTEN
     * 支持H12Pro/H16/H30/H20
     */
    val KeySignalQuality:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canListen(true)
```

- ##### H12图传接收机信号质量
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

- ##### H12接收机选项设置
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

- ##### H16图传接收机串口0波特率
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

- ##### H16图传接收机串口1波特率
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

- ##### H16图传接收机信号质量
从1.1.0版本起，推荐使用KeySignalQuality
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

- ##### H16图传接收机信号质量(原始数据)
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

- ##### H30图传接收机信号质量
从1.1.0版本起，推荐使用KeySignalQuality
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

- ##### H30图传接收机串口波特率
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

- ##### H20图传接收机串口0波特率
```
    /**
     * H20图传接收机串口0波特率
     * 访问方式
     * SET,GET
     * 仅支持H20
     */
    val KeyH20Uart0BaudRate:KeyInfo<H20UartBaudRate> = KeyInfo.Builder<H20UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### H20图传接收机串口1波特率
```
    /**
     * H20图传接收机串口1波特率
     * 访问方式
     * SET,GET
     * 仅支持H20
     */
    val KeyH20Uart1BaudRate:KeyInfo<H20UartBaudRate> = KeyInfo.Builder<H20UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### H20图传接收机串口1波特率
```
    /**
     * H20带宽设置
     * 访问方式
     * SET,GET
     * 仅支持H20
     * Bandwidth.ul：上行带宽
     * Bandwidth.dl：下行带宽
     */
    val KeyH20Bandwidth:KeyInfo<Bandwidth> = KeyInfo.Builder<Bandwidth>()
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

### 双轴云台相机(串口版)控制
```
 val dualAxisGimbalCamera = PayloadManager.getSerialPortPayload(PayloadType.DUAL_AXIS_GIMBAL_CAMERA,"/dev/ttyHS0",4000000) as DualAxisGimbalCamera

//一键控制
//向下
dualAxisGimbalCamera?.akey(AKey.DOWN)
//回中
dualAxisGimbalCamera?.akey(AKey.MID)
//向上
dualAxisGimbalCamera?.akey(AKey.TOP)

//控制俯仰
//向上
dualAxisGimbalCamera?.controlPitch(true)
//向下
dualAxisGimbalCamera?.controlPitch(false)

//同步时间（要在收到帧数据后再调用才有效）
dualAxisGimbalCamera?.setTime(System.currentTimeMillis())
```

### 三体相机(网口版)控制
```
//获取三体相机(网口版)
//获取实例后需要调用连接方法才能控制
val threeBodyCamera2 = PayloadManager.getTCPPayload(PayloadType.THREE_BODY_CAMERA2, "192.168.144.108", 5001) as ThreeBodyCamera2?

//切换LED
threeBodyCamera2?.toggleLED()
threeBodyCamera2?.toggleLED(boolean)

```

### C10云台相机控制
```
//获取C10云台相机
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

//速度控制偏航，-9.9 ~ +9.9，单位°/s 负数向左，正数向右
c10?.controlYaw(1f)
        
//速度控制俯仰，-9.9 ~ +9.9，单位°/s 负数向下，正数向上
c10?.controlPitch(-1f)

//控制偏航角度, -150.00 ~ +150.00，单位°
c10?.gotoYaw(30f)

//控制俯仰角度，-90.00 ~ +90.00，单位°
c10?.gotoPitch(-90f)
```

### C20相机控制
```
//获取C20相机
//获取实例后需要调用连接方法才能控制
val c20Camera = PayloadManager.getTCPPayload(PayloadType.C20_CAMERA, "192.168.144.108", 8100) as C20Camera?

//拍照
c20Camera?.takePicture()
//开始录像
c20Camera?.startRecordVideo()
//停止录像
c20Camera?.stopRecordVideo()

//变倍变焦
//开始变倍
c20Camera?.startZoomIn()
c20Camera?.startZoomOut()
//开始变焦
c20Camera?.startFucusFar()
c20Camera?.startFucusNear()
//停止变倍变焦
c20Camera?.stopZoomOrFucus()

//日夜模式
//设置
c20Camera?.setDayNightMode()
//查询
c20Camera?.getDayNightMode()

//翻转
//设置
c20Camera?.setFlip()
//查询
c20Camera?.setFlip()

更多接口详情查看
com.skydroid.rcsdk.common.payload.C20Camera

```

### C20云台控制
```
//获取C20云台
//获取实例后需要调用连接方法才能控制
val c20Gimbal = PayloadManager.getTCPPayload(PayloadType.C20_GIMBAL, "192.168.144.108", 5000) as C20Gimbal?

//一键控制
//向下
c20Gimbal?.akey(AKey.DOWN)
//回中
c20Gimbal?.akey(AKey.MID)
//向上
c20Gimbal?.akey(AKey.TOP)

//速度控制偏航，-9.9 ~ +9.9，单位°/s 负数向左，正数向右
c20Gimbal?.controlYaw(1f)
        
//速度控制俯仰，-9.9 ~ +9.9，单位°/s 负数向下，正数向上
c20Gimbal?.controlPitch(-1f)

//控制偏航角度, -150.00 ~ +150.00，单位°
c20Gimbal?.gotoYaw(30f)

//控制俯仰角度，-90.00 ~ +90.00，单位°
c20Gimbal?.gotoPitch(-90f)

//切换LED
c20Gimbal?.toggleLED()
c20Gimbal?.toggleLED(boolean)

```

### C10Pro相机控制（0.2.7以下固件）
```
//获取C10Pro相机
//获取实例后需要调用连接方法才能控制
val c10ProCamera = PayloadManager.getUDPPayload(PayloadType.C10PRO_CAMERA,12580,"192.168.144.108",12580) as C10ProCamera?

//拍照
c10ProCamera?.takePicture()
//开始录像
c10ProCamera?.startRecordVideo()
//停止录像
c10ProCamera?.stopRecordVideo()

//同步时间
c10ProCamera?.setTime()
//获取版本号
c10ProCamera?.getVersion()
//设置LED（针对新款三体相机有效）
c10ProCamera?.setLED()

更多接口详情查看
com.skydroid.rcsdk.common.payload.C10ProCamera

```

### C10Pro云台控制（0.2.7以下固件）
```
//获取C10Pro云台
//获取实例后需要调用连接方法才能控制
val c10ProGimbal = PayloadManager.getUDPPayload(PayloadType.C10PRO_GIMBAL, 5000, "192.168.144.108", 5000) as C10ProGimbal?

//一键控制
//向下
c10ProGimbal?.akey(AKey.DOWN)
//回中
c10ProGimbal?.akey(AKey.MID)
//向上
c10ProGimbal?.akey(AKey.TOP)

//速度控制偏航，-9.9 ~ +9.9，单位°/s 负数向左，正数向右
c10ProGimbal?.controlYaw(1f)
        
//速度控制俯仰，-9.9 ~ +9.9，单位°/s 负数向下，正数向上
c10ProGimbal?.controlPitch(-1f)

//控制偏航角度, -150.00 ~ +150.00，单位°
c10ProGimbal?.gotoYaw(30f)

//控制俯仰角度，-90.00 ~ +90.00，单位°
c10ProGimbal?.gotoPitch(-90f)

```

### C10Pro云台相机控制（0.2.7及以上固件）
```
//获取C10Pro云台相机
//获取实例后需要调用连接方法才能控制
c10p = PayloadManager.getUDPPayload(PayloadType.C10PRO,5000,"192.168.144.108",5000) as C12?

//一键控制
//向下
c10p?.akey(AKey.DOWN)
//回中
c10p?.akey(AKey.MID)
//向上
c10p?.akey(AKey.TOP)

//速度控制偏航，-9.9 ~ +9.9，单位°/s 负数向左，正数向右
c10p?.controlYaw(1f)
        
//速度控制俯仰，-9.9 ~ +9.9，单位°/s 负数向下，正数向上
c10p?.controlPitch(-1f)

//控制偏航角度, -150.00 ~ +150.00，单位°
c10p?.gotoYaw(30f)

//控制俯仰角度，-90.00 ~ +90.00，单位°
c10p?.gotoPitch(-90f)

//拍照
c10p?.takePicture(callBack:CompletionCallback?)

//开始录像
c10p?.startRecordVideo(callBack:CompletionCallback?)

//结束录像
c10p?.stopRecordVideo(callBack:CompletionCallback?)

//获取录像状态
c10p?.getRecordVideoState(callBack: CompletionCallbackWith<Boolean>)

//同步时间（需要在出图后设置才有效）
c10p?.setTime(time:Long,callBack:CompletionCallback?)

//设置osd显示/关闭
c10p?.setOSD(boolean: Boolean,callBack: CompletionCallback?)

//获取相机版本号
c10p?.getCameraVersion(callBack: CompletionCallbackWith<String>)

//LED开关（针对新款三体相机有效）
c10p?.setLed(onOrOff:Boolean,callBack: CompletionCallback?)
```

### C12云台相机控制
```
//获取C12云台相机
//获取实例后需要调用连接方法才能控制
c12 = PayloadManager.getUDPPayload(PayloadType.C12,5000,"192.168.144.108",5000) as C12?

//一键控制
//向下
c12?.akey(AKey.DOWN)
//回中
c12?.akey(AKey.MID)
//向上
c12?.akey(AKey.TOP)

//速度控制偏航，-9.9 ~ +9.9，单位°/s 负数向左，正数向右
c12?.controlYaw(1f)
        
//速度控制俯仰，-9.9 ~ +9.9，单位°/s 负数向下，正数向上
c12?.controlPitch(-1f)

//控制偏航角度, -150.00 ~ +150.00，单位°
c12?.gotoYaw(30f)

//控制俯仰角度，-90.00 ~ +90.00，单位°
c12?.gotoPitch(-90f)

//设置倍率 0-4  0:原图,1-4:变倍
c12?.setZoomRatios(value:Int,callBack: CompletionCallback?)

//设置伪彩
//    WHITE_HOT,白热
//    SEPIA,辉金
//    IRONBOW,铁红
//    RAINBOW,彩虹
//    NIGHT,微光
//    AURORA,极光
//    RED_HOT,红热
//    JUNGLE,从林
//    MEDICAL,医疗
//    BLACK_HOT,黑热
//    GLORY_HOT;金红
c12?.setThermalPalette(palette: ThermalPalette, callBack: CompletionCallback?)

//拍照
c12?.takePicture(callBack:CompletionCallback?)

//开始录像
c12?.startRecordVideo(callBack:CompletionCallback?)

//结束录像
c12?.stopRecordVideo(callBack:CompletionCallback?)

//获取录像状态
c12?.getRecordVideoState(callBack: CompletionCallbackWith<Boolean>)

//同步时间（需要在出图后设置才有效）
c12?.setTime(time:Long,callBack:CompletionCallback?)

//获取相机版本号
c12?.getCameraVersion(callBack: CompletionCallbackWith<String>)

```

# 工具类
#### 自定义遥控器按钮
com.skydroid.rcsdk.common.button.ButtonHelper
```
详细使用方法参考
CustomRCButtonsActivity
```

#### RCSDKUitls
com.skydroid.rcsdk.utils.RCSDKUitls
```
getDeviceType 获取遥控器型号
```
