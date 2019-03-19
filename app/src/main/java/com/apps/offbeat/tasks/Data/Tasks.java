package com.apps.offbeat.tasks.Data;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class Tasks extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
