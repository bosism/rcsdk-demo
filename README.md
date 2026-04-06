Update Log
```
v1.8.4
Bug fixes     
1. Channel setting parameters cannot be obtained from H16.

v1.8.3
Bug fixes     
1. G series occasionally crashes when retrieving version number.
2. Error in obtaining sensitivity on H12
3. Other bugs

v1.8.1
New features
1. G-series new upload mode interface
    AirLinkKey.KeyGLinkSpeedMode
        GLinkSpeedMode.UPLOAD: Upload mode
        GLinkSpeedMode.NORMAL: Disable upload mode.
Bug fixes     
1. When the G-series frequency pairing fails, the original frequency pairing state is not restored.
2. Troubleshooting G-series connection status errors

v1.8.0
New features
1. Supports G30
2. The `createXXXPayload` method has been added to `PayloadManager`.
3. Physical button control of pan/tilt function - New options: Down/Return to Center/Up, Down/Return to Center
4. Debug printing switch -- RCSDKManager.setDebug

Bug fixes
1. C20 Customizable Remote Control Button LED Control
2. Serial communication bug (calling RCSDKManager.disconnectRC() and then reconnecting the remote control will cause the serial connection to be directly disconnected).

optimization
1. Issue with repeated listening during multiple Pipeline/Payload connections -- Currently modified to only listen once.
2. Optimize gimbal control speed
3. Configure MAC pairing mode interface parameters
4. Issue with failure to reconnect after calling RCSDKManager.disconnectRC()
5. RCSDKManager.disconnectRC() closes connections in PayloadManager/PipelineManager -- Currently modified to not close connections in PayloadManager/PipelineManager.

v1.7.9
1. Bug fixes
    G-series signal strength acquisition occasionally freezes and fails to refresh data.
2. Added interface
    AirLink.KeySkySetAutoMCSDuration: Adaptive MCS (G series) over duration
  Universal gimbal camera control interface
    PayloadManager.getXXXPayload(PayloadType.COMMON,5000,"192.168.144.108",5000) as CommonPayload?
    
v1.7.8
1. The frequency pairing time for the G series has been changed to 30 seconds.
2. Added an interface for exiting frequency pairing mode on the G series.
    RemoteControllerKey.KeyRequestStopPair (Stops frequency pairing; valid for G series)
3. Disable debug logging
    
v1.7.7
1. Fixed the issue of no frequency response in some scenarios in version 1.7.6.
2. Add Key
    RemoteControllerKey.KeyButtonToneAndLongPress (Key tone and long press settings, supports G series remote control MCU firmware 1.3 and above. See Device Assistant 2.9.5 for reference.)
    RemoteControllerKey.KeyButtonSaveValue (Power-off save button value, supports G-series remote control MCU firmware 1.3 and above. See Device Assistant 2.9.5 for reference.)
    RemoteControllerKey.KeyRollerMode (Roller mode, supports G-series remote control MCU firmware 1.3 and above. See Device Assistant 2.9.5 for reference.)
    RemoteControllerKey.KeyMixedControlAndNoReturnCenter (Mixed control and throttle dead zone settings. Supports G-series remote control MCU firmware 1.3 and above; see Device Assistant 2.9.5 for reference.)
    RemoteControllerKey.KeyButtonLockMode (Self-locking mode, supports G-series remote control MCU firmware 1.3 and above. See Device Assistant 2.9.5 for reference.)

v1.7.6
1. Supports frequency pairing with G-series RF firmware 20250402
2. G-series interface stability
3. The onDisconnect callback is invoked when a FIN packet is received in a TCP connection.

v1.7.4
1. Bug fixes
    * Crashing issue in some cases on G-series remote controls

v1.7.3
1. Modify the G12/G20 data transmission pipeline to UDP (requires Android system version update 20250110 and later).
2. Bug fixes
    In some cases, custom remote control button utility classes may cause blocking issues.
    
v1.7.2
1. Bug fixes
    Occasionally, some remote controls (H12Pro, G12, G20) experience signal display errors.
2. Add Key
    915 module enabled (supports G20)
    RemoteControllerKey.KeyModule915Enable
    
v1.7.1
1. Bug fixes

v1.6.8
1. Fixed memory leak issue in G12 and G20 data transmission pipelines.

v1.6.6
1. G20 remote control
2. Add Key
    AirLinkKey.KeyRCSetReTxCount (Configures the number of retransmissions at the ground end, improving link reliability; supports G12 and G20)
    AirLinkKey.KeySkySetReTxCount (Configures the number of retransmissions at the sky end, improving link reliability; supports G12 and G20)
    AirLinkKey.KeySetAutoMCS (Adaptive MCS, improves uplink speed, supports G12 and G20)

v1.6.5
1. Bug fixes
    H20 remote control not receiving signal percentage issue

v1.6.4
1. G12 remote control
2. Bug fixes
    In some cases, KeyManager may block for 300ms.
    
v1.6.3
1. Bug fixes
    Get remote control channel commands
    
v1.6.2
1. Add Key:
    AirLinkKey.KeyRCVersion (Remote control wireless module version number, currently supports H20 and H30)
    AirLinkKey.KeySkyVersion (SkyLink wireless module version number, currently supports H20 and H30)
    AirLinkKey.KeySkyMCUVersion (Sky-side MUC version number, currently supports H30)
    AirLinkKey.KeyRawSignalQuality (Raw signal data, currently supports H16/H12Pro/H20/H30)
2. Obtaining the SDK version number
3. Fixed H20 baud rate setting error.
4. Optimization of underlying request logic*

v1.5.2
1. C12 continuously variable zoom
    C12::addZoomRatios
    C12::subtractZoomRatios
2. H20 remote control customizable buttons and pulsator control default to reverse.
3. Added interface for C12 PTZ control
    Simultaneously control pitch and yaw: C12::controlYawPitch (requires gimbal firmware 0.5 or higher)

v1.5.2
1. C12 continuously variable zoom
    C12::addZoomRatios
    C12::subtractZoomRatios
2. H20 remote control customizable buttons and pulsator control default to reverse.
3. Added interface for C12 PTZ control
    Simultaneously control pitch and yaw: C12::controlYawPitch (requires gimbal firmware 0.5 or higher)

v1.5.0
1. Added a method to retrieve the remote control model in the SDK utility class.
com.skydroid.rcsdk.utils.RCSDKUtils

v1.4.9
1. Fixed a bug that caused camera capture to be blocked in some cases.
2. Fixed the issue of H20 serial port 0 and serial port 1 being reversed.
3. H30 data transmission communication uses UDP communication by default.

v1.4.7
1. Fixed an issue where some H16 data transmissions received multiple times.
2. C10Pro OSD Settings

v1.4.5
1. C12 gimbal camera
2. C10Pro gimbal camera (It is recommended to use the C10Pro class instead of the C10ProCamera class and C10ProGimbal class. The C10Pro camera firmware needs to be v0.2.7 or above to support the C10Pro class. For versions earlier than v0.2.7, use the C10ProCamera class and C10ProGimbal class.)
3. Added a utility class for custom button events on the remote control (please refer to the relevant code for detailed usage: CustomRCButtonsActivity).
4. Bug fixes
    disconnectRC crash issues, etc.

v1.3.4
1. Add Key:
    AirLinkKey.KeyH20Bandwidth (Sets/Gets the H20 remote control bandwidth)

v1.3.3
1. EC10 remote control

v1.3.2
1. C20 LED Switch

v1.3.1
1. Optimize TCP communication reconnection mechanism
2. Adjust the remote control protocol timeout (100ms)

v1.3.0
1. Add Key:
    RemoteControllerKey.KeyModel(Get remote controller firmware model)
    RemoteControllerKey.KeyVersion(gets the remote controller firmware version number)
2. Optimize CPU usage
3. Optimize UDP communication filtering rules
4. Optimize obfuscation rules to prevent conflicts with other third-party libraries.

v1.2.1
1. Fixed H2O signal strength error issue.
2. Added angle control method for C10/C10Pro/C20 gimbal control
3. Adjust the speed control parameters of the C10/C10Pro/C20 PTZ (please refer to the C10/C10Pro/C20 PTZ Control section of the document).
4. Supports serial port dual-axis pan-tilt camera control

v1.1.0
Supports H20 remote control

v0.9.7
Supports C20 gimbal camera

v0.8.5
1. Fixed the blocking issue when multiple TCP connections are active simultaneously.
2. Fixed the issue where PipelineManager and PayloadManager could not be closed when the connection failed.

v0.8.3
1. Added Yunzhuo accessory management (C10, Three-Body Camera, etc.)
2. Fixed the UDPPipeline connection status error issue.
3. Add Key:
    AirLinkKey.KeyH16RawSignalQuality (gets the raw signal value DBM of H16)

v0.7.1
Supports H30 remote control

v0.6
Supports H16 remote control

v0.1
First version released
```

