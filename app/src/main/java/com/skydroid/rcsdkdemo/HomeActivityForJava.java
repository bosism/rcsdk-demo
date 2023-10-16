package com.skydroid.rcsdkdemo;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.skydroid.rcsdk.KeyManager;
import com.skydroid.rcsdk.PipelineManager;
import com.skydroid.rcsdk.RCSDKManager;
import com.skydroid.rcsdk.SDKManagerCallBack;
import com.skydroid.rcsdk.comm.CommListener;
import com.skydroid.rcsdk.common.Uart;
import com.skydroid.rcsdk.common.callback.CompletionCallback;
import com.skydroid.rcsdk.common.callback.CompletionCallbackWith;
import com.skydroid.rcsdk.common.callback.KeyListener;
import com.skydroid.rcsdk.common.error.SkyException;
import com.skydroid.rcsdk.common.pipeline.Pipeline;
import com.skydroid.rcsdk.common.remotecontroller.ControlMode;
import com.skydroid.rcsdk.key.AirLinkKey;
import com.skydroid.rcsdk.key.RemoteControllerKey;

import java.util.Arrays;

/**
 * @author 咔一下
 * @date 2023/10/11 9:50
 * @email 1501020210@qq.com
 * @describe
 */
public class HomeActivityForJava extends AppCompatActivity {
    public static final String TAG = "HomeActivity";

    private final KeyListener<Integer> keySignalQualityListener = new KeyListener<Integer>() {
        @Override
        public void onValueChange(Integer oldValue, Integer newValue) {
            strSignalValue = "信号强度:" + newValue;
            printInfo(InfoKey.Signal,strSignalValue);
        }
    };

    private final KeyListener<int[]> keyH16ChannelsListener = new KeyListener<int[]>() {
        @Override
        public void onValueChange(int[] oldValue, int[] newValue) {
            strH16ChannelsValue = Arrays.toString(newValue);
            printInfo(InfoKey.H16Channels,strH16ChannelsValue);
        }
    };

    private final MutableLiveData<String> infoLiveData = new MutableLiveData<String>();
    private TextView tvInfo = null;
    private String strSignalValue = "";
    private String strH16ChannelsValue = "";
    private String strOtherValue = "";

