package com.crv.myapplication.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.crv.myapplication.R;

/**
 * Created by NGN_PRINT on 2016-07-25.
 */
public class SplashActivity extends Activity {

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        // Creates a count timer, which will be expired after 5000 milli Seconds
        new CountDownTimer(3000, 1000){
            // This method will be invoked on finishing or expiring the timer
            @Override
            public void onFinish(){
                // Creates an intent to start new activity
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
            @Override
            public void onTick(long millisUntilFinished){
            }
        }.start();
    }
}
