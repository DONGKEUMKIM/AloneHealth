package com.example.caucse.alonehealth;


import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {
    SQLiteManager sqLiteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sqLiteManager = SQLiteManager.getInstance(this);
        sqLiteManager.init();
        Handler hd = new Handler();
        hd.postDelayed(new splashhandler(), 3000);  //3초

    }

    private class splashhandler implements Runnable{
        public void run(){
            startActivity(new Intent(getApplication(),MainActivity.class)); //로딩이 끝난후 이동할 Activity
            SplashActivity.this.finish();   //로딩페이지 Activity Stack에서 제거
        }

    }
}
