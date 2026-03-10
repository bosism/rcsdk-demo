package com.skydroid.rcsdkdemo.other;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;

import com.skydroid.rcsdk.common.error.SkyException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class
 * Created by ljb on 2024.06.13.
 */
public class AppUtils {
    public static final SimpleDateFormat timestampFormatter = new SimpleDateFormat("HH:mm:ss.SSS:", Locale.US);

    public static String getTimeStamp() {
        return timestampFormatter.format(new Date());
    }

    public static String getSkyExceptionInfo(String cmd, SkyException e, String version) {
        return cmd + (e == null ? "Success" : ("Failed: " + e.getMessage())) + "--" + version;
    }

    public static void showC10pCameraControlDialog(Context context, DialogInterface.OnClickListener listener) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
        final String[] list = {"Camera version", "Preset down", "Preset center", "Preset up", "Take picture", "Start recording", "Stop recording", "Set time", "Yaw right", "Yaw left", "Pitch up", "Pitch down"};
        arrayAdapter.addAll(list);
        new AlertDialog.Builder(context)
                .setAdapter(arrayAdapter, listener)
                .create().show();
    }

    public static void showLEDCameraControlDialog(Context context, DialogInterface.OnClickListener listener) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
        final String[] list = {"LED on", "LED off"};
        arrayAdapter.addAll(list);
        new AlertDialog.Builder(context)
                .setAdapter(arrayAdapter, listener)
                .create().show();
    }

}
