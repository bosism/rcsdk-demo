Release Notes
```
v1.8.4
Bug fixes     
1.H16Get不到ChannelSetParameters

v1.8.3
Bug fixes     
1.G系列GetVersion号偶尔崩溃问题
2.H12上Get感量报错问题
3.其他Bug

v1.8.1
New Features
1.G系列新Upload模式接口
    AirLinkKey.KeyGLinkSpeedMode
        GLinkSpeedMode.UPLOAD:Upload模式
        GLinkSpeedMode.NORMAL:CloseUpload模式
Bug fixes     
1.HandleG系列Pairingfailed时没有恢复原来的PairingStatus
2.HandleG系列ConnectStatusError问题

v1.8.0
New Features
1.SupportsG30
2.PayloadManagerNewcreateXXXPayload方法
3.物理Button控制Gimbal功能-New选项:Down/Center/Up、Down/Center
4.Debug打印开关--RCSDKManager.setDebug

Bug fixes
1.C20CustomRemote ControllerButtonLED控制
2.Serial Port通讯BUG（调用RCSDKManager.disconnectRC()，后重新ConnectRemote Controller会导致Serial PortConnect直接Disconnect）

Optimization
1.Pipeline/Payload多次Connect重复Listen问题--目前修改为只会Listen一次
2.OptimizationGimbal控制速度
3.SetMAC的Pairing方式接口Parameters修改
4.RCSDKManager.disconnectRC()调用后无法重新Connect问题
5.RCSDKManager.disconnectRC()ClosePayloadManager/PipelineManager中的Connect--目前修改为不ClosePayloadManager/PipelineManager中的Connect

v1.7.9
1.Bug fixes
    G系列GetSignalStrength偶尔Data卡死不刷新问题
2.New接口
    AirLink.KeySkySetAutoMCSDuration:Duration自适应MCS(G系列)
  通用Gimbal相机控制接口
    PayloadManager.getXXXPayload(PayloadType.COMMON,5000,"192.168.144.108",5000) as CommonPayload?
    
v1.7.8
1.G系列Pairing时间改为30s
2.NewG系列退出Pairing模式接口
    RemoteControllerKey.KeyRequestStopPair(StopPairing，G系列有效)
3.Close调试Log
    
v1.7.7
1.Fixed1.7.6部分场景下Pairing无响应问题
2.New Key
    RemoteControllerKey.KeyButtonToneAndLongPress(按键音和长按Set，SupportsG系列Remote ControllerMCU固件1.3及以上。可Refer toDevice助手2.9.5)
    RemoteControllerKey.KeyButtonSaveValue(掉电SaveButton值，SupportsG系列Remote ControllerMCU固件1.3及以上。可Refer toDevice助手2.9.5)
    RemoteControllerKey.KeyRollerMode(滚轮模式，SupportsG系列Remote ControllerMCU固件1.3及以上。可Refer toDevice助手2.9.5)
    RemoteControllerKey.KeyMixedControlAndNoReturnCenter(混控和油门死区Set。SupportsG系列Remote ControllerMCU固件1.3及以上，可Refer toDevice助手2.9.5)
    RemoteControllerKey.KeyButtonLockMode(自锁模式，SupportsG系列Remote ControllerMCU固件1.3及以上。可Refer toDevice助手2.9.5)

v1.7.6
1.SupportsG系列射频固件20250402Pairing
2.G系列接口稳定性
3.TCPConnect收到FIN时回调onDisconnect

v1.7.4
1.Bug fixes
    *部分情况下在G系列Remote Controller中崩溃问题

v1.7.3
1.G12/G20Telemetry pipeline修改为UDP（需要更新20250110及以后的Android系统Version）
2.Bug fixes
    部分情况下Custom遥控Button工具类可能引发阻塞问题
    
v1.7.2
1.Bug fixes
    部分Remote Controller(H12Pro、G12、G20)偶尔SignalDisplayError问题
2.New Key
    915模块使能(SupportsG20)
    RemoteControllerKey.KeyModule915Enable
    
v1.7.1
1.Bug fixes

v1.6.8
1.FixedG12、G20Telemetry pipeline内存泄漏问题

v1.6.6
1.G20Remote Controller
2.New Key
    AirLinkKey.KeyRCSetReTxCount(Configure地面端重传次数,提升链路可靠性,SupportsG12、G20)
    AirLinkKey.KeySkySetReTxCount(Configure天空端重传次数,提升链路可靠性,SupportsG12、G20)
    AirLinkKey.KeySetAutoMCS(自适应MCS,可提升上行速度,SupportsG12、G20)

v1.6.5
1.Bug fixes
    H20Remote ControllerGet不到Signal百分比问题

v1.6.4
1.G12Remote Controller
2.Bug fixes
    部分情况下KeyManager会阻塞300ms的问题
    
v1.6.3
1.Bug fixes
    GetRemote ControllerChannel指令
    
v1.6.2
1.New Key：
    AirLinkKey.KeyRCVersion(Remote Controller端无线模块Version号,目前SupportsH20,H30)
    AirLinkKey.KeySkyVersion(天空端端无线模块Version号,目前SupportsH20,H30)
    AirLinkKey.KeySkyMCUVersion(天空端MUCVersion号,目前SupportsH30)
    AirLinkKey.KeyRawSignalQuality(原始SignalData,目前SupportsH16/H12Pro/H20/H30)
2.SDKVersion号Get
3.FixedH20波特率SetError问题
4.底层请求逻辑Optimization*

v1.5.2
1.C12无级变倍
    C12::addZoomRatios
    C12::subtractZoomRatios
2.H20Remote ControllerCustomButton波轮控制Default反向
3.C12Gimbal控制New接口
    同时控制Pitch偏航：C12::controlYawPitch（需要Gimbal固件0.5及以上）

v1.5.2
1.C12无级变倍
    C12::addZoomRatios
    C12::subtractZoomRatios
2.H20Remote ControllerCustomButton波轮控制Default反向
3.C12Gimbal控制New接口
    同时控制Pitch偏航：C12::controlYawPitch（需要Gimbal固件0.5及以上）

v1.5.0
1.NewSDK工具类GetRemote Controller型号方法
com.skydroid.rcsdk.utils.RCSDKUtils

v1.4.9 
1.Fixed部分情况下Take picture阻塞的bug
2.FixedH20Serial Port0、Serial Port1相反问题
3.H30数传通讯DefaultUsageUDP通讯

v1.4.7
1.Fixed部分H16数传接收多次问题
2.C10Pro OSDSet

v1.4.5
1.C12Gimbal相机
2.C10ProGimbal相机（建议UsageC10Pro类代替C10ProCamera类与C10ProGimbal类，C10Pro相机固件需要v0.2.7及以上才SupportsC10Pro类，v0.2.7以前UsageC10ProCamera类与C10ProGimbal类）
3.NewRemote ControllerCustomButton事件工具类（详细Usage方法请查阅相关代码：CustomRCButtonsActivity）
4.Bug fixes
    disconnectRC崩溃问题等

v1.3.4
1.New Key：
    AirLinkKey.KeyH20Bandwidth(Set/GetH20Remote Controller带宽)

v1.3.3
1.EC10Remote Controller

v1.3.2
1.C20 LED开关

v1.3.1
1.OptimizationTCP通讯重连机制
2.调整Remote Controller协议超时时间（100ms）

v1.3.0
1.New Key：
    RemoteControllerKey.KeyModel(GetRemote Controller固件型号)
    RemoteControllerKey.KeyVersion(GetRemote Controller固件Version号)
2.OptimizationCPU占用
3.OptimizationUDP通讯过滤规则
4.Optimization混淆规则,防止与其他第三方库冲突

v1.2.1
1.FixedH20SignalStrengthError问题
2.C10/C10Pro/C20Gimbal控制New角度控制方法
3.调整C10/C10Pro/C20Gimbal速度控制Parameters(请Refer to文档C10/C10Pro/C20Gimbal控制章节)
4.SupportsSerial Port双轴Gimbal相机控制

v1.1.0
SupportsH20Remote Controller

v0.9.7
SupportsC20Gimbal相机

v0.8.5
1.Fixed同时多个TCPConnect时阻塞问题
2.FixedPipelineManager,PayloadManagerConnect不上时无法Close问题

v0.8.3
1.New云卓配件管理（C10、三体相机等）
2.FixedUDPPipelineConnectStatusError问题
3.New Key:
    AirLinkKey.KeyH16RawSignalQuality(GetH16原始Signal值DBM)

v0.7.1
SupportsH30Remote Controller

v0.6
SupportsH16Remote Controller

v0.1
发布第一版
```

