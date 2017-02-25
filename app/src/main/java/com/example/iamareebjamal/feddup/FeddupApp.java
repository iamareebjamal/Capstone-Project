package com.example.iamareebjamal.feddup;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

public class FeddupApp extends Application {

    private RefWatcher refWatcher;

    public static RefWatcher getRefWatcher(Context context) {
        FeddupApp application = (FeddupApp) context.getApplicationContext();
        return application.refWatcher;
    }

    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);

        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        if(!BuildConfig.DEBUG) return;

        Stetho.initializeWithDefaults(this);
    }
}