# Demo Project

Download or clone the Android sample code project on Git: https://gitee.com/skydroid/rcsdk-demo

<font color=blue>
Usage precautions:<br>
1. Please ensure that other applications (including the assistant and ground station) are stopped or closed to avoid port conflicts that could cause data link failures;
2. The joystick movement value cannot be actively reported; it must be retrieved only once per request. It is recommended to read the value at least once every 100ms.
3. Data transmission pipeline connection fails when the receiver is not connected;
</font>

<br>
<br>
The following are test results of H12Pro+S1pro+C12Pro:

![image](https://gitee.com/skydroid/rcsdk-demo/raw/master/image/rcsdk_demo_1.png)
![image](https://gitee.com/skydroid/rcsdk-demo/raw/master/image/rcsdk_demo_2.png)


# Remote control products currently supported by RCSDK
H12, H12Pro, H16/H16Pro, H30, H20, G12, G20, G30

# RCSDK Architecture Overview
Mobile applications typically access the RCSDK through the main classes shown in the diagram below:
![image](https://gitee.com/skydroid/rcsdk-demo/raw/master/image/rcsdk.png)

- RCSDKManager: The entry point class for the RCSDK toolkit, managing the initialization, deinitialization, and connection of the RCSDK, as well as listening for connection events of hardware products.
- KeyManager: The RCSDK uses a parameter setting and retrieval interface based on keys as the basic element.
- PipelineManager: The entry point for data transfer with third-party devices.
- PayloadManager: The entry point for controlling Yunzhuo-related accessories (C10, Tri-Body Camera, etc.).


# Integrating SDK into a Blank Project
This guide explains how to port the RCSDK package from RCSDK-Demo to your blank project.

```
This guide uses Android Studio Chipmunk | 2021.2.1 Patch 1.

SDK permissions required
<uses-permission android:name="android.permission.INTERNET" />

The Kotlin version is 1.6.10.

Confusion
-keep class com.skydroid.**{*;}
```
- ### Import SDK AAR package

```
rcsdk-v1.8.3.aar
h16_airlink.aar //H16 image transmission module minSdk 24
```

- ### Modify the build.gradle (app) file
Add the SDK package to the dependencies section.
```
    implementation files("libs/rcsdk-v1.8.3.aar")
    implementation files('libs/h16_airlink.aar') //Optional, H16 remote control image transmission module. If it's not an H16 remote control, no need to import. This module's minSdk is 24.
```

- ### Modify the AndroidManifest.xml file

Refer to the AndroidManifest.xml file in the demo to add the most basic permissions required by the SDK.
```
<uses-permission android:name="android.permission.INTERNET" />
```

- ### Initialize RCSDK
Initialize context information before using any SDK components;  
Initialization only needs to be done once;  
It is recommended to initialize it in the Application class;
```
RCSDKManager.initSDK(this,object :SDKManagerCallBack{
            override fun onRcConnectFail(e: SkyException?) {
                //Connection failed
            }

            override fun onRcConnected() {
                //Device connection
            }

            override fun onRcDisconnect() {
                //Device disconnected
            }
        })
```

- ### Connect remote control
```
RCSDKManager.connectToRC()
```

- ### Disconnect the remote control
Note: You need to disconnect when not in use, otherwise the port will be occupied indefinitely.
```
RCSDKManager.disconnectRC()
```

# KeyManager
Remote control parameter setting and acquisition function interface

- ### SET
```
//Set remote control mode
KeyManager.set(RemoteControllerKey.KeyControlMode, ControlMode.JP) {
                        e ->
                    if (e == null){
                        log("Successfully set joystick mode") //success
                    }else{
                        log("Failed to set joystick mode: ${e}") } //fail
                    }
```

- ### GET
```
Get remote control mode
KeyManager.get(RemoteControllerKey.KeyControlMode,object:
                    CompletionCallbackWith<ControlMode> {
                    override fun onSuccess(result: ControlMode?) {
                        Successfully obtained
                        log(result)
                    }

                    override fun onFailure(e: SkyException?) {
                        //Failed to retrieve
                        log(e)
                    }
                })
```

- ### ACTION
```
Remote control frequency pairing
KeyManager.action(RemoteControllerKey.KeyRequestPairing){
                e ->
                if (e == null){
                    log("Frequency pairing successful") //success
                }else{
                    log("Frequency pairing failed: ${e}") //fail
                }
            }
```

- ### LISTEN
```
var keySignalQualityListener = KeyListener<Int>{
        oldValue, newValue ->
        Log.e(TAG,"Signal strength:${oldValue},${newValue}")
    }
    
// Monitor H12Pro signal strength (value range: 0-100%)
KeyManager.listen(AirLinkKey.KeySignalQuality,keySignalQualityListener)

//Cancel monitoring H12Pro signal strength
KeyManager.cancelListen(keySignalQualityListener)
```

# PipelineManager
Communication interface with third-party devices

- ### Communication with third-party devices (e.g., flight controllers)
```
// Create a communication pipe
pipeline = PipelineManager.createPipeline()
pipeline?.let {
    //Set up listener
    it.onCommListener = object : CommListener{
        override fun onConnectSuccess() {
            log("Pipe connection successful")
        }

        override fun onConnectFail(e: SkyException?) {
            log("Pipe connection failed ${e}")
        }

        override fun onDisconnect() {
            log("Pipe disconnected")
        }

        override fun onReadData(data: ByteArray?) {
            //Data sent by a third-party device
        }

    }
    //Connect communication pipe
    PipelineManager.connectPipeline(it)
}

//Send data to a third-party device
pipeline?.let {
    it.writeData(bytes)
}

//Disconnect communication channel
pipeline?.let {
    PipelineManager.disconnectPipeline(it)
}
```

Custom method for creating communication pipes
```
// Create a communication pipeline based on the remote control type
PipelineManager.createPipeline(DeviceType.H12Pro)

// Create a custom serial communication pipe
PipelineManager.createSerialPipeline("/dev/ttyHS1",921600)

// Create a UDP communication pipe
//Parameter 1: Local port number; Parameter 2: Remote receiver IP address; Parameter 3: Remote receiver port number
PipelineManager.createUDPPipeline(14550,"192.168.144.10",14550)

// Create a TCP communication pipe
PipelineManager.createTCPPipeline("192.168.144.101",14550)

// Create a serial port 0 communication pipe
PipelineManager.createPipeline(Uart.UART0)

// Create a communication pipe for serial port 1
PipelineManager.createPipeline(Uart.UART1)

// Create a G12/G20 communication pipe (applicable to G12 and G20)
PipelineManager.createG12G20Pipeline()

```

# Key
### RemoteControllerKey
- ##### Remote control joystick mode
```
    /**
     * Remote control joystick mode
     Access Method
     * SET,GET
     * Supports ALL
     /
    val KeyControlMode: KeyInfo<ControlMode> = KeyInfo.Builder<ControlMode>()
        .canSet(true)
        .canGet(true)
```

- ##### H12 Joystick Channel Settings
```
    /**
     * H12 channel
     Access Method
     * SET,GET
     * Supports H12
     /
    val KeyH12ChannelSettings: KeyInfo<H12ChannelSettings> = KeyInfo.Builder<H12ChannelSettings>()
        .canSet(true)
        .canGet(true)
```

- ##### Joystick Channel Settings
```
    /**
     * Channel Settings
     Access Method
     * SET,GET
     * Supports H12Pro/H16/H30/H20/G12/G20/G30
     /
    val KeyChannelSettings: KeyInfo<ChannelSettings> = KeyInfo.Builder<ChannelSettings>()
        .canSet(true)
        .canGet(true)
```

- ##### Remote control frequency pairing
```
    /**
     * Remote control frequency pairing
     Access Method
     * ACTION
     * Supports ALL
     /
    val KeyRequestPairing: KeyInfo<EmptyMsg> = KeyInfo.Builder<EmptyMsg>()
        .canAction(true)
```

- ##### Remote control serial number
```
    /**
     * Remote control serial number
     Access Method
     * GET
     * Supports ALL
     /
    val KeySerialNumber: KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)
```

- ##### Remote control joystick sensitivity
```
    /**
     * Remote control joystick sensitivity
     Access Method
     * GET
     * Supports H12/H12Pro/H30/H20/G12/G20/G30
     /
    val KeyChannels: KeyInfo<IntArray> = KeyInfo.Builder<IntArray>()
        .canGet(true)
```

- ##### H16 Remote Control Joystick Sensitivity
```
    /**
     * H16 remote control joystick sensitivity
     Access Method
     * LISTEN
     * Supports H16
     /
    val KeyH16Channels: KeyInfo<IntArray> = KeyInfo.Builder<IntArray>()
        .canListen(true)
```

- ##### Coach Mode
```
    /**
     Coach Mode
     Access Method
     * SET,GET
     * Supports H12Pro/H16/H30
     /
    val KeyCoachMode: KeyInfo<CoachMode> = KeyInfo.Builder<CoachMode>()
        .canSet(true)
        .canGet(true)
```

- ##### Custom Data
```
    /**
     * Custom data 200 bytes
     Access Method
     * GET, SET
     * Supports ALL
     /
    val KeyCustomData: KeyInfo<ByteArray> = KeyInfo.Builder<ByteArray>()
        .canGet(true)
        .canSet(true)
```

- ##### Remote control model
```
    /**
     * Remote control model
     Access Method
     * GET
     * Supports ALL
     /
    val KeyModel:KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)
```

- ##### Remote Control Version
```
    /**
     * Remote control version
     Access Method
     * GET
     * Supports ALL
     /
    val KeyVersion:KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)
```

- ##### 915 Module Enable
```
    /**
     * Enable the 915 control module
     Access Method
     * SET,GET
     * Supports G20
     /
    val KeyModule915Enable:KeyInfo<Boolean> = KeyInfo.Builder<Boolean>()
        .canSet(true)
        .canGet(true)
```

### AirLinkKey

- ##### Receiver serial port 0 baud rate
```
    /**
     * Image transmission receiver serial port at 0 baud rate
     Access Method
     * SET,GET
     * Supports H12Pro/G12/G20/G30
     /
    val KeyUart0BaudRate:KeyInfo<UartBaudRate> = KeyInfo.Builder<UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### H12Pro Receiver RC Channel Uncontrolled Protection Value
```
    /**
     * Receiver RC channel runaway protection value
     Access Method
     * SET,GET
     * Supports H12Pro
     /
    val KeyLostSBUSValues:KeyInfo<LostSBUSValues> = KeyInfo.Builder<LostSBUSValues>()
        .canSet(true)
        .canGet(true)
```

- ##### Receiver Signal Quality (Raw Data)
```
    /**
     * Receiver signal quality (raw data)
     Access Method
     * LISTEN
     * Supports H16/H12Pro/H20/H30/G12/G20/G30
     /
    val KeyRawSignalQuality:KeyInfo<String> = KeyInfo.Builder<String>()
        .canListen(true)
    The data obtained is in the following format:
        G series
            {
                "dev_connect": false, // Connection status
	            "ap_ldpc_err": "0", // The proportion of LDPC blocks with decoding errors in the remote control interleaving block.
	            "ap_ldpc_num": "0", // Percentage of frames with decoding errors on the remote control
	            "ap_snr": "0", //Remote control -SNR
	            "ap_gain_a": "0", // Signal strength received by remote control's A-channel antenna
	            "ap_gain_b": "0", //Signal strength received by remote control's B-channel antenna
	            "ap_tx_mcs": "0", //Remote controller - Transmitting MCS
	            "ap_tx_power": "0", //Remote control - transmit power
	            "ap_tx_chan": "0", //Remote controller - transmission channel
	            "ap_tx_freq_khz": "0", //Remote control - transmit frequency band
	            "ap_lfs_2g_band_chan_snr": "0",
	            "ap_lfs_2g_band_gain_a": "0",
	            "ap_lfs_2g_band_gain_b": "0",
	            "ap_lfs_5g_band_chan_snr": "0",
	            "ap_lfs_5g_band_gain_a": "0",
	            "ap_lfs_5g_band_gain_b": "0",
	            "ap_main_loc": "0",
	            "ap_sync_num": "0",
	            "dev_ldpc_err": "0", // The proportion of LDPC blocks with decoding errors in the receiver-interleaved block.
	            "dev_ldpc_num": "0", // Percentage of frames with decoding errors at the receiver
	            "dev_snr": "0", // Receiver-SNR
	            "dev_gain_a": "0", // Receiver-A antenna signal strength
	            "dev_gain_b": "0", // Receiver signal strength received by antenna B
	            "dev_tx_mcs": "0", // Receiver-Transmitter MCS
	            "dev_tx_power": "0", // Receiver-transmit power
	            "dev_tx_chan": "0", // Receiver-transmit channel
	            "dev_tx_freq_khz": "0", // Receiver-transmit frequency band
	            "dev_lfs_2g_band_chan_snr": "0",
	            "dev_lfs_2g_band_gain_a": "0",
	            "dev_lfs_2g_band_gain_b": "0",
	            "dev_lfs_5g_band_chan_snr": "0",
	            "dev_lfs_5g_band_gain_a": "0",
	            "dev_lfs_5g_band_gain_b": "0",
	            "dev_sync_num": "0",
	            "acs_chan": 0,
	            "work_chan": 0,
	            "signal": 0 // The percentage of signal quality used for reference, calculated based on the remote control's SNR (remote control SNR <= 0: signal quality is 0; remote control SNR >= 16: signal quality is 100).
            }
```

- ##### Receiver signal quality
```
    /**
     * Receiver signal quality
     Access Method
     * LISTEN
     * Supports H12Pro/H16/H30/H20/G12/G20
     /
    val KeySignalQuality:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canListen(true)
```

- ##### H12 Receiver Signal Quality
```
    /**
     * H12 receiver signal quality
     Access Method
     * GET
     * H12 only
     /
    val KeyH12SignalQuality:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canGet(true)
```

- ##### H12 Receiver Options Settings
```
    /**
     * Receiver option settings
     Access Method
     * SET,GET
     * H12 only
     /
    val KeyReceiverOptions: KeyInfo<ReceiverOptions> = KeyInfo.Builder<ReceiverOptions>()
        .canSet(true)
        .canGet(true)
```

- ##### H16 receiver serial port 0 baud rate
```
    /**
     * H16 receiver serial port at 0 baud rate
     Access Method
     * SET,GET
     * H16 only
     /
    val KeyH16Uart0BaudRate:KeyInfo<UartBaudRate> = KeyInfo.Builder<UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### H16 receiver serial port 1 baud rate
```
    /**
     * H16 receiver serial port 1 baud rate
     Access Method
     * SET,GET
     * H16 only
     /
    val KeyH16Uart1BaudRate:KeyInfo<UartBaudRate> = KeyInfo.Builder<UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### H16 Receiver Signal Quality
Starting with version 1.1.0, KeySignalQuality is recommended.
```
    /**
     * H16 receiver signal quality
     Access Method
     * LISTEN
     * H16 only
     /
    val KeyH16SignalQuality:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canListen(true)
```

- ##### H16 Receiver Signal Quality (Raw Data)
```
    /**
     * H16 receiver signal quality (raw data)
     Access Method
     * LISTEN
     * H16 only
     /
    val KeyH16RawSignalQuality:KeyInfo<String> = KeyInfo.Builder<String>()
        .canListen(true)
```

- ##### H30 Receiver Signal Quality
Starting with version 1.1.0, KeySignalQuality is recommended.
```
    /**
     * H30 receiver signal quality
     Access Method
     * LISTEN
     * Only H30 is supported
     /
    val KeyH30SignalQuality:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canListen(true)
```

- ##### H30 Receiver Serial Port Baud Rate
```
    /**
     * H30 receiver serial port baud rate
     Access Method
     * SET,GET
     * Only H30 is supported
     /
    val KeyH30UartBaudRate:KeyInfo<H30UartBaudRate> = KeyInfo.Builder<H30UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### H20 receiver serial port 0 baud rate
```
    /**
     * H20 receiver serial port at 0 baud rate
     Access Method
     * SET,GET
     * H2O only
     /
    val KeyH20Uart0BaudRate:KeyInfo<H20UartBaudRate> = KeyInfo.Builder<H20UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### H20 receiver serial port 1 baud rate
```
    /**
     * H20 receiver serial port 1 baud rate
     Access Method
     * SET,GET
     * H2O only
     /
    val KeyH20Uart1BaudRate:KeyInfo<H20UartBaudRate> = KeyInfo.Builder<H20UartBaudRate>()
        .canSet(true)
        .canGet(true)
```

- ##### H20 receiver serial port 1 baud rate
```
    /**
     * H2O bandwidth settings
     Access Method
     * SET,GET
     * H2O only
     * Bandwidth.ul: Uplink bandwidth
     * Bandwidth.dl: Downlink bandwidth
     /
    val KeyH20Bandwidth:KeyInfo<Bandwidth> = KeyInfo.Builder<Bandwidth>()
        .canSet(true)
        .canGet(true)
```

- ##### Remote Control Wireless Module Version
```
    /**
     * Remote control wireless module version
     Access Method
     * GET
     * Supports G12/G20/G30/H30/H20
     /
    val KeyRCVersion:KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)
```

- ##### Sky-end Wireless Module Version
```
    /**
     * Sky-end wireless module version
     Access Method
     * GET
     * Supports G12/G20/G30/H30/H20
     /
    val KeySkyVersion:KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)
```

- ##### SkyEnd MCU Version
```
    /**
     * Sky-end MCU version
     Access Method
     * GET
     * Supports H30
     /
    val KeySkyMCUVersion:KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)
```

- ##### Number of retransmissions at the ground end (G series)
```
    /**
     * The ground terminal sets the number of retransmissions, which defaults to 6. This setting is lost after a restart (to restore the default of 6 retransmissions).
     * Setting range: 0-500 0: Indicates retransmission to the correct pair, ensuring link reliability.
     Access Method
     * SET
     * Supports G12/G20/G30
     /
    val KeyRCSetReTxCount:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canSet(true)
```


- ##### Number of retransmissions from the Sky terminal (G series)
```
    /**
     * The retransmission count is set on the Sky platform, defaulting to 6 times. This setting is lost after a restart (to restore the default of 6 times).
     * Setting range: 0-500 0: Indicates retransmission to the correct pair, ensuring link reliability.
     Access Method
     * SET
     * Supports G12/G20/G30
     /
    val KeySkySetReTxCount:KeyInfo<Int> = KeyInfo.Builder<Int>()
        .canSet(true)
```

- ##### Adaptive MCS (G Series)
```
    /**
     *Note* This interface should not be used in conjunction with the KeySkySetAutoMCSDuration interface.
     * Adaptive MCS
     * Enabling this feature can improve upload speed; the effect will be lost after a restart.
     * Applicable to file uploads. Enable before uploading and disable after uploading.
     Access Method
     * SET
     * Supports G12/G20/G30
     /
    val KeySetAutoMCS:KeyInfo<Boolean> = KeyInfo.Builder<Boolean>()
        .canSet(true)
```

- ##### Duration-based Adaptive MCS (G Series)
```
    /**
     *Note* This interface should not be used in conjunction with the KeySetAutoMCS interface.
     * Adaptive MCS over Duration
     * When activated, it can increase upload speed; effective for the duration of the activation.
     * Applicable to file uploads. Enable before uploading and disable after uploading.
     Access Method
     * SET
     * Supports G12/G20/G30
     * Instructions for use:
     * When the duration is set to 6 seconds, the remote control will enter adaptive MCS for 6 seconds. If the duration is set to 6 seconds again at the 4th second, the receiver will reset the timer.
     * File uploads can be scheduled at intervals, and will stop once the upload is complete. This interface prevents accidental program crashes while the remote control is still in adaptive MCS mode.
     /
    val KeySkySetAutoMCSDuration:KeyInfo<SetAutoAndDuration> = KeyInfo.Builder<SetAutoAndDuration>()
        .canSet(true)
        
```

- ##### MAC Address (G Series)
```
    /**
     MAC address
     Access Method
     * GET
     * Supports G12/G20/G30
     /
    val KeyMAC:KeyInfo<String> = KeyInfo.Builder<String>()
        .canGet(true)   
```

- ##### Radio Frequency Switches (G Series)
```
    /**
     * Configure RF switch
     Access Method
     * SET,GET
     * Supports G12/G20/G30
     /
    val KeyRCRFEnable:KeyInfo<Boolean> = KeyInfo.Builder<Boolean>()
        .canSet(true)
        .canGet(true)
```

- ##### Setting the MAC pairing method (G series)
```
    /**
     * Configure MAC frequency pairing method
     Access Method
     * SET
     * Supports G12/G20/G30
     /
    val KeyRequestPairingAtSetMac:KeyInfo<String> = KeyInfo.Builder<String>()
        .canSet(true)
```

- ##### Upload Mode (G Series)
```
    /**
     * G-series upload/download mode
     Access Method
     * GET/SET
     * Supports G12/G20/G30
     /
    val KeyGLinkSpeedMode:KeyInfo<GLinkSpeedMode> = KeyInfo.Builder<GLinkSpeedMode>()
        .canSet(true)
        .canGet(true)
        
    //Open upload mode
    KeyManager.set(AirLinkKey.KeyGLinkSpeedMode,GLinkSpeedMode.UPLOAD){
        if(it == null){
            //success
        }else{
            //fail
        }
    }
    //Disable upload mode
    KeyManager.set(AirLinkKey.KeyGLinkSpeedMode,GLinkSpeedMode.NORMAL){
        if(it == null){
            //success
        }else{
            //fail
        }
    }
```

# PayloadManager
Yunzhuo related accessories communication interface
```
//C10 Camera Control
val c10 = PayloadManager.getTCPPayload(PayloadType.C10, "192.168.144.108", 5000) as C10?
//The internal reconnection mechanism is already implemented, so it does not need to be implemented again.
if (c10 != null) {
    c10.setCommListener(object : CommListener {
        override fun onConnectSuccess() {
             log("C10 connection successful")
        }

        override fun onConnectFail(e: SkyException) {

        }

        override fun onDisconnect() {
            log("C10 Disconnection")
        }

        override fun onReadData(bytes: ByteArray) {

        }
    })
    
    //Connect C10 camera
    PayloadManager.connectPayload(c10)
}

//Control C10 to return to center with one key
c10.akey(AKey.MID)

//Disconnect C10 camera
PayloadManager.disconnectPayload(c10)
```

### Three-Body Camera (Serial Port Version) Control
```
//Get the Three-Body camera (serial port version)
//After obtaining the instance, you need to call the connection method to control it.
val threeBodyCamera = PayloadManager.getSerialPortPayload(PayloadType.THREE_BODY_CAMERA, "/dev/ttyHS0", 4000000) as ThreeBodyCamera?
 
//Photograph
threeBodyCamera?.snapshot()

//Start recording
threeBodyCamera?.toggleReCord(true)

//End recording
threeBodyCamera?.toggleReCord(false)

//Switch LED
threeBodyCamera?.toggleLED()

// Synchronize time (this is only effective if called after receiving frame data)
threeBodyCamera?.setTime(System.currentTimeMillis())
```

### Dual-axis pan-tilt camera (serial port version) control
```
 val dualAxisGimbalCamera = PayloadManager.getSerialPortPayload(PayloadType.DUAL_AXIS_GIMBAL_CAMERA,"/dev/ttyHS0",4000000) as DualAxisGimbalCamera

One-click control
//down
dualAxisGimbalCamera?.akey(AKey.DOWN)
// Return to center
dualAxisGimbalCamera?.akey(AKey.MID)
//up
dualAxisGimbalCamera?.akey(AKey.TOP)

//Control pitch
//up
dualAxisGimbalCamera?.controlPitch(true)
//down
dualAxisGimbalCamera?.controlPitch(false)

// Synchronize time (this is only effective if called after receiving frame data)
dualAxisGimbalCamera?.setTime(System.currentTimeMillis())
```

### Control of the old Three-Body Camera (Network Port Version)
```
//Get the Three-Body Camera (Network Port Version)
//After obtaining the instance, you need to call the connection method to control it.
val threeBodyCamera2 = PayloadManager.getTCPPayload(PayloadType.THREE_BODY_CAMERA2, "192.168.144.108", 5001) as ThreeBodyCamera2?

//Switch LED
threeBodyCamera2?.toggleLED()
threeBodyCamera2?.toggleLED(boolean)

```

### C10 Gimbal Camera Control
```
//Get C10 gimbal camera
//After obtaining the instance, you need to call the connection method to control it.
val c10 = PayloadManager.getTCPPayload(PayloadType.C10, "192.168.144.108", 5000) as C10?

One-click control
//down
c10?.akey(AKey.DOWN)
// Return to center
c10?.akey(AKey.MID)
//up
c10?.akey(AKey.TOP)
        
//Photograph
c10?.takePicture()

//Start recording
c10?.startRecordVideo()
        
//Stop recording
c10?.stopRecordVideo()

//Speed ​​control yaw, -63.5 ~ +63.5, unit °/s. Negative numbers indicate left, positive numbers indicate right.
c10?.controlYaw(1f)
        
//Speed ​​controls pitch, -63.5 ~ +63.5, unit: °/s. Negative numbers indicate downward movement, positive numbers indicate upward movement.
c10?.controlPitch(-1f)

//Control yaw angle, -150.00 ~ +150.00, unit °
c10?.gotoYaw(30f)

//Control pitch angle, -90.00 ~ +90.00, unit °
c10?.gotoPitch(-90f)
```

### C20 Camera Control
```
//Get C20 camera
//After obtaining the instance, you need to call the connection method to control it.
val c20Camera = PayloadManager.getTCPPayload(PayloadType.C20_CAMERA, "192.168.144.108", 8100) as C20Camera?

//Photograph
c20Camera?.takePicture()
//Start recording
c20Camera?.startRecordVideo()
//Stop recording
c20Camera?.stopRecordVideo()

// Zoom
//Start doubling
c20Camera?.startZoomIn()
c20Camera?.startZoomOut()
//Start zooming
c20Camera?.startFucusFar()
c20Camera?.startFucusNear()
//Stop zooming
c20Camera?.stopZoomOrFucus()

//Day/Night Mode
//set up
c20Camera?.setDayNightMode()
//Query
c20Camera?.getDayNightMode()

// Flip
//set up
c20Camera?.setFlip()
//Query
c20Camera?.setFlip()

For more interface details, please see
com.skydroid.rcsdk.common.payload.C20Camera

```

### C20 PTZ Control
```
//Get C20 gimbal
//After obtaining the instance, you need to call the connection method to control it.
val c20Gimbal = PayloadManager.getTCPPayload(PayloadType.C20_GIMBAL, "192.168.144.108", 5000) as C20Gimbal?

One-click control
//down
c20Gimbal?.akey(AKey.DOWN)
// Return to center
c20Gimbal?.akey(AKey.MID)
//up
c20Gimbal?.akey(AKey.TOP)

//Speed ​​control yaw, -63.5 ~ +63.5, unit °/s. Negative numbers indicate left, positive numbers indicate right.
c20Gimbal?.controlYaw(1f)
        
//Speed ​​controls pitch, -63.5 ~ +63.5, unit: °/s. Negative numbers indicate downward movement, positive numbers indicate upward movement.
c20Gimbal?.controlPitch(-1f)

//Control yaw angle, -150.00 ~ +150.00, unit °
c20Gimbal?.gotoYaw(30f)

//Control pitch angle, -90.00 ~ +90.00, unit °
c20Gimbal?.gotoPitch(-90f)

//Switch LED
c20Gimbal?.toggleLED()
c20Gimbal?.toggleLED(boolean)

```

### C10Pro Camera/New Three-Body Camera Control (Firmware below 0.2.7)
```
//Get C10 Pro camera
//After obtaining the instance, you need to call the connection method to control it.
val c10ProCamera = PayloadManager.getUDPPayload(PayloadType.C10PRO_CAMERA,12580,"192.168.144.108",12580) as C10ProCamera?

//Photograph
c10ProCamera?.takePicture()
Command example: "AT+AZ -p2\r\n"

//Start recording
c10ProCamera?.startRecordVideo()
Command example: "AT+AZ -p0\r\n"

//Stop recording
c10ProCamera?.stopRecordVideo()
Command example: "AT+AZ -p1\r\n"

//Synchronize time
c10ProCamera?.setTime()

//Get version number
c10ProCamera?.getVersion()
Command example: "AT+INFO\r\n"

//Set LED (effective for the new Three-Body camera)
c10ProCamera?.setLED()
Command examples: Turn on "AT+LED -e1\r\n"; Turn off "AT+LED -e0\r\n"

For more interface details, please see
com.skydroid.rcsdk.common.payload.C10ProCamera

```

### C10Pro Gimbal Control (Firmware below 0.2.7)
```
//Get C10Pro gimbal
//After obtaining the instance, you need to call the connection method to control it.
val c10ProGimbal = PayloadManager.getUDPPayload(PayloadType.C10PRO_GIMBAL, 5000, "192.168.144.108", 5000) as C10ProGimbal?

One-click control
//down
c10ProGimbal?.akey(AKey.DOWN)
// Return to center
c10ProGimbal?.akey(AKey.MID)
//up
c10ProGimbal?.akey(AKey.TOP)

//Speed ​​control yaw, -63.5 ~ +63.5, unit °/s. Negative numbers indicate left, positive numbers indicate right.
c10ProGimbal?.controlYaw(3f)
Command example: "#TPUG2wGSY1E75"
c10ProGimbal?.controlYaw(-3f)
Command example: "#TPUG2wGSYE276"

//Speed ​​controls pitch, -63.5 ~ +63.5, unit: °/s. Negative numbers indicate downward movement, positive numbers indicate upward movement.
c10ProGimbal?.controlPitch(3f)
Command example: "#TPUG2wGSP1E6C"
c10ProGimbal?.controlPitch(-3f)
Command example: "#TPUG2wGSPE26D"

//Control yaw angle, -150.00 ~ +150.00, unit °
c10ProGimbal?.gotoYaw(30f)

//Control pitch angle, -90.00 ~ +90.00, unit °
c10ProGimbal?.gotoPitch(-90f)

```

### C10Pro Gimbal Camera/New Three-Body Camera Control (Firmware 0.2.7 and above)
```
//Get C10Pro gimbal camera
//After obtaining the instance, you need to call the connection method to control it.
c10p = PayloadManager.getUDPPayload(PayloadType.C10PRO,5000,"192.168.144.108",5000) as C10Pro?

One-click control
//down
c10p?.akey(AKey.DOWN)
// Return to center
c10p?.akey(AKey.MID)
//up
c10p?.akey(AKey.TOP)

//Speed ​​control yaw, -63.5 ~ +63.5, unit °/s. Negative numbers indicate left, positive numbers indicate right.
c10p?.controlYaw(3f)
Command example: "#TPUG2wGSY1E75"
c10p?.controlYaw(-3f)
Command example: "#TPUG2wGSYE276"
  
//Speed ​​controls pitch, -63.5 ~ +63.5, unit: °/s. Negative numbers indicate downward movement, positive numbers indicate upward movement.
c10p?.controlPitch(3f)
Command example: "#TPUG2wGSP1E6C"
c10p?.controlPitch(-3f)
Command example: "#TPUG2wGSPE26D"

//Control yaw angle, -150.00 ~ +150.00, unit °
c10p?.gotoYaw(30f)

//Control pitch angle, -90.00 ~ +90.00, unit °
c10p?.gotoPitch(-90f)

//Photograph
c10p?.takePicture(callBack:CompletionCallback?)
Command example: "#TPUD2wCAP013E"

//Start recording
c10p?.startRecordVideo(callBack:CompletionCallback?)
Command example: "#TPUD2wREC0144"

//End recording
c10p?.stopRecordVideo(callBack:CompletionCallback?)
Command example: "#TPUD2wREC0043"

//Get recording status
c10p?.getRecordVideoState(callBack: CompletionCallbackWith<Boolean>)
Command example: "#TPUD2rREC003E"

//Synchronization time (must be set after outputting the drawing to take effect)
c10p?.setTime(time:Long,callBack:CompletionCallback?)

//Set OSD to show/hide
c10p?.setOSD(boolean: Boolean,callBack: CompletionCallback?)

//Get camera version number
c10p?.getCameraVersion(callBack: CompletionCallbackWith<String>)
Command example: "#TPUD2rVER0051"

//Set/Read video output parameters
c10p?.setVideoConfig()
c10p?.getVideoConfig()

//LED switch (effective for the new Three-Body camera)
c10p?.setLed(onOrOff:Boolean,callBack: CompletionCallback?)
```

### C12 Gimbal Camera Control
```
//Get C12 gimbal camera
//After obtaining the instance, you need to call the connection method to control it.
c12 = PayloadManager.getUDPPayload(PayloadType.C12,5000,"192.168.144.108",5000) as C12?

One-click control
//down
c12?.akey(AKey.DOWN)
// Return to center
c12?.akey(AKey.MID)
//up
c12?.akey(AKey.TOP)

//Speed ​​control yaw, -63.5 ~ +63.5, unit °/s. Negative numbers indicate left, positive numbers indicate right.
c12?.controlYaw(1f)
        
//Speed ​​controls pitch, -63.5 ~ +63.5, unit: °/s. Negative numbers indicate downward movement, positive numbers indicate upward movement.
c12?.controlPitch(-1f)

//Control yaw angle, -150.00 ~ +150.00, unit °
c12?.gotoYaw(30f)

//Control pitch angle, -90.00 ~ +90.00, unit °
c12?.gotoPitch(-90f)

//Set the magnification 0-4 0: Original image, 1-4: Zoom
c12?.setZoomRatios(value:Int,callBack: CompletionCallback?)

//Set false color
// WHITE_HOT, White Heat
// SEPIA, Huijin
// IRONBOW, Iron Red
// RAINBOW, Rainbow
// NIGHT, Glimmer
// AURORA, Aurora
// RED_HOT, Red Hot
// JUNGLE, from the forest
// MEDICAL, Medical
// BLACK_HOT, Black Hot
// GLORY_HOT;Golden Red
c12?.setThermalPalette(palette: ThermalPalette, callBack: CompletionCallback?)

//Photograph
c12?.takePicture(callBack:CompletionCallback?)

//Start recording
c12?.startRecordVideo(callBack:CompletionCallback?)

//End recording
c12?.stopRecordVideo(callBack:CompletionCallback?)

//Get recording status
c12?.getRecordVideoState(callBack: CompletionCallbackWith<Boolean>)

//Synchronization time (must be set after outputting the drawing to take effect)
c12?.setTime(time:Long,callBack:CompletionCallback?)

//Get camera version number
c12?.getCameraVersion(callBack: CompletionCallbackWith<String>)

```

### General-purpose gimbal camera control
```
A generic camera/gimbal includes all control protocols; developers need to determine whether they support these protocols themselves.
Supports: C10, C10Pro, C11, C12, C13, electronic gimbal (C01), three-body network camera, single/dual-axis network camera
Connection method: Determine based on camera type.

The following tests are performed using C12.
//After obtaining the instance, you need to call the connection method to control it.
val c12:CommonPayload? = PayloadManager.getUDPPayload(PayloadType.COMMON,5000,"192.168.144.108",5000) as CommonPayload?

One-click control
//down
c12?.akey(AKey.DOWN)
// Return to center
c12?.akey(AKey.MID)
//up
c12?.akey(AKey.TOP)

//Speed ​​control yaw, -63.5 ~ +63.5, unit °/s. Negative numbers indicate left, positive numbers indicate right.
c12?.controlYaw(1f)
        
//Speed ​​controls pitch, -63.5 ~ +63.5, unit: °/s. Negative numbers indicate downward movement, positive numbers indicate upward movement.
c12?.controlPitch(-1f)

//Control yaw angle, -150.00 ~ +150.00, unit °
c12?.gotoYaw(30f)

//Control pitch angle, -90.00 ~ +90.00, unit °
c12?.gotoPitch(-90f)

//Set false color
// WHITE_HOT, White Heat
// SEPIA, Huijin
// IRONBOW, Iron Red
// RAINBOW, Rainbow
// NIGHT, Glimmer
// AURORA, Aurora
// RED_HOT, Red Hot
// JUNGLE, from the forest
// MEDICAL, Medical
// BLACK_HOT, Black Hot
// GLORY_HOT;Golden Red
c12?.setThermalPalette(palette: ThermalPalette, callBack: CompletionCallback?)

//Photograph
c12?.takePicture(callBack:CompletionCallback?)

//Start recording
c12?.startRecordVideo(callBack:CompletionCallback?)

//End recording
c12?.stopRecordVideo(callBack:CompletionCallback?)

//Get recording status
c12?.getRecordVideoState(callBack: CompletionCallbackWith<Boolean>)

//Synchronization time (must be set after outputting the drawing to take effect)
c12?.setTime(time:Long,callBack:CompletionCallback?)

//Get camera version number
c12?.getCameraVersion(callBack: CompletionCallbackWith<String>)

```

# Utility Class
#### Customize remote control buttons (customize remote control channels/pivot wheel control/joystick control)
com.skydroid.rcsdk.common.button.ButtonHelper
```
For detailed usage instructions, please refer to [link/reference].
CustomRCButtonsActivity
```

#### RCSDKUitls
com.skydroid.rcsdk.utils.RCSDKUitls
```
getDeviceType retrieves the remote control model.
getVersion retrieves the SDK version number.
```