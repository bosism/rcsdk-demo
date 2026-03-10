package com.skydroid.rcsdkdemo;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.skydroid.rcsdk.PayloadManager;
import com.skydroid.rcsdk.RCSDKManager;
import com.skydroid.rcsdk.comm.CommListener;
import com.skydroid.rcsdk.common.button.ButtonAction;
import com.skydroid.rcsdk.common.button.ButtonConfig;
import com.skydroid.rcsdk.common.button.ButtonHandler;
import com.skydroid.rcsdk.common.button.ButtonHandlerListener;
import com.skydroid.rcsdk.common.button.ButtonHelper;
import com.skydroid.rcsdk.common.button.HandleButtonMode;
import com.skydroid.rcsdk.common.callback.CompletionCallback;
import com.skydroid.rcsdk.common.error.ErrorException;
import com.skydroid.rcsdk.common.error.SkyException;
import com.skydroid.rcsdk.common.payload.C10Pro;
import com.skydroid.rcsdk.common.payload.PayloadType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 咔一下
 * @date 2024/4/3 10:15
 * @email 1501020210@qq.com
 * @describe Custom remote controller buttons
 */
public class CustomRCButtonsActivity extends AppCompatActivity {

    private TextView tv_info;

    //Utility to collect remote controller channel values
    private ReadRCButtonHelper readRCButtonHelper;