# Demo 工程

Download或者克隆Git上的AndroidExample代码工程:https://gitee.com/skydroid/rcsdk-demo

<font color=blue>
Usage Notes:<br>
1.Please ensure其他应用(包含助手、地面站)处于StopCloseStatus,避免端口占用导致Data链路failed;<br>
2.GetStick杆量值,无法主动上报,请求一次Get一次,推荐至少100msRead一次;<br>
3.Telemetry pipeline,未Connect 接收机 时,Telemetry pipeline Connectfailed;<br>
</font>

<br>
<br>
Below isH12Pro+S1pro+C12Pro的测试效果图:

![image](https://gitee.com/skydroid/rcsdk-demo/raw/master/image/rcsdk_demo_1.png)
![image](https://gitee.com/skydroid/rcsdk-demo/raw/master/image/rcsdk_demo_2.png)


# RCSDK目前Supports的Remote Controller产品
H12、H12Pro、H16/H16Pro、H30、H20、G12、G20、G30

# RCSDK架构体系概述
移动应用程序一般通过下图所示的几个主要类来访问RCSDK：
![image](https://gitee.com/skydroid/rcsdk-demo/raw/master/image/rcsdk.png)

- RCSDKManager： RCSDK工具包的入口类，管理RCSDK的初始化，反初始化，Connect，以及Listen硬件产品的Connect事件。
- KeyManager： RCSDKUsage了以Key为基础元素的ParametersSet和ParametersGet功能接口
- PipelineManager：与第三方DeviceData传输的入口
- PayloadManager：控制云卓相关配件(C10、三体相机等)的入口


# blank project集成 SDK
This guide介绍如何将 RCSDK-Demo 中的 RCSDK包移植到用户的blank project中

```
This guide中Usage的 Android Studio Version为 Android Studio Chipmunk | 2021.2.1 Patch 1

SDK所需权限
<uses-permission android:name="android.permission.INTERNET" />

KotlinVersion为：1.6.10

混淆
-keep class com.skydroid.**{*;}
```
- ### 导入SDK AAR包

```
rcsdk-v1.8.3.aar
h16_airlink.aar //H16Telemetry模块 minSdk 24
```

- ### 修改build.gradle(app) 文件
在 dependencies 项里添加SDK包
```
    implementation files("libs/rcsdk-v1.8.3.aar")
    implementation files('libs/h16_airlink.aar')//可选,H16Remote ControllerTelemetry模块,如果不是H16Remote Controller,无需导入,该模块minSdk为24
```

- ### 修改 AndroidManifest.xml 文件

参照 Demo 的 AndroidManifest.xml添加SDK 需要的最基础权限
```
<uses-permission android:name="android.permission.INTERNET" />
```

- ### 初始化RCSDK
在UsageSDK各组件之前初始化context信息;  
初始化一次即可;  
推荐在Application中初始化;
```
RCSDKManager.initSDK(this,object :SDKManagerCallBack{
            override fun onRcConnectFail(e: SkyException?) {
                //Connectfailed
            }

            override fun onRcConnected() {
                //DeviceConnect
            }

            override fun onRcDisconnect() {
                //DeviceDisconnectConnect
            }
        })
```

- ### ConnectRemote Controller
```
RCSDKManager.connectToRC()
```

- ### DisconnectRemote Controller
注意：不Usage时需要DisconnectConnect，否则会一直占用端口
```
RCSDKManager.disconnectRC()
```

# KeyManager
Remote ControllerParametersSet、Get功能接口

- ### SET
```
//SetRemote Controller控制模式
KeyManager.set(RemoteControllerKey.KeyControlMode, ControlMode.JP) {
                        e ->
                    if (e == null){
                        log("SetStick模式Success") //success
                    }else{
                        log("SetStick模式failed：${e}") } //fail
                    }
```

- ### GET
```
GetRemote Controller控制模式
KeyManager.get(RemoteControllerKey.KeyControlMode,object :
                    CompletionCallbackWith<ControlMode> {
                    override fun onSuccess(result: ControlMode?) {
                        //GetSuccess
                        log(result)
                    }

                    override fun onFailure(e: SkyException?) {
                        //Getfailed
                        log(e)
                    }
                })
```

- ### ACTION
```
Remote ControllerPairing
KeyManager.action(RemoteControllerKey.KeyRequestPairing){
                e ->
                if (e == null){
                    log("PairingSuccess") //success
                }else{
                    log("Pairingfailed：${e}") //fail
                }
            }
```

- ### LISTEN
```
var keySignalQualityListener = KeyListener<Int>{
        oldValue, newValue ->
        Log.e(TAG,"SignalStrength:${oldValue},${newValue}")
    }
    
//ListenH12ProSignalStrength (取值Range: 0-100%)
KeyManager.listen(AirLinkKey.KeySignalQuality,keySignalQualityListener)

//取消ListenH12ProSignalStrength
KeyManager.cancelListen(keySignalQualityListener)
```

# PipelineManager
与第三方Device通讯接口

- ### 与第三方Device(例如飞控)通讯
```
//Create通讯管道
pipeline = PipelineManager.createPipeline()
pipeline?.let {
    //SetListen
    it.onCommListener = object : CommListener{
        override fun onConnectSuccess() {
            log("管道ConnectSuccess")
        }

        override fun onConnectFail(e: SkyException?) {
            log("管道Connectfailed${e}")
        }

        override fun onDisconnect() {
            log("管道DisconnectConnect")
        }

        override fun onReadData(data: ByteArray?) {
            //第三方DeviceSend的Data
        }

    }
    //Connect通讯管道
    PipelineManager.connectPipeline(it)
}

//SendData到第三方Device
pipeline?.let {
    it.writeData(bytes)
}

//Disconnect通讯管道
pipeline?.let {
    PipelineManager.disconnectPipeline(it)
}
```

CustomCreate通讯管道方法
```
//根据Remote Controller类型Create通讯管道
PipelineManager.createPipeline(DeviceType.H12Pro)

//CreateCustomSerial Port通讯管道
PipelineManager.createSerialPipeline("/dev/ttyHS1",921600)

//CreateUDP通讯管道
//Parameters1:本地端口号;Parameters2:远程接收端IP;Parameters3:远程接收端端口号
PipelineManager.createUDPPipeline(14550,"192.168.144.10",14550)

//CreateTCP通讯管道
PipelineManager.createTCPPipeline("192.168.144.101",14550)

//CreateSerial Port0通讯管道
PipelineManager.createPipeline(Uart.UART0)

//CreateSerial Port1通讯管道
PipelineManager.createPipeline(Uart.UART1)

//CreateG12G20通讯管道(适用于G12、G20)
PipelineManager.createG12G20Pipeline()

```

# Key
### RemoteControllerKey
- ##### Remote ControllerStick模式
```
    /**
     * Remote ControllerStick模式
     * Access
     * SET,GET
     * SupportsALL
     */
    val KeyControlMode: KeyInfo<ControlMode> = KeyInfo.Builder<ControlMode>()
        .canSet(true)
        .canGet(true)
```

- ##### H12StickChannelSet
```
    /**
     * H12Channel
     * Access
     * SET,GET
     * SupportsH12
     */
    val KeyH12ChannelSettings: KeyInfo<H12ChannelSettings> = KeyInfo.Builder<H12ChannelSettings>()
        .canSet(true)
        .canGet(true)
```

- ##### StickChannelSet
```
    /**
     * ChannelSet
     * Access
     * SET,GET
     * SupportsH12Pro/H16/H30/H20/G12/G20/G30
     */
    val KeyChannelSettings: KeyInfo<ChannelSettings> = KeyInfo.Builder<ChannelSettings>()
        .canSet(true)
        .canGet(true)
```

- ##### Remote ControllerPairing
```
    /**
     * Remote ControllerPairing
     * Access
     * ACTION
     * SupportsALL
     */
    val KeyRequestPairing: KeyInfo<EmptyMsg> = KeyInfo.Builder<EmptyMsg>()
        .canAction(true)
```

- ##### Remote Controller序列号
```
    /**
     * Remote Controller序列号
     * Access
     * GET
     * SupportsALL
     */
    val KeySerialNumber: KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)
```

- ##### Remote ControllerStick感量
```
    /**
     * Remote ControllerStick感量
     * Access
     * GET
     * SupportsH12/H12Pro/H30/H20/G12/G20/G30
     */
    val KeyChannels: KeyInfo<IntArray> = KeyInfo.Builder<IntArray>()
        .canGet(true)
```

- ##### H16Remote ControllerStick感量
```
    /**
     * H16Remote ControllerStick感量
     * Access
     * LISTEN
     * SupportsH16
     */
    val KeyH16Channels: KeyInfo<IntArray> = KeyInfo.Builder<IntArray>()
        .canListen(true)
```

- ##### 教练模式
```
    /**
     * 教练模式
     * Access
     * SET,GET
     * SupportsH12Pro/H16/H30
     */
    val KeyCoachMode: KeyInfo<CoachMode> = KeyInfo.Builder<CoachMode>()
        .canSet(true)
        .canGet(true)
```

- ##### CustomData
```
    /**
     * CustomData 200byte
     * Access
     * GET,SET
     * SupportsALL
     */
    val KeyCustomData: KeyInfo<ByteArray> = KeyInfo.Builder<ByteArray>()
        .canGet(true)
        .canSet(true)
```

- ##### Remote Controller型号
```
    /**
     * Remote Controller型号
     * Access
     * GET
     * SupportsALL
     */
    val KeyModel:KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)
```

- ##### Remote ControllerVersion
```
    /**
     * Remote ControllerVersion
     * Access
     * GET
     * SupportsALL
     */
    val KeyVersion:KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)
```

- ##### 915模块使能
```
    /**
     * 控制915模块使能
     * Access
     * SET,GET
     * SupportsG20
     */
    val KeyModule915Enable:KeyInfo<Boolean> = KeyInfo.Builder<Boolean>()
        .canSet(true)
        .canGet(true)
```

### AirLinkKey

- ##### 接收机Serial Port0波特率
```
    /**
     * Telemetry接收机Serial Port0波特率
     * Access
     * SET,GET
     * SupportsH12Pro/G12/G20/G30
     */
    val KeyUart0BaudRate:KeyInfo<UartBaudRate> = KeyInfo.Builder<UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### H12Pro接收机RCChannel失控保护值
```
    /**
     * 接收机RCChannel失控保护值
     * Access
     * SET,GET
     * SupportsH12Pro
     */
    val KeyLostSBUSValues:KeyInfo<LostSBUSValues> = KeyInfo.Builder<LostSBUSValues>()
        .canSet(true)
        .canGet(true)
```

- ##### 接收机Signal质量(原始Data)
```
    /**
     * 接收机Signal质量(原始Data)
     * Access
     * LISTEN
     * SupportsH16/H12Pro/H20/H30/G12/G20/G30
     */
    val KeyRawSignalQuality:KeyInfo<String> = KeyInfo.Builder<String>()
        .canListen(true)
    Get的Data格式如下：
        G系列
            {
                "dev_connect": false, //ConnectStatus
	            "ap_ldpc_err": "0", //Remote Controller-交织块中解码Error的LDPC块个数所占的比例
	            "ap_ldpc_num": "0", //Remote Controller-解码Error的帧个数比例
	            "ap_snr": "0", //Remote Controller-SNR
	            "ap_gain_a": "0", //Remote Controller-A路天线接收SignalStrength
	            "ap_gain_b": "0", //Remote Controller-B路天线接收SignalStrength
	            "ap_tx_mcs": "0", //Remote Controller-发射MCS
	            "ap_tx_power": "0", //Remote Controller-Send功率
	            "ap_tx_chan": "0", //Remote Controller-发射信道
	            "ap_tx_freq_khz": "0", //Remote Controller-Send频段
	            "ap_lfs_2g_band_chan_snr": "0",
	            "ap_lfs_2g_band_gain_a": "0",
	            "ap_lfs_2g_band_gain_b": "0",
	            "ap_lfs_5g_band_chan_snr": "0",
	            "ap_lfs_5g_band_gain_a": "0",
	            "ap_lfs_5g_band_gain_b": "0",
	            "ap_main_loc": "0",
	            "ap_sync_num": "0",
	            "dev_ldpc_err": "0", //接收机-交织块中解码Error的LDPC块个数所占的比例
	            "dev_ldpc_num": "0", //接收机-解码Error的帧个数比例
	            "dev_snr": "0", //接收机-SNR
	            "dev_gain_a": "0", //接收机-A路天线接收SignalStrength
	            "dev_gain_b": "0", //接收机-B路天线接收SignalStrength
	            "dev_tx_mcs": "0", //接收机-发射MCS
	            "dev_tx_power": "0", //接收机-Send功率
	            "dev_tx_chan": "0", //接收机-发射信道
	            "dev_tx_freq_khz": "0", //接收机-Send频段
	            "dev_lfs_2g_band_chan_snr": "0",
	            "dev_lfs_2g_band_gain_a": "0",
	            "dev_lfs_2g_band_gain_b": "0",
	            "dev_lfs_5g_band_chan_snr": "0",
	            "dev_lfs_5g_band_gain_a": "0",
	            "dev_lfs_5g_band_gain_b": "0",
	            "dev_sync_num": "0",
	            "acs_chan": 0,
	            "work_chan": 0,
	            "signal": 0 //根据Remote Controller-SNR计算出来的用于Refer to的Signal质量百分比（Remote ControllerSNR<=0:Signal质量为0；Remote ControllerSNR>=16:Signal质量为100）
            }
```

- ##### 接收机Signal质量
```
    /**
     * 接收机Signal质量
     * Access
     * LISTEN
     * SupportsH12Pro/H16/H30/H20/G12/G20
     */
    val KeySignalQuality:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canListen(true)
```

- ##### H12接收机Signal质量
```
    /**
     * H12接收机Signal质量
     * Access
     * GET
     * OnlySupportsH12
     */
    val KeyH12SignalQuality:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canGet(true)
```

- ##### H12接收机选项Set
```
    /**
     * 接收机选项Set
     * Access
     * SET,GET
     * OnlySupportsH12
     */
    val KeyReceiverOptions: KeyInfo<ReceiverOptions> = KeyInfo.Builder<ReceiverOptions>()
        .canSet(true)
        .canGet(true)
```

- ##### H16接收机Serial Port0波特率
```
    /**
     * H16接收机Serial Port0波特率
     * Access
     * SET,GET
     * OnlySupportsH16
     */
    val KeyH16Uart0BaudRate:KeyInfo<UartBaudRate> = KeyInfo.Builder<UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### H16接收机Serial Port1波特率
```
    /**
     * H16接收机Serial Port1波特率
     * Access
     * SET,GET
     * OnlySupportsH16
     */
    val KeyH16Uart1BaudRate:KeyInfo<UartBaudRate> = KeyInfo.Builder<UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### H16接收机Signal质量
从1.1.0Version起，推荐UsageKeySignalQuality
```
    /**
     * H16接收机Signal质量
     * Access
     * LISTEN
     * OnlySupportsH16
     */
    val KeyH16SignalQuality:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canListen(true)
```

- ##### H16接收机Signal质量(原始Data)
```
    /**
     * H16接收机Signal质量(原始Data)
     * Access
     * LISTEN
     * OnlySupportsH16
     */
    val KeyH16RawSignalQuality:KeyInfo<String> = KeyInfo.Builder<String>()
        .canListen(true)
```

- ##### H30接收机Signal质量
从1.1.0Version起，推荐UsageKeySignalQuality
```
    /**
     * H30接收机Signal质量
     * Access
     * LISTEN
     * OnlySupportsH30
     */
    val KeyH30SignalQuality:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canListen(true)
```

- ##### H30接收机Serial Port波特率
```
    /**
     * H30接收机Serial Port波特率
     * Access
     * SET,GET
     * OnlySupportsH30
     */
    val KeyH30UartBaudRate:KeyInfo<H30UartBaudRate> = KeyInfo.Builder<H30UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### H20接收机Serial Port0波特率
```
    /**
     * H20接收机Serial Port0波特率
     * Access
     * SET,GET
     * OnlySupportsH20
     */
    val KeyH20Uart0BaudRate:KeyInfo<H20UartBaudRate> = KeyInfo.Builder<H20UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### H20接收机Serial Port1波特率
```
    /**
     * H20接收机Serial Port1波特率
     * Access
     * SET,GET
     * OnlySupportsH20
     */
    val KeyH20Uart1BaudRate:KeyInfo<H20UartBaudRate> = KeyInfo.Builder<H20UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### H20接收机Serial Port1波特率
```
    /**
     * H20带宽Set
     * Access
     * SET,GET
     * OnlySupportsH20
     * Bandwidth.ul：上行带宽
     * Bandwidth.dl：下行带宽
     */
    val KeyH20Bandwidth:KeyInfo<Bandwidth> = KeyInfo.Builder<Bandwidth>()
        .canSet(true)
        .canGet(true)
```

- ##### Remote Controller无线模块Version
```
    /**
     * Remote Controller无线模块Version
     * Access
     * GET
     * SupportsG12/G20/G30/H30/H20
     */
    val KeyRCVersion:KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)
```

- ##### 天空端无线模块Version
```
    /**
     * 天空端无线模块Version
     * Access
     * GET
     * SupportsG12/G20/G30/H30/H20
     */
    val KeySkyVersion:KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)
```

- ##### 天空端MCUVersion
```
    /**
     * 天空端MCUVersion
     * Access
     * GET
     * SupportsH30
     */
    val KeySkyMCUVersion:KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)
```

- ##### 地面端重传次数(G系列)
```
    /**
     * 地面端Set重传次数,Default6次，重启后失效(恢复Default6次)
     * SetRange:0-500  0:表示重传到对,保证了链路的可靠性
     * Access
     * SET
     * SupportsG12/G20/G30
     */
    val KeyRCSetReTxCount:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canSet(true)
```


- ##### 天空端重传次数(G系列)
```
    /**
     * 天空端Set重传次数,Default6次，重启后失效(恢复Default6次)
     * SetRange:0-500  0:表示重传到对,保证了链路的可靠性
     * Access
     * SET
     * SupportsG12/G20/G30
     */
    val KeySkySetReTxCount:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canSet(true)
```

- ##### 自适应MCS(G系列)
```
    /**
     * 注意* 该接口不要和KeySkySetAutoMCSDuration接口混合用
     * 自适应MCS
     * 开启后可提提升上行速度,重启后失效
     * 适用于Upload文件,Upload前开启,Upload完成Close
     * Access
     * SET
     * SupportsG12/G20/G30
     */
    val KeySetAutoMCS:KeyInfo<Boolean> = KeyInfo.Builder<Boolean>()
        .canSet(true)
```

- ##### Duration自适应MCS(G系列)
```
    /**
     * 注意* 该接口不要和KeySetAutoMCS接口混合用
     * Duration自适应MCS
     * 开启后可提提升上行速度,Duration有效
     * 适用于Upload文件,Upload前开启,Upload完成Close
     * Access
     * SET
     * SupportsG12/G20/G30
     * UsageDescription：
     * 当SetDuraton为6s,Remote Controller将进入自适应MCS持续6s，如果在第4s时，再次SetDuraton为6s。接收机将重新计时。
     * 文件Upload过程中可定时Send，文件Upload完成后StopSend。该接口可防止程序不小心崩溃，Remote Controller还处于自适应MCS模式的情况。
     */
    val KeySkySetAutoMCSDuration:KeyInfo<SetAutoAndDuration> = KeyInfo.Builder<SetAutoAndDuration>()
        .canSet(true)
        
```

- ##### MAC地址(G系列)
```
    /**
     * MAC地址
     * Access
     * GET
     * SupportsG12/G20/G30
     */
    val KeyMAC:KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)   
```

- ##### 射频开关(G系列)
```
    /**
     * Set射频开关
     * Access
     * SET,GET
     * SupportsG12/G20/G30
     */
    val KeyRCRFEnable:KeyInfo<Boolean> = KeyInfo.Builder<Boolean>()
        .canSet(true)
        .canGet(true)
```

- ##### SetMAC的Pairing方式(G系列)
```
    /**
     * SetMAC的Pairing方式
     * Access
     * SET
     * SupportsG12/G20/G30
     */
    val KeyRequestPairingAtSetMac:KeyInfo<String> = KeyInfo.Builder<String>()
        .canSet(true)
```

- ##### Upload模式(G系列)
```
    /**
     * G系列Upload/Download模式
     * Access
     * GET/SET
     * SupportsG12/G20/G30
     */
    val KeyGLinkSpeedMode:KeyInfo<GLinkSpeedMode> = KeyInfo.Builder<GLinkSpeedMode>()
        .canSet(true)
        .canGet(true)
        
    //OpenUpload模式
    KeyManager.set(AirLinkKey.KeyGLinkSpeedMode,GLinkSpeedMode.UPLOAD){
        if(it == null){
            //Success
        }else{
            //failed
        }
    }
    //CloseUpload模式
    KeyManager.set(AirLinkKey.KeyGLinkSpeedMode,GLinkSpeedMode.NORMAL){
        if(it == null){
            //Success
        }else{
            //failed
        }
    }
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
             log("C10ConnectSuccess")
        }

        override fun onConnectFail(e: SkyException) {

        }

        override fun onDisconnect() {
            log("C10DisconnectConnect")
        }

        override fun onReadData(bytes: ByteArray) {

        }
    })
    
    //ConnectC10相机
    PayloadManager.connectPayload(c10)
}

//控制C10One-touchCenter
c10.akey(AKey.MID)

//DisconnectC10相机Connect
PayloadManager.disconnectPayload(c10)
```

### 三体相机(Serial Port版)控制
```
//Get三体相机(Serial Port版)
//Get实例后需要调用Connect方法才能控制
val threeBodyCamera = PayloadManager.getSerialPortPayload(PayloadType.THREE_BODY_CAMERA, "/dev/ttyHS0", 4000000) as ThreeBodyCamera?
 
//Take picture
threeBodyCamera?.snapshot()

//StartRecording
threeBodyCamera?.toggleReCord(true)

//结束Recording
threeBodyCamera?.toggleReCord(false)

//切换LED
threeBodyCamera?.toggleLED()

//同步时间（要在收到帧Data后再调用才有效）
threeBodyCamera?.setTime(System.currentTimeMillis())
```

### 双轴Gimbal相机(Serial Port版)控制
```
 val dualAxisGimbalCamera = PayloadManager.getSerialPortPayload(PayloadType.DUAL_AXIS_GIMBAL_CAMERA,"/dev/ttyHS0",4000000) as DualAxisGimbalCamera

//One-touch控制
//Down
dualAxisGimbalCamera?.akey(AKey.DOWN)
//Center
dualAxisGimbalCamera?.akey(AKey.MID)
//Up
dualAxisGimbalCamera?.akey(AKey.TOP)

//控制Pitch
//Up
dualAxisGimbalCamera?.controlPitch(true)
//Down
dualAxisGimbalCamera?.controlPitch(false)

//同步时间（要在收到帧Data后再调用才有效）
dualAxisGimbalCamera?.setTime(System.currentTimeMillis())
```

### 旧款三体相机(网口版)控制
```
//Get三体相机(网口版)
//Get实例后需要调用Connect方法才能控制
val threeBodyCamera2 = PayloadManager.getTCPPayload(PayloadType.THREE_BODY_CAMERA2, "192.168.144.108", 5001) as ThreeBodyCamera2?

//切换LED
threeBodyCamera2?.toggleLED()
threeBodyCamera2?.toggleLED(boolean)

```

### C10Gimbal相机控制
```
//GetC10Gimbal相机
//Get实例后需要调用Connect方法才能控制
val c10 = PayloadManager.getTCPPayload(PayloadType.C10, "192.168.144.108", 5000) as C10?

//One-touch控制
//Down
c10?.akey(AKey.DOWN)
//Center
c10?.akey(AKey.MID)
//Up
c10?.akey(AKey.TOP)
        
//Take picture
c10?.takePicture()

//StartRecording
c10?.startRecordVideo()
        
//StopRecording
c10?.stopRecordVideo()

//速度控制偏航，-63.5 ~ +63.5，单位°/s 负数Left，正数Right
c10?.controlYaw(1f)
        
//速度控制Pitch，-63.5 ~ +63.5，单位°/s 负数Down，正数Up
c10?.controlPitch(-1f)

//控制偏航角度, -150.00 ~ +150.00，单位°
c10?.gotoYaw(30f)

//控制Pitch角度，-90.00 ~ +90.00，单位°
c10?.gotoPitch(-90f)
```

### C20相机控制
```
//GetC20相机
//Get实例后需要调用Connect方法才能控制
val c20Camera = PayloadManager.getTCPPayload(PayloadType.C20_CAMERA, "192.168.144.108", 8100) as C20Camera?

//Take picture
c20Camera?.takePicture()
//StartRecording
c20Camera?.startRecordVideo()
//StopRecording
c20Camera?.stopRecordVideo()

//变倍变焦
//Start变倍
c20Camera?.startZoomIn()
c20Camera?.startZoomOut()
//Start变焦
c20Camera?.startFucusFar()
c20Camera?.startFucusNear()
//Stop变倍变焦
c20Camera?.stopZoomOrFucus()

//日夜模式
//Set
c20Camera?.setDayNightMode()
//查询
c20Camera?.getDayNightMode()

//翻转
//Set
c20Camera?.setFlip()
//查询
c20Camera?.setFlip()

See interface docs for
com.skydroid.rcsdk.common.payload.C20Camera

```

### C20Gimbal控制
```
//GetC20Gimbal
//Get实例后需要调用Connect方法才能控制
val c20Gimbal = PayloadManager.getTCPPayload(PayloadType.C20_GIMBAL, "192.168.144.108", 5000) as C20Gimbal?

//One-touch控制
//Down
c20Gimbal?.akey(AKey.DOWN)
//Center
c20Gimbal?.akey(AKey.MID)
//Up
c20Gimbal?.akey(AKey.TOP)

//速度控制偏航，-63.5 ~ +63.5，单位°/s 负数Left，正数Right
c20Gimbal?.controlYaw(1f)
        
//速度控制Pitch，-63.5 ~ +63.5，单位°/s 负数Down，正数Up
c20Gimbal?.controlPitch(-1f)

//控制偏航角度, -150.00 ~ +150.00，单位°
c20Gimbal?.gotoYaw(30f)

//控制Pitch角度，-90.00 ~ +90.00，单位°
c20Gimbal?.gotoPitch(-90f)

//切换LED
c20Gimbal?.toggleLED()
c20Gimbal?.toggleLED(boolean)

```

### C10Pro相机/新款三体相机控制（0.2.7以下固件）
```
//GetC10Pro相机
//Get实例后需要调用Connect方法才能控制
val c10ProCamera = PayloadManager.getUDPPayload(PayloadType.C10PRO_CAMERA,12580,"192.168.144.108",12580) as C10ProCamera?

//Take picture
c10ProCamera?.takePicture()
CommandExample:"AT+AZ -p2\r\n"

//StartRecording
c10ProCamera?.startRecordVideo()
CommandExample:"AT+AZ -p0\r\n"

//StopRecording
c10ProCamera?.stopRecordVideo()
CommandExample:"AT+AZ -p1\r\n"

//同步时间
c10ProCamera?.setTime()

//GetVersion号
c10ProCamera?.getVersion()
CommandExample:"AT+INFO\r\n"

//SetLED（针对新款三体相机有效）
c10ProCamera?.setLED()
CommandExample:开 "AT+LED -e1\r\n";关 "AT+LED -e0\r\n"

See interface docs for
com.skydroid.rcsdk.common.payload.C10ProCamera

```

### C10ProGimbal控制（0.2.7以下固件）
```
//GetC10ProGimbal
//Get实例后需要调用Connect方法才能控制
val c10ProGimbal = PayloadManager.getUDPPayload(PayloadType.C10PRO_GIMBAL, 5000, "192.168.144.108", 5000) as C10ProGimbal?

//One-touch控制
//Down
c10ProGimbal?.akey(AKey.DOWN)
//Center
c10ProGimbal?.akey(AKey.MID)
//Up
c10ProGimbal?.akey(AKey.TOP)

//速度控制偏航，-63.5 ~ +63.5，单位°/s 负数Left，正数Right
c10ProGimbal?.controlYaw(3f)
CommandExample:"#TPUG2wGSY1E75"
c10ProGimbal?.controlYaw(-3f)
CommandExample:"#TPUG2wGSYE276"

//速度控制Pitch，-63.5 ~ +63.5，单位°/s 负数Down，正数Up
c10ProGimbal?.controlPitch(3f)
CommandExample:"#TPUG2wGSP1E6C"
c10ProGimbal?.controlPitch(-3f)
CommandExample:"#TPUG2wGSPE26D"

//控制偏航角度, -150.00 ~ +150.00，单位°
c10ProGimbal?.gotoYaw(30f)

//控制Pitch角度，-90.00 ~ +90.00，单位°
c10ProGimbal?.gotoPitch(-90f)

```

### C10ProGimbal相机/新款三体相机控制（0.2.7及以上固件）
```
//GetC10ProGimbal相机
//Get实例后需要调用Connect方法才能控制
c10p = PayloadManager.getUDPPayload(PayloadType.C10PRO,5000,"192.168.144.108",5000) as C10Pro?

//One-touch控制
//Down
c10p?.akey(AKey.DOWN)
//Center
c10p?.akey(AKey.MID)
//Up
c10p?.akey(AKey.TOP)

//速度控制偏航，-63.5 ~ +63.5，单位°/s 负数Left，正数Right
c10p?.controlYaw(3f)
CommandExample:"#TPUG2wGSY1E75"
c10p?.controlYaw(-3f)
CommandExample:"#TPUG2wGSYE276"
  
//速度控制Pitch，-63.5 ~ +63.5，单位°/s 负数Down，正数Up
c10p?.controlPitch(3f)
CommandExample:"#TPUG2wGSP1E6C"
c10p?.controlPitch(-3f)
CommandExample:"#TPUG2wGSPE26D"

//控制偏航角度, -150.00 ~ +150.00，单位°
c10p?.gotoYaw(30f)

//控制Pitch角度，-90.00 ~ +90.00，单位°
c10p?.gotoPitch(-90f)

//Take picture
c10p?.takePicture(callBack:CompletionCallback?)
CommandExample:"#TPUD2wCAP013E"

//StartRecording
c10p?.startRecordVideo(callBack:CompletionCallback?)
CommandExample:"#TPUD2wREC0144"

//结束Recording
c10p?.stopRecordVideo(callBack:CompletionCallback?)
CommandExample:"#TPUD2wREC0043"

//GetRecordingStatus
c10p?.getRecordVideoState(callBack: CompletionCallbackWith<Boolean>)
CommandExample:"#TPUD2rREC003E"

//同步时间（需要在出图后Set才有效）
c10p?.setTime(time:Long,callBack:CompletionCallback?)

//SetosdDisplay/Close
c10p?.setOSD(boolean: Boolean,callBack: CompletionCallback?)

//Get相机Version号
c10p?.getCameraVersion(callBack: CompletionCallbackWith<String>)
CommandExample:"#TPUD2rVER0051"

//Set/Read视频输出Parameters
c10p?.setVideoConfig()
c10p?.getVideoConfig()

//LED开关（针对新款三体相机有效）
c10p?.setLed(onOrOff:Boolean,callBack: CompletionCallback?)
```

### C12Gimbal相机控制
```
//GetC12Gimbal相机
//Get实例后需要调用Connect方法才能控制
c12 = PayloadManager.getUDPPayload(PayloadType.C12,5000,"192.168.144.108",5000) as C12?

//One-touch控制
//Down
c12?.akey(AKey.DOWN)
//Center
c12?.akey(AKey.MID)
//Up
c12?.akey(AKey.TOP)

//速度控制偏航，-63.5 ~ +63.5，单位°/s 负数Left，正数Right
c12?.controlYaw(1f)
        
//速度控制Pitch，-63.5 ~ +63.5，单位°/s 负数Down，正数Up
c12?.controlPitch(-1f)

//控制偏航角度, -150.00 ~ +150.00，单位°
c12?.gotoYaw(30f)

//控制Pitch角度，-90.00 ~ +90.00，单位°
c12?.gotoPitch(-90f)

//Set倍率 0-4  0:原图,1-4:变倍
c12?.setZoomRatios(value:Int,callBack: CompletionCallback?)

//Set伪彩
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

//Take picture
c12?.takePicture(callBack:CompletionCallback?)

//StartRecording
c12?.startRecordVideo(callBack:CompletionCallback?)

//结束Recording
c12?.stopRecordVideo(callBack:CompletionCallback?)

//GetRecordingStatus
c12?.getRecordVideoState(callBack: CompletionCallbackWith<Boolean>)

//同步时间（需要在出图后Set才有效）
c12?.setTime(time:Long,callBack:CompletionCallback?)

//Get相机Version号
c12?.getCameraVersion(callBack: CompletionCallbackWith<String>)

```

### 通用Gimbal相机控制
```
通用的相机/Gimbal，包含了所有的控制协议，需要开发者自己判断是否Supports控制
Supports：C10，C10Pro，C11，C12，C13，电子Gimbal(C01)，三体网口相机，单双轴网口相机
Connect方式：根据相机类型自行判断

以下UsageC12进行测试
//Get实例后需要调用Connect方法才能控制
val c12:CommonPayload? = PayloadManager.getUDPPayload(PayloadType.COMMON,5000,"192.168.144.108",5000) as CommonPayload?

//One-touch控制
//Down
c12?.akey(AKey.DOWN)
//Center
c12?.akey(AKey.MID)
//Up
c12?.akey(AKey.TOP)

//速度控制偏航，-63.5 ~ +63.5，单位°/s 负数Left，正数Right
c12?.controlYaw(1f)
        
//速度控制Pitch，-63.5 ~ +63.5，单位°/s 负数Down，正数Up
c12?.controlPitch(-1f)

//控制偏航角度, -150.00 ~ +150.00，单位°
c12?.gotoYaw(30f)

//控制Pitch角度，-90.00 ~ +90.00，单位°
c12?.gotoPitch(-90f)

//Set伪彩
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

//Take picture
c12?.takePicture(callBack:CompletionCallback?)

//StartRecording
c12?.startRecordVideo(callBack:CompletionCallback?)

//结束Recording
c12?.stopRecordVideo(callBack:CompletionCallback?)

//GetRecordingStatus
c12?.getRecordVideoState(callBack: CompletionCallbackWith<Boolean>)

//同步时间（需要在出图后Set才有效）
c12?.setTime(time:Long,callBack:CompletionCallback?)

//Get相机Version号
c12?.getCameraVersion(callBack: CompletionCallbackWith<String>)

```

# 工具类
#### CustomRemote ControllerButton(Remote ControllerChannelCustom/波轮控制/Stick控制)
com.skydroid.rcsdk.common.button.ButtonHelper
```
详细Usage方法Refer to
CustomRCButtonsActivity
```

#### RCSDKUitls
com.skydroid.rcsdk.utils.RCSDKUitls
```
getDeviceType GetRemote Controller型号
getVersion GetSDKVersion号
```
