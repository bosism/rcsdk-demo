package com.skydroid.rcsdkdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.skydroid.rcsdk.*;
import com.skydroid.rcsdk.comm.CommListener;
import com.skydroid.rcsdk.common.Uart;
import com.skydroid.rcsdk.common.callback.*;
import com.skydroid.rcsdk.common.error.SkyException;
import com.skydroid.rcsdk.common.payload.*;
import com.skydroid.rcsdk.common.pipeline.Pipeline;
import com.skydroid.rcsdk.common.remotecontroller.ControlMode;
import com.skydroid.rcsdk.key.AirLinkKey;
import com.skydroid.rcsdk.key.RemoteControllerKey;
import com.skydroid.rcsdk.utils.RCSDKUtils;
import com.skydroid.rcsdkdemo.other.AppUtils;
import com.skydroid.rcsdkdemo.other.EnumInfoKey;
import com.skydroid.rcsdkdemo.other.ReceiveInfo;

import java.util.Arrays;

/**
 * @author skydroid
 * @date 2023/10/11 9:50
 * @email 1501020210@qq.com
 * @describe
 * <p>
 * Added C10Pro new legacy FW camera control and UI refresh; by ljb on 2024.06.13.
 */
public class HomeActivityForJava extends AppCompatActivity {
    public static final String TAG = "HomeActivityForJava";
    private ReceiveInfo mReceiveInfo = new ReceiveInfo();
    private final KeyListener<Integer> keySignalQualityListener = new KeyListener<Integer>() {
        @Override
        public void onValueChange(Integer oldValue, Integer newValue) {
            printInfo(EnumInfoKey.Signal,"Signal strength: " + newValue);
        }
    };

    private final KeyListener<int[]> keyH16ChannelsListener = new KeyListener<int[]>() {
        @Override
        public void onValueChange(int[] oldValue, int[] newValue) {
            printInfo(EnumInfoKey.H16Channels,Arrays.toString(newValue));
        }
    };

    private final MutableLiveData<String> infoLiveData = new MutableLiveData<String>();
    private TextView tvInfo = null;
    private EditText etData = null;

    private Pipeline pipeline = null;
    private C10Pro c10Pro = null;// Camera control for firmware 0.2.7+, gimbal control for all versions
    private C10ProCamera c10ProCamera = null;// Camera control for firmware below 0.2.7
    // TODO Notes:
    // TODO When using this, ensure other apps (assistant, ground station, etc.) are closed or stopped so ports are free.
    // TODO Channel values are not pushed; request each time at least every 100ms.
    // TODO Telemetry pipeline fails when receiver is not connected.
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        tvInfo = findViewById(R.id.tv_info);
        etData = findViewById(R.id.et_data);
        infoLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String str) {
                tvInfo.setText(str);
            }
        });
        // TODO Initialize SDK once only.
        RCSDKManager.INSTANCE.initSDK(this, new SDKManagerCallBack() {
            @Override
            public void onRcConnected() {
                // Create communication pipeline (reconnect enabled; call connect only once).
                // Telemetry pipeline fails when receiver is not connected;
                Pipeline pipeline = PipelineManager.INSTANCE.createPipeline(Uart.UART0);
                pipeline.setOnCommListener(getCommListener(0, "Telemetry pipeline"));
                //Connect communication pipeline
                PipelineManager.INSTANCE.connectPipeline(pipeline);
                HomeActivityForJava.this.pipeline = pipeline;
            }

            @Override
            public void onRcConnectFail(@Nullable SkyException e) {

            }

            @Override
            public void onRcDisconnect() {

            }
        });
        RCSDKManager.INSTANCE.setMainThreadCallBack(true); // callback on main thread
        //Connect to RC
        RCSDKManager.INSTANCE.connectToRC();

        // Legacy wired three-body camera
//        ThreeBodyCamera2 threeBodyCamera2 = (ThreeBodyCamera2)PayloadManager.INSTANCE.getTCPPayload(PayloadType.THREE_BODY_CAMERA2, "192.168.144.108", 5001);
        // Legacy serial three-body camera
//        ThreeBodyCamera threeBodyCamera = (ThreeBodyCamera)PayloadManager.INSTANCE.getSerialPortPayload(PayloadType.THREE_BODY_CAMERA, "/dev/ttyHS0", 4000000);
        // C20 camera
//        C20Camera c20Camera = (C20Camera)PayloadManager.INSTANCE.getTCPPayload(PayloadType.C20_CAMERA, "192.168.144.108", 8100);
        // C20 gimbal