    //Remote controller custom button utility
    private ButtonHelper c10pButtonHelper;
    private ButtonHelper customButtonHelper;
    private final ButtonHandlerListener buttonHandlerListener = new ButtonHandlerListener() {
        @Override
        public void onButtonActionResult(@NonNull ButtonAction buttonAction, boolean b) {
            if (buttonAction == ButtonAction.GIMBAL_YAW || buttonAction == ButtonAction.GIMBAL_PITCH){
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView tv = tv_info;
                    if (tv != null){
                        tv.setText("Executed action: " + buttonAction.name() + ", result: " + b);
                    }
                }
            });

        }
    };

    //C10Pro
    private C10Pro c10Pro;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_rc_buttons);
        tv_info = findViewById(R.id.tv_info);

        //Collect RC button channel data into custom button helper
        ReadRCButtonHelper readRCButtonHelper = new ReadRCButtonHelper(RCSDKManager.INSTANCE.getDeviceType());
        this.readRCButtonHelper = readRCButtonHelper;
        readRCButtonHelper.setListener(new ReadRCButtonHelper.RCButtonValueListener() {
            @Override
            public void onRCButtonValue(int[] buttons) {
                ButtonHelper c10pButtonHelper = CustomRCButtonsActivity.this.c10pButtonHelper;
                ButtonHelper customButtonHelper = CustomRCButtonsActivity.this.customButtonHelper;
                if (c10pButtonHelper != null){
                    c10pButtonHelper.receiveButtonData(buttons);
                }
                if (customButtonHelper != null){
                    customButtonHelper.receiveButtonData(buttons);
                }
            }
        });
        readRCButtonHelper.start();

        ButtonHelper c10pButtonHelper = new ButtonHelper();
        ButtonHelper customButtonHelper = new ButtonHelper();
        this.c10pButtonHelper = c10pButtonHelper;
        this.customButtonHelper = customButtonHelper;
        //Listen for execution result
        c10pButtonHelper.addListener(buttonHandlerListener);
        customButtonHelper.addListener(buttonHandlerListener);

        //Remote controller custom button utility-启用
        c10pButtonHelper.start();
        customButtonHelper.start();

        //Connect C10Pro
        C10Pro c10Pro = (C10Pro)PayloadManager.INSTANCE.getUDPPayload(PayloadType.C10PRO,5000,"192.168.144.108",5000);
        c10Pro.setCommListener(new CommListener() {
            @Override
            public void onConnectSuccess() {
                log("C10Pro connected");
            }

            @Override
            public void onConnectFail(SkyException e) {
                log("C10Pro connect failed: " + e);
            }

            @Override
            public void onDisconnect() {
                log("C10Pro disconnected");
            }

            @Override
            public void onReadData(byte[] bytes) {
                log("C10Pro received data: " + bytes);
            }
        });
        this.c10Pro = c10Pro;
        PayloadManager.INSTANCE.connectPayload(c10Pro);
        //Configure button channels
        List<ButtonConfig> c10pConfigs = new ArrayList<>();
        //Configuration for H12Pro
        //Channel 11 (H12Pro G roller): gimbal yaw  HandleButtonMode有2种类型 -- ALWAYS:持续调用,适用于摇杆控制云台。CHANGE:通道值变化才调用,适用一键控制，拍照，录像等
        c10pConfigs.add(new ButtonConfig(10,ButtonAction.GIMBAL_YAW,HandleButtonMode.ALWAYS));
        //Channel 12 (H12Pro H roller): gimbal pitch
        c10pConfigs.add(new ButtonConfig(11,ButtonAction.GIMBAL_PITCH,HandleButtonMode.ALWAYS));
        //Channel 9 (H12Pro C button): recenter gimbal
        c10pConfigs.add(new ButtonConfig(8,ButtonAction.GIMBAL_MID,HandleButtonMode.CHANGE));
        //Channel 10 (H12Pro D button): gimbal down
        c10pConfigs.add(new ButtonConfig(9,ButtonAction.GIMBAL_DOWN,HandleButtonMode.CHANGE));

        /*
        //创建默认配置
        //H12Pro channels 11/12 control gimbal；
        //H16,H30 13、14通道控制云台；
        //H20 14通道控制云台，7通道拍照
        List<ButtonConfig> defConfigs = ButtonHelper.Companion.createDefaultConfig();
        */

        //Bind configuration with C10Pro
        c10pButtonHelper.setConfig(c10pConfigs,c10Pro);

        //Custom events for H12Pro
        List<ButtonConfig> customButtonConfigs = new ArrayList<>();
        //6通道（H12Pro A按钮）-自定义0
        customButtonConfigs.add(new ButtonConfig(5,ButtonAction.CUSTOM_0,HandleButtonMode.CHANGE));
        //7通道（H12Pro F拨杆）-自定义1
        customButtonConfigs.add(new ButtonConfig(6,ButtonAction.CUSTOM_1,HandleButtonMode.CHANGE));
        //Custom button event handler
        ButtonHandler buttonHandler = new ButtonHandler() {
            @Override
            public void onHandleButton(@NonNull ButtonAction buttonAction, int oldValue, int newValue, @NonNull int[] ints, @NonNull CompletionCallback completionCallback) {
                switch (buttonAction){
                    case CUSTOM_0:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),"Handle custom button event 0: " + oldValue + "，" + newValue,Toast.LENGTH_SHORT).show();
                                //Mark result as success
                                completionCallback.onResult(null);
                            }
                        });
                        break;
                    case CUSTOM_1:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),"Handle custom button event 1: " + oldValue + "，" + newValue,Toast.LENGTH_SHORT).show();
                                if (newValue == 1950){
                                    //Mark result as success
                                    completionCallback.onResult(null);
                                }else {
                                    //Mark result as failed
                                    completionCallback.onResult(new ErrorException());
                                }

                            }
                        });
                        break;
                }
            }
        };
        //Bind configuration to event handler
        customButtonHelper.setConfig(customButtonConfigs,buttonHandler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (readRCButtonHelper != null) {
            readRCButtonHelper.stop();
        }
        //Remote controller custom button utility-关闭
        ButtonHelper c10pButtonHelper = this.c10pButtonHelper;
        ButtonHelper customButtonHelper = this.customButtonHelper;
        if (c10pButtonHelper != null){
            c10pButtonHelper.removeListener(buttonHandlerListener);
            c10pButtonHelper.stop();
        }
        if (customButtonHelper != null){
            customButtonHelper.removeListener(buttonHandlerListener);
            customButtonHelper.stop();
        }
        C10Pro c10Pro = this.c10Pro;
        if (c10Pro != null){
            PayloadManager.INSTANCE.disconnectPayload(c10Pro);
        }

    }

    private void log(Object obj) {
        if (obj == null) {
            return;
        }
        Log.e(CustomRCButtonsActivity.class.toString(), obj.toString());
    }
}
