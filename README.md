# Demo 工程

下载或者克隆Git上的Android示例代码工程:https://gitee.com/skydroid/rcsdk-demo

# RCSDK架构体系概述
移动应用程序一般通过下图所示的几个主要类来访问RCSDK：
![image](https://gitee.com/skydroid/rcsdk-demo/raw/master/image/rcsdk.png)

- RCSDKManager： RCSDK工具包的入口类，管理RCSDK的初始化，反初始化，连接，以及监听硬件产品的连接事件。
- KeyManager： RCSDK使用了以Key为基础元素的参数设置和参数获取功能接口
- PipelineManager：与第三方设备数据传输的入口

# 空白项目集成 SDK
本指引介绍如何将 RCSDK-Demo 中的 RCSDK包移植到用户的空白项目中

```
本指引中使用的 Android Studio 版本为 Android Studio Chipmunk | 2021.2.1 Patch 1

SDK所需权限
<uses-permission android:name="android.permission.INTERNET" />
```
- ### 导入SDK AAR包

```
rcsdk-v0.5-alpha.aar
```

- ### 修改build.gradle(app) 文件
在 dependencies 项里添加SDK包
```
    implementation files("libs/rcsdk-v0.5-alpha.aar")
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

# KeyManager
遥控器参数设置、获取功能接口

- ### SET
```
//设置遥控器控制模式
KeyManager.set(RemoteControllerKey.KeyControlMode, ControlMode.JP) { e -> 
                    log("设置摇杆模式完成：${e}") 
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
                log("对频完成：${it}")
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
```

# Key
### RemoteControllerKey
- ##### KeyControlMode
```
    /**
     * 遥控器摇杆模式
     * 访问方式
     * SET,GET
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
     * 仅H12可用
     */
    val KeyH12ChannelSettings: KeyInfo<H12ChannelSettings> = KeyInfo.Builder<H12ChannelSettings>()
        .canSet(true)
        .canGet(true)
```

- ##### KeyH12ProChannelSettings
```
    /**
     * H12Pro通道
     * 访问方式
     * SET,GET
     * 仅H12Pro可用
     */
    val KeyH12ProChannelSettings: KeyInfo<ChannelSettings> = KeyInfo.Builder<ChannelSettings>()
        .canSet(true)
        .canGet(true)
```

- ##### KeyRequestPairing
```
    /**
     * 遥控器对频
     * 访问方式
     * ACTION
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
     */
    val KeyChannels: KeyInfo<IntArray> = KeyInfo.Builder<IntArray>()
        .canGet(true)
```

- ##### KeyCoachMode
```
    /**
     * 教练模式
     * 访问方式
     * SET GET
     * 不支持H12
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
     * GET SET
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
     * 仅H12Pro可用
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
     * 仅H12Pro可用
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
     * 仅H12Pro可用
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
     * 仅H12可用
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
     * H12可用
     */
    val KeyReceiverOptions: KeyInfo<ReceiverOptions> = KeyInfo.Builder<ReceiverOptions>()
        .canSet(true)
        .canGet(true)
```