//        C20Gimbal c20Gimbal = (C20Gimbal)PayloadManager.INSTANCE.getTCPPayload(PayloadType.C20_GIMBAL, "192.168.144.108", 5000);

        // C10Pro camera control (or new three-body camera Ethernet version)
        c10Pro = (C10Pro) PayloadManager.INSTANCE.getUDPPayload(PayloadType.C10PRO,5000,"192.168.144.108",5000);
        // Internal reconnect is already handled
        if (c10Pro != null){
            c10Pro.setCommListener(getCommListener(1, "C10Pro"));
            PayloadManager.INSTANCE.connectPayload(c10Pro);
        }
        c10ProCamera = (C10ProCamera) PayloadManager.INSTANCE.getUDPPayload(PayloadType.C10PRO_CAMERA,12580,"192.168.144.108",12580);
        // Internal reconnect is already handled
        if (c10ProCamera != null){
            c10ProCamera.setCommListener(getCommListener(2, "C10pCamera"));
            PayloadManager.INSTANCE.connectPayload(c10ProCamera);
        }
        initTestView();
        setTitle("RCSDK_Demo_V" + RCSDKUtils.getVersion() + " Java  Device:" + RCSDKUtils.getDeviceType());
    }

    private CommListener getCommListener(int type, String tag) {
        return new CommListener() {
            @Override
            public void onConnectSuccess() {
                log(tag + " connected");
            }

            @Override
            public void onConnectFail(SkyException e) {
                log(tag + " connect failed" + e);
            }

            @Override
            public void onDisconnect() {
                log(tag + " disconnected");
            }

            @Override
            public void onReadData(byte[] bytes) {
                if (type == 0) {
                    log(tag + " received length " + bytes.length + ", data " + new String(bytes));
                    // Telemetry pipeline
                    printInfo(EnumInfoKey.DataTransmission, "Telemetry: " + new String(bytes));
                }
            }
        };
    }

    private void initTestView() {
        findViewById(R.id.btn_pairing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyManager.INSTANCE.action(RemoteControllerKey.INSTANCE.getKeyRequestPairing(), new CompletionCallback() {
                    @Override
                    public void onResult(SkyException e) {
                        printInfo(EnumInfoKey.Other, AppUtils.getSkyExceptionInfo("Pairing", e, ""));
                    }
                });
            }
        });
        findViewById(R.id.btn_set_control_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyManager.INSTANCE.set(RemoteControllerKey.INSTANCE.getKeyControlMode(), ControlMode.USA, new CompletionCallback() {
                    @Override
                    public void onResult(SkyException e) {
                        printInfo(EnumInfoKey.SetControlMode, AppUtils.getSkyExceptionInfo("Set stick mode", e, ""));
                    }
                });
            }
        });
        findViewById(R.id.btn_get_control_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get RC hand mode
                KeyManager.INSTANCE.get(RemoteControllerKey.INSTANCE.getKeyControlMode(), new CompletionCallbackWith<ControlMode>() {
                    @Override
                    public void onSuccess(ControlMode controlMode) {
                        printInfo(EnumInfoKey.GetControlMode,"Get stick mode：" + controlMode.name());
                    }

                    @Override
                    public void onFailure(SkyException e) {
                printInfo(EnumInfoKey.GetControlMode,"Get stick mode failed: " + e);
                    }
                });
            }
        });
        findViewById(R.id.btn_get_channels).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get channel values
                switch (RCSDKManager.INSTANCE.getDeviceType()){
                    case H16:
                        //Avoid repeated listener registration
                        KeyManager.INSTANCE.cancelListen(keyH16ChannelsListener);
                        // H16/H16Pro channel values are LISTEN mode; listener will keep reporting until cancelled.
                        KeyManager.INSTANCE.listen(RemoteControllerKey.INSTANCE.getKeyH16Channels(), keyH16ChannelsListener);
                        break;
                    default:
                        // H12/H12Pro/H30 channel values are GET mode; must request each time.
                        KeyManager.INSTANCE.get(RemoteControllerKey.INSTANCE.getKeyChannels(), new CompletionCallbackWith<int[]>() {
                            @Override
                            public void onSuccess(int[] value) {
                                printInfo(EnumInfoKey.Channels,"Get channel values：" + Arrays.toString(value));
                            }

                            @Override
                            public void onFailure(SkyException e) {
                                printInfo(EnumInfoKey.Channels,"Get stick values failed: " + e);
                            }
                        });
                        break;
                }
            }
        });
        // Signal strength range: 0-100%
        findViewById(R.id.btn_get_signal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (RCSDKManager.INSTANCE.getDeviceType()){
                    case H12:
                        // H12 signal strength uses GET mode; request once each time.
                        KeyManager.INSTANCE.get(AirLinkKey.INSTANCE.getKeyH12SignalQuality(), new CompletionCallbackWith<Integer>() {
                            @Override
                            public void onSuccess(Integer integer) {
                                printInfo(EnumInfoKey.Signal,"H12 signal strength：" + integer);
                            }

                            @Override
                            public void onFailure(SkyException e) {
                                printInfo(EnumInfoKey.Signal,"H12 signal strength get failed: " + e);
                            }
                        });
                        break;

                    default:
                        //Avoid repeated listener registration
                        KeyManager.INSTANCE.cancelListen(keySignalQualityListener);
                        // Except H12, other RC signal strength values are LISTEN mode and stream after listener is set.
                        KeyManager.INSTANCE.listen(AirLinkKey.INSTANCE.getKeySignalQuality(),keySignalQualityListener);
                        break;
                }

            }
        });

        findViewById(R.id.btn_akey).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c10pCameraControl(false);
            }
        });
        findViewById(R.id.btn_akey_027).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c10pCameraControl(true);
            }
        });
        findViewById(R.id.btn_rc_buttons).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivityForJava.this,CustomRCButtonsActivity.class));
            }
        });
        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReceiveInfo.cleatInfo();
                tvInfo.setText("");
            }
        });
        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etData == null) {
                    return;
                }
                String temp = etData.getText().toString();
                if (TextUtils.isEmpty(temp)) {
                    Toast.makeText(getApplicationContext(), "Please enter text to send.", Toast.LENGTH_SHORT).show();
                    return;
                }
                HomeActivityForJava.this.pipeline.writeData(temp.getBytes());
                Toast.makeText(getApplicationContext(), "Send $temp", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.btn_led).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ledCameraControl(false);
            }
        });
        findViewById(R.id.btn_led_027).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ledCameraControl(true);
            }
        });
    }

    /**
     * New Ethernet C12 camera LED control
     */
    private void ledCameraControl(boolean isCameraVer027AndAbove){
        AppUtils.showC10pCameraControlDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        if (isCameraVer027AndAbove){
                            if (c10Pro != null) {
                                c10Pro.setLed(true, new CompletionCallback() {
                                    @Override
                                    public void onResult(SkyException e) {
                                        printInfo(EnumInfoKey.LED, AppUtils.getSkyExceptionInfo("LED on", e, "FW 0.2.7+"));
                                    }
                                });
                            }
                        }else {
                            if (c10ProCamera != null){
                                c10ProCamera.setLED(true, new CompletionCallback() {
                                    @Override
                                    public void onResult(SkyException e) {
                                        printInfo(EnumInfoKey.LED, AppUtils.getSkyExceptionInfo("LED on", e, "Legacy FW"));
                                    }
                                });
                            }
                        }
                        break;
                    case 1:
                        if (isCameraVer027AndAbove){
                            if (c10Pro != null) {
                                c10Pro.setLed(false, new CompletionCallback() {
                                    @Override
                                    public void onResult(SkyException e) {
                                        printInfo(EnumInfoKey.LED, AppUtils.getSkyExceptionInfo("LED off", e, "FW 0.2.7+"));
                                    }
                                });
                            }
                        }else {
                            if (c10ProCamera != null){
                                c10ProCamera.setLED(false, new CompletionCallback() {
                                    @Override
                                    public void onResult(SkyException e) {
                                        printInfo(EnumInfoKey.LED, AppUtils.getSkyExceptionInfo("LED off", e, "Legacy FW"));
                                    }
                                });
                            }
                        }
                        break;
                }
            }
        });
    }

    /**
     * Gimbal Control + Camera Control
     */
    private void c10pCameraControl(boolean isCameraVer027AndAbove) {
        AppUtils.showC10pCameraControlDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        if (c10ProCamera != null) {
                            c10ProCamera.getVersion(new CompletionCallbackWith<String>() {
                                @Override
                                public void onSuccess(String version) {
                                    printInfo(EnumInfoKey.CameraVersion, "Camera version: " + version);
                                }

                                @Override
                                public void onFailure(SkyException p0) {
                                    printInfo(EnumInfoKey.CameraVersion, "Camera version: " + p0);
                                }
                            });
                        }
                        break;

                    case 1:
                        if (c10Pro != null) {
                            c10Pro.akey(AKey.DOWN);
                        }
                        break;
                    case 2:
                        if (c10Pro != null) {
                            c10Pro.akey(AKey.MID);
                        }
                    case 3:
                        if (c10Pro != null) {
                            c10Pro.akey(AKey.TOP);
                        }
                        break;
                    case 4:
                        if (isCameraVer027AndAbove) {
                            if (c10Pro != null) {
                                c10Pro.takePicture(new CompletionCallback() {
                                    @Override
                                    public void onResult(SkyException e) {
                                        printInfo(EnumInfoKey.TakePicture, AppUtils.getSkyExceptionInfo("Take Picture", e, "FW 0.2.7+"));
                                    }
                                });
                            }

                        } else {
                            if (c10ProCamera != null) {
                                c10ProCamera.takePicture(new CompletionCallback() {
                                    @Override
                                    public void onResult(SkyException e) {
                                        printInfo(EnumInfoKey.TakePicture, AppUtils.getSkyExceptionInfo("Take Picture", e, "Legacy FW"));
                                    }
                                });
                            }
                        }
                        break;
                    case 5:
                        if (isCameraVer027AndAbove) {
                            if (c10Pro != null) {
                                c10Pro.startRecordVideo(new CompletionCallback() {
                                    @Override
                                    public void onResult(SkyException e) {
                                        printInfo(EnumInfoKey.RecordVideo, AppUtils.getSkyExceptionInfo("Start recording", e, "FW 0.2.7+"));
                                    }
                                });
                            }

                        } else {
                            if (c10ProCamera != null) {
                                c10ProCamera.startRecordVideo(new CompletionCallback() {
                                    @Override
                                    public void onResult(SkyException e) {
                                        printInfo(EnumInfoKey.RecordVideo, AppUtils.getSkyExceptionInfo("Start recording", e, "Legacy FW"));
                                    }
                                });
                            }
                        }
                        break;
                    case 6:
                        if (isCameraVer027AndAbove) {
                            if (c10Pro != null) {
                                c10Pro.stopRecordVideo(new CompletionCallback() {
                                    @Override
                                    public void onResult(SkyException e) {
                                        printInfo(EnumInfoKey.RecordVideo, AppUtils.getSkyExceptionInfo("Stop recording", e, "FW 0.2.7+"));
                                    }
                                });
                            }

                        } else {
                            if (c10ProCamera != null) {
                                c10ProCamera.stopRecordVideo(new CompletionCallback() {
                                    @Override
                                    public void onResult(SkyException e) {
                                        printInfo(EnumInfoKey.RecordVideo, AppUtils.getSkyExceptionInfo("Stop recording", e, "Legacy FW"));
                                    }
                                });
                            }
                        }
                        break;
                    case 7:
                        if (isCameraVer027AndAbove) {
                            if (c10Pro != null) {
                                c10Pro.setTime(System.currentTimeMillis(), new CompletionCallback() {
                                    @Override
                                    public void onResult(SkyException e) {
                                        printInfo(EnumInfoKey.CameraTime, AppUtils.getSkyExceptionInfo("Set time", e, "FW 0.2.7+"));
                                    }
                                });
                            }

                        } else {
                            if (c10ProCamera != null) {
                                c10ProCamera.setTime(System.currentTimeMillis(), new CompletionCallback() {
                                    @Override
                                    public void onResult(SkyException e) {
                                        printInfo(EnumInfoKey.CameraTime, AppUtils.getSkyExceptionInfo("Set time", e, "Legacy FW"));
                                    }
                                });
                            }
                        }
                        break;
                    case 8:// Yaw command, right, speed 30
                        if (c10Pro != null) {
                            //c10Pro.controlYaw(3f);// Method 1: API
                            c10Pro.writeData("#TPUG2WGSY1E75".getBytes());// Method 2: protocol command
                        }
                        break;
                    case 9:// Yaw command, left, speed -30
                        if (c10Pro != null) {
                            //c10Pro.controlYaw(-3f);// Method 1: API
                            c10Pro.writeData("#TPUG2wGSYE276".getBytes());// Method 2: protocol command
                        }
                        break;
                    case 10:// Pitch command, up, speed 30
                        if (c10Pro != null) {
                            //c10Pro.controlPitch(3f);// Method 1: API
                            c10Pro.writeData("#TPUG2WGSP1E6C".getBytes());// Method 2: protocol command
                        }
                        break;
                    case 11:// Pitch command, down, speed -30
                        if (c10Pro != null) {
                            //c10Pro.controlPitch(-3f);// Method 1: API
                            c10Pro.writeData("#TPUG2WGSPE26D".getBytes());// Method 2: protocol command
                        }
                        break;
                }
            }
        });
    }

    private void printInfo(EnumInfoKey key, Object obj){
        if (obj == null){
            return;
        }
        String sb = mReceiveInfo.updateInfo(key, obj);
        if (sb != null) {
            infoLiveData.postValue(sb);
        }
        log("printInfo -------key " + key + ",,,obj " + obj);
    }

    private void log(Object obj){
        if (obj == null){
            return;
        }
        Log.e(TAG,obj.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        infoLiveData.removeObservers(this);
        //Disconnect RC connection to release ports while app is running
        RCSDKManager.INSTANCE.disconnectRC();
        KeyManager.INSTANCE.cancelListen(keySignalQualityListener);
        Pipeline p = this.pipeline;
        if (p != null){
            PipelineManager.INSTANCE.disconnectPipeline(p);
        }
        C10Pro localC10p = this.c10Pro;
        if (localC10p != null){
            PayloadManager.INSTANCE.disconnectPayload(localC10p);
        }
    }
}
