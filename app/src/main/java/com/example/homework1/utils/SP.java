package com.example.homework1.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.homework1.interfaces.Constants;

public class SP implements Constants {
    private static SP instance;
    private final SharedPreferences prefs;

    public static SP getInstance() {
        return instance;
    }

    private SP(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(SP_FILE, Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new SP(context);
        }
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key, String def) {
        return prefs.getString(key, def);
    }
}