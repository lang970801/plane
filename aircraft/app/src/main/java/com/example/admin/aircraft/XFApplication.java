package com.example.admin.aircraft;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * Created by lWX537240 on 2018/7/17.
 */

public class XFApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5c174f1e");

    }
}
