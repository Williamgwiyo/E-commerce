package com.tu.tuchati;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class Myshop1 extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
