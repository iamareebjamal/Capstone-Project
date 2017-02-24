package com.example.iamareebjamal.feddup;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.google.firebase.database.FirebaseDatabase;

public class FeddupApp extends Application {

    static { FirebaseDatabase.getInstance().setPersistenceEnabled(true); }

    public void onCreate() {
        super.onCreate();

        if(!BuildConfig.DEBUG) return;

        Stetho.initializeWithDefaults(this);
    }
}
