package com.skydroid.rcsdkdemo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
            Log.e(TAG,"信号强度:" + oldValue + "," + newValue);
        }
    };

    private Pipeline pipeline = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //初始化SDK
        RCSDKManager.INSTANCE.initSDK(this, new SDKManagerCallBack() {
            @Override
            public void onRcConnected() {
                //设置遥控器手型模式
                KeyManager.INSTANCE.set(RemoteControllerKey.INSTANCE.getKeyControlMode(), ControlMode.JP, new CompletionCallback() {
                    @Override
                    public void onResult(SkyException e) {
                        if (e == null){
                            log("设置摇杆模式成功");
                        }else{
                            log("设置摇杆模式失败：" + e);
                        }
                    }
                });

                //获取遥控器手型模式
                KeyManager.INSTANCE.get(RemoteControllerKey.INSTANCE.getKeyControlMode(), new CompletionCallbackWith<ControlMode>() {
                    @Override
                    public void onSuccess(ControlMode controlMode) {
                        log(controlMode);
                    }

                    @Override
                    public void onFailure(SkyException e) {

                    }
                });

                switch (RCSDKManager.INSTANCE.getDeviceType()){
                    case H12:
                        //H12的信号强度为GET方式，需要主动请求，请求一次获取一次
                        KeyManager.INSTANCE.get(AirLinkKey.INSTANCE.getKeyH12SignalQuality(), new CompletionCallbackWith<Integer>() {
                            @Override
                            public void onSuccess(Integer integer) {
                                log("H12信号强度：" + integer);
                            }

                            @Override
                            public void onFailure(SkyException e) {

                            }
                        });
                        break;

                    case H12Pro:
                        //H12Pro的信号强度为LISTEN方式,设置监听器后，会一直回调，直到取消监听
                        KeyManager.INSTANCE.listen(AirLinkKey.INSTANCE.getKeySignalQuality(),keySignalQualityListener);
                        break;
                    case H16:
                        //H16的信号强度为LISTEN方式,设置监听器后，会一直回调，直到取消监听
                        KeyManager.INSTANCE.listen(AirLinkKey.INSTANCE.getKeyH16SignalQuality(),keySignalQualityListener);
                        break;
                }

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
        findViewById(R.id.btn_pairing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyManager.INSTANCE.action(RemoteControllerKey.INSTANCE.getKeyRequestPairing(), new CompletionCallback() {
                    @Override
                    public void onResult(SkyException e) {
                        if (e == null){
                            log("对频成功");
                        }else{
                            log("对频失败：" + e);
                        }
                    }
                });
            }
        });
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
        KeyManager.INSTANCE.cancelListen(keySignalQualityListener);
        Pipeline p = this.pipeline;
        if (p != null){
            PipelineManager.INSTANCE.disconnectPipeline(p);
        }
    }
}
