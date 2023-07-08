package com.example.homework1;

import android.app.Application;

import com.example.homework1.utils.SP;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SP.init(this);
    }
}
