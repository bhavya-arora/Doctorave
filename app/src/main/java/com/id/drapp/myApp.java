package com.id.drapp;

import android.content.Context;

public class myApp extends android.app.Application{

    private static Context context;

    public void onCreate() {
        super.onCreate();
        myApp.context = getApplicationContext();
    }


    public static Context getAppContext() {
        return myApp.context;
    }

}
