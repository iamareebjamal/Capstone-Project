package com.example.iamareebjamal.feddup;

import android.app.Application;

import com.facebook.stetho.Stetho;

public class FeddupApp extends Application {

    public void onCreate() {
        super.onCreate();

        if(!BuildConfig.DEBUG) return;

        Stetho.initializeWithDefaults(this);
    }
}
