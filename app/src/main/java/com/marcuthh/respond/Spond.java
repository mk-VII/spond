package com.marcuthh.respond;

import android.app.Application;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.google.firebase.database.FirebaseDatabase;

public class Spond extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
