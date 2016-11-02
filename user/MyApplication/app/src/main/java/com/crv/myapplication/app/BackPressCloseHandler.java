package com.crv.myapplication.app;

import android.app.Activity;
import android.widget.Toast;

/**
 * Created by NGN_PRINT on 2016-09-12.
 */
public class BackPressCloseHandler {
    private long backKeyPressedTime = 0;
    private Toast toast;

    private Activity activity;

    public BackPressCloseHandler(Activity context){
        this.activity = context;
    }

    public void onBackPressed(){
        if(System.currentTimeMillis() > backKeyPressedTime + 2000){
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }

        if(System.currentTimeMillis() <= backKeyPressedTime + 2000){
            toast.cancel();

            activity.moveTaskToBack(true);
            activity.finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public void showGuide(){
        toast = Toast.makeText(activity, "한번 더 누르시면 종료 됩니다.",Toast.LENGTH_SHORT);
        toast.show();
    }
}
