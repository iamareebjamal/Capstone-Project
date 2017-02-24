package com.example.iamareebjamal.feddup;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class FeddupApp extends Application {

    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        if(!BuildConfig.DEBUG) return;

        Stetho.initializeWithDefaults(this);
    }
}