    private Pipeline pipeline = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        tvInfo = findViewById(R.id.tv_info);
        infoLiveData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String str) {
                tvInfo.setText(str);
            }
        });
        //初始化SDK
        RCSDKManager.INSTANCE.initSDK(this, new SDKManagerCallBack() {
            @Override
            public void onRcConnected() {
                //创建通讯管道(内部有断开重连机制，只需要调用一次连接即可)
                Pipeline pipeline = PipelineManager.INSTANCE.createPipeline(Uart.UART0);
                pipeline.setOnCommListener(new CommListener() {
                    @Override
                    public void onConnectSuccess() {
                        log("管道连接成功");
                    }

                    @Override
                    public void onConnectFail(SkyException e) {
                        log("管道连接失败" + e);
                    }

                    @Override
                    public void onDisconnect() {
                        log("管道断开连接");
                    }

                    @Override
                    public void onReadData(byte[] bytes) {

                    }
                });
                //连接通讯管道
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
        //连接到遥控器
        RCSDKManager.INSTANCE.connectToRC();
        initTestView();
    }

    private void initTestView() {
        findViewById(R.id.btn_pairing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyManager.INSTANCE.action(RemoteControllerKey.INSTANCE.getKeyRequestPairing(), new CompletionCallback() {
                    @Override
                    public void onResult(SkyException e) {
                        if (e == null){
                            printInfo(InfoKey.Other,"对频成功");
                        }else{
                            printInfo(InfoKey.Other,"对频失败：" + e);
                        }
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
                        if (e == null){
                            printInfo(InfoKey.Other,"设置摇杆模式成功");
                        }else{
                            printInfo(InfoKey.Other,"设置摇杆模式失败：" + e);
                        }
                    }
                });
            }
        });
        findViewById(R.id.btn_get_control_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取遥控器手型模式
                KeyManager.INSTANCE.get(RemoteControllerKey.INSTANCE.getKeyControlMode(), new CompletionCallbackWith<ControlMode>() {
                    @Override
                    public void onSuccess(ControlMode controlMode) {
                        printInfo(InfoKey.Other,"获取摇杆模式：" + controlMode.name());
                    }

                    @Override
                    public void onFailure(SkyException e) {
                        printInfo(InfoKey.Other,"获取摇杆模式失败：" + e);
                    }
                });
            }
        });
        findViewById(R.id.btn_get_channels).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取摇杆杆量
                switch (RCSDKManager.INSTANCE.getDeviceType()){
                    case H16:
                        //防止反复监听
                        KeyManager.INSTANCE.cancelListen(keyH16ChannelsListener);
                        //H16/H16Pro的摇杆杆量为LISTEN方式,设置监听器后，会一直回调，直到取消监听
                        KeyManager.INSTANCE.listen(RemoteControllerKey.INSTANCE.getKeyH16Channels(), keyH16ChannelsListener);
                        break;
                    default:
                        //H12/H12Pro/H30摇杆杆量为GET方式，需要主动请求，请求一次获取一次
                        KeyManager.INSTANCE.get(RemoteControllerKey.INSTANCE.getKeyChannels(), new CompletionCallbackWith<int[]>() {
                            @Override
                            public void onSuccess(int[] value) {
                                printInfo(InfoKey.Other,"获取摇杆杆量：" + Arrays.toString(value));
                            }

                            @Override
                            public void onFailure(SkyException e) {
                                printInfo(InfoKey.Other,"获取摇杆失败：" + e);
                            }
                        });
                        break;
                }
            }
        });

        findViewById(R.id.btn_get_signal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (RCSDKManager.INSTANCE.getDeviceType()){
                    case H12:
                        //H12的信号强度为GET方式，需要主动请求，请求一次获取一次
                        KeyManager.INSTANCE.get(AirLinkKey.INSTANCE.getKeyH12SignalQuality(), new CompletionCallbackWith<Integer>() {
                            @Override
                            public void onSuccess(Integer integer) {
                                printInfo(InfoKey.Other,"H12信号强度：" + integer);
                            }

                            @Override
                            public void onFailure(SkyException e) {
                                printInfo(InfoKey.Other,"H12信号强度获取失败：" + e);
                            }
                        });
                        break;

                    case H12Pro:
                        //防止反复监听
                        KeyManager.INSTANCE.cancelListen(keySignalQualityListener);
                        //H12Pro的信号强度为LISTEN方式,设置监听器后，会一直回调，直到取消监听
                        KeyManager.INSTANCE.listen(AirLinkKey.INSTANCE.getKeySignalQuality(),keySignalQualityListener);
                        break;
                    case H16:
                        //防止反复监听
                        KeyManager.INSTANCE.cancelListen(keySignalQualityListener);
                        //H16的信号强度为LISTEN方式,设置监听器后，会一直回调，直到取消监听
                        KeyManager.INSTANCE.listen(AirLinkKey.INSTANCE.getKeyH16SignalQuality(),keySignalQualityListener);
                        break;
                }

            }
        });
    }


    private void printInfo(InfoKey key,Object obj){
        if (obj == null){
            return;
        }
        switch (key){
            case Signal:
                strSignalValue = obj.toString();
                break;
            case H16Channels:
                strH16ChannelsValue = obj.toString();
                break;
            case Other:
                strOtherValue = obj.toString();
                break;
        }
        StringBuffer sb = new StringBuffer();
        if (!TextUtils.isEmpty(strSignalValue)){
            sb.append(strSignalValue);
            sb.append("\n");
        }
        if (!TextUtils.isEmpty(strH16ChannelsValue)){
            sb.append(strH16ChannelsValue);
            sb.append("\n");
        }
        if (!TextUtils.isEmpty(strOtherValue)){
            sb.append(strOtherValue);
        }
        infoLiveData.postValue(sb.toString());
        Log.e(TAG,obj.toString());
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
        //断开遥控器连接（如果不断开，程序还在运行的时候，其他程序会出端口占用情况）
        RCSDKManager.INSTANCE.disconnectRC();
        KeyManager.INSTANCE.cancelListen(keySignalQualityListener);
        Pipeline p = this.pipeline;
        if (p != null){
            PipelineManager.INSTANCE.disconnectPipeline(p);
        }
    }

    enum InfoKey{
        Signal,
        H16Channels,
        Other
    }
}
