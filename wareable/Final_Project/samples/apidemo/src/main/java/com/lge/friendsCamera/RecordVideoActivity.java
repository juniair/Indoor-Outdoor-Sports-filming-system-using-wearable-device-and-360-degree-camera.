/*
 * Copyright 2016 LG Electronics Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lge.friendsCamera;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.lge.osclibrary.HttpAsyncTask;
import com.lge.osclibrary.OSCCommandsExecute;
import com.lge.osclibrary.OSCCommandsStatus;
import com.lge.osclibrary.OSCParameterNameMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Record video, check recording status and live snapshot during recording
 * Before start recording video, 'captureMode' should be set as 'video'
 */
public class RecordVideoActivity extends AppCompatActivity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
       View.OnClickListener {
///////////////////////////////////////
private static final int STATUS_PAUSE = 0;
    private static final int STATUS_PLAYING = 1;
    private static final int STATUS_END = 2;

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);
    private ImageView mImageView;
    private Bitmap playImage;
    private Bitmap pauseImage;
    private Bitmap restartImage;
    private Button btnState;
    private GestureDetector gestureDetector;

    private SensorManager mSensorManager;
    private Sensor mGyroSensor;
    private GoogleApiClient mGoogleApiClient;

    private PowerManager.WakeLock mWakeLock;
    /////////////////////////////
    private TimerTask mTask;
    private Timer mTimer;
    private final int MSG_ONLY_DISMISS=1;
    //private AlertDialog mDialog;
    /////////////////////////////
    private boolean buff = false;
    private float gyro_x=0;
    private float gyro_y=0;
    private float gyro_z=0;
    private int stateFlag = 1;       // 현재 촬영대기상태인지 일반상태인지
    private int forOneShot = 0;     // 한번만 촬영요청을 하기 위해서 선언
    private int gameStatus = STATUS_PAUSE;

///////////////////////////////////////


    private final static String TAG = RecordVideoActivity.class.getSimpleName();

    private Context mContext;

    enum recordState {STOP_RECORDING, IS_RECORDING, PAUSE_RECORDING}

    private recordState currentRecordState;

    private static final String START = "camera.startCapture";
    private static final String RESUME = "camera._resumeRecording";
    private static final String PAUSE = "camera._pauseRecording";
    private static final String STOP = "camera.stopCapture";

    private Switch sButton;
    private Button buttonRecording;
    private Button buttonStop;
    private Button buttonLiveSnapShot;

    private ProgressDialog mProgressDialog;
    private boolean testBoolean = false;
    final String optionCaptureMode = "captureMode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
        initialize();

        ///////////////////////////
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    private void initialize() {
        mContext = this;
        FriendsCameraApplication.setContext(mContext);

        //getOption
        getOptionCaptureMode();
        setCaptureModeVideo();
        currentRecordState = recordState.STOP_RECORDING;
    }

    private void setupViews() {
        setContentView(R.layout.recordvideo_layout);

        buttonRecording = (Button) findViewById(R.id.button_startVideo);
        buttonRecording.setOnClickListener(this);

        buttonStop = (Button) findViewById(R.id.button_stopVideo);
        buttonStop.setOnClickListener(this);

        sButton = (Switch)findViewById(R.id.toggleSwitch);
        sButton.setOnClickListener(this);

        buttonRecording.setEnabled(false);
        buttonStop.setEnabled(false);


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.button_startVideo:
                startVideo();
                break;
            case R.id.button_stopVideo:
                stopVideo();
                break;
            case R.id.toggleSwitch:
                boolean on = ((Switch)view).isChecked();
                Log.d("on",String.valueOf(on));
                if(on)
                {
                    Log.d("testSwitch",String.valueOf(testBoolean));
                    testBoolean=true;
                    buttonRecording.setEnabled(true);
                    buttonStop.setEnabled(true);
                }
                else
                {
                    Log.d("testSwitch",String.valueOf(testBoolean));
                    testBoolean=false;
                    buttonRecording.setEnabled(false);
                    buttonStop.setEnabled(false);

                }

                break;
        }
    }

    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    ////////////////////////////////////
    public void onSensorChanged(SensorEvent event) {
        Log.d("Sensor", "onSensorChanged");
        Log.d("촬영전","test"+forOneShot);
        Log.d("testCheck",String.valueOf(forOneShot)+String.valueOf(testBoolean));
        if(event.sensor.getType() == Sensor.TYPE_HEART_RATE){

        }
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE && forOneShot==0 && testBoolean==true) {
            //gyro_x = event.values[0];
            //gyro_y = event.values[1];
            gyro_z = event.values[2];
            Log.d("aaaaaaaaaaaaa","aaaaaaaaaaa");


            if(gameStatus == STATUS_PLAYING){
                //sendHeadVector();
            }

            //final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            if (gyro_z <0) {
                buff = true;
                if(gyro_z < -0.5    ) {
                    System.out.println("Event");
                    Log.d("촬영중@@@","test=>"+forOneShot);

                    Toast toast = Toast.makeText(getApplicationContext(),
                            "촬영시작!!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    startVideo();
                }
            }
        }
    }

    public void sleep(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) { }
    }
    public void onConnectionSuspended(int i) {
    }
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
    public void onConnected(Bundle bundle) {
        //if(mGoogleApiClient != null) Wearable.DataApi.addListener(mGoogleApiClient, this);
        //sendMessage(STATUS);
    }
    ///////////////////////////////////

    /**
     * start/resume/pause recording video
     */
    private void startVideo() {
        forOneShot = 1;
        buttonRecording.setBackgroundResource(R.color.red);
        if (currentRecordState == recordState.STOP_RECORDING) {
            changeRecordingStatus(START);
        } else if (currentRecordState == recordState.PAUSE_RECORDING) {
            changeRecordingStatus(RESUME);
        } else {  //is recording
            changeRecordingStatus(PAUSE);
        }
        Toast toast = Toast.makeText(getApplicationContext(),
                "촬영시작!!", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * stop recording video
     */
    private void stopVideo() {
        //Stop Recording

        changeRecordingStatus(STOP);
        alert();
        if(forOneShot==1)
        {
            buttonRecording.setBackgroundResource(R.color.white);
            Toast toast = Toast.makeText(getApplicationContext(),
                        "촬영이 끝났습니다!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            forOneShot = 0;
            testBoolean = false;

            sButton.performClick();    // 강제로 클릭
        }

    }
    private void alert(){

        if(forOneShot==0)                // 입력한 회원번호가 존재하지 않으면
        {
            new AlertDialog.Builder(this)
                    .setMessage("촬영중이 아닙니다!!")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //dialog.dismiss();
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    /**
     * get recording status
     * API : /osc/commands/execute (camera._getRecordingStatus)
     */
    private void getRecordingStatus() {
        OSCCommandsExecute commandsExecute = new OSCCommandsExecute("camera._getRecordingStatus", null);
        commandsExecute.setListener(new HttpAsyncTask.OnHttpListener() {
            @Override
            public void onResponse(OSCReturnType type, Object response) {
                Utils.showTextDialog(mContext, getString(R.string.response), Utils.parseString(response));
            }
        });
        commandsExecute.execute();
    }

    /**
     * get captureMode option
     * API: /osc/commands/execute (camera.getOptions)
     */
    private void getOptionCaptureMode() {
        JSONObject parameters = new JSONObject();

        try {
            JSONArray optionParameter = new JSONArray();
            optionParameter.put(optionCaptureMode);

            parameters.put(OSCParameterNameMapper.Options.OPTIONNAMES, optionParameter);
            OSCCommandsExecute commandsExecute = new OSCCommandsExecute("camera.getOptions", parameters);

            commandsExecute.setListener(new HttpAsyncTask.OnHttpListener() {
                @Override
                public void onResponse(OSCReturnType type, Object response) {
                    try {

                        if (type == OSCReturnType.SUCCESS) {
                            //If the getOption request get response successfully,
                            //check whether the mode is video or not
                            JSONObject jObject = new JSONObject((String) response);

                            JSONObject results = jObject.getJSONObject(OSCParameterNameMapper.RESULTS);
                            JSONObject options = results.getJSONObject(OSCParameterNameMapper.Options.OPTIONS);
                            String captureMode = options.getString(optionCaptureMode);

                            if (!captureMode.equals("video")) {
                                //Ask user whether change captureMode as 'video' or not
                                //  yes = Send request to captureMode as video(camera.setOption)
                                //  no = finish this activity
                                DialogInterface.OnClickListener okListener =
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mProgressDialog = ProgressDialog.show
                                                        (mContext, "", "Setting..", true, false);
                                                setCaptureModeVideo();
                                            }
                                        };
                                DialogInterface.OnClickListener cancelListener =
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                ((RecordVideoActivity) mContext).finish();
                                            }
                                        };

                                Utils.showSelectDialog(
                                        mContext, "Note: ",
                                        "Do you want to change captureMode to 'video'?",
                                        okListener, cancelListener);
                            }
                        } else {
                            Utils.showTextDialog(mContext, getString(R.string.response),
                                    Utils.parseString(response));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            commandsExecute.execute();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * change captureMode to video
     * API: /osc/commands/execute (camera.setOptions)
     */
    private void setCaptureModeVideo() {
        JSONObject setParam = new JSONObject();
        JSONObject optionParam = new JSONObject();

        try {
            setParam.put("captureMode", "video");
            optionParam.put("options", setParam);

            OSCCommandsExecute commandsExecute =
                    new OSCCommandsExecute("camera.setOptions", optionParam);
            commandsExecute.setListener(new HttpAsyncTask.OnHttpListener() {
                @Override
                public void onResponse(OSCReturnType type, Object response) {
                    if (mProgressDialog != null)
                        mProgressDialog.cancel();

                    if (type == OSCReturnType.SUCCESS) {
                        Toast.makeText(mContext, "Set captureMode to 'video' successfully",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Utils.showTextDialog(mContext, getString(R.string.response), Utils.parseString(response));
                    }
                }
            });
            commandsExecute.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

@Override
protected void onDestroy() {
    //mWakeLock.release();
//    mDialog.dismiss();
    super.onDestroy();
}

    @Override
    protected void onPause() {
        super.onPause();
        //if(mGoogleApiClient != null) Wearable.DataApi.removeListener(mGoogleApiClient, this);
        if(mSensorManager != null) mSensorManager.unregisterListener(this);
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) mGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mSensorManager != null){
            mSensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(mGoogleApiClient != null) mGoogleApiClient.connect();
    }
    //////////////////////
    /**
     * Change recording status
     * API : /osc/commands/execute
     * (camera.startCapture, camera._resumeRecording, camera._pauseRecording )
     *
     * @param command START / RESUME / PAUSE / STOP
     */
    private void changeRecordingStatus(final String command) {
        OSCCommandsExecute commandsExecute = new OSCCommandsExecute(command, null);
        commandsExecute.setListener(new HttpAsyncTask.OnHttpListener() {
            @Override
            public void onResponse(OSCReturnType type, Object response) {
                Log.v(TAG, "command :: " + command);
                if (type == OSCReturnType.SUCCESS) {
                    String state = Utils.getCommandState(response);
                    Log.v(TAG, "state = " + state);
                    if (state != null) {
                        if (state.equals(OSCParameterNameMapper.STATE_INPROGRESS)) {
                            String commandId = Utils.getCommandId(response);
                            checkCommandsStatus(commandId);
                            return;
                        } else {
                            // state == done
                            setRecordingState(command);
                            if (mProgressDialog.isShowing())
                                mProgressDialog.cancel();
                        }
                    }
                }
                    Utils.showTextDialog(mContext, getString(R.string.response),
                            Utils.parseString(response));
                    //if (mProgressDialog.isShowing())//
                    //    mProgressDialog.cancel();//

            }
        });
        commandsExecute.execute();
    }

    /**
     * Check the status for previous inProgress commands.
     * Determine whether start/resume/pause recording and liveSnapshot have completed.
     *
     * @param commandId command Id of previous request
     *                  API : /osc/commands/status
     */
    private void checkCommandsStatus(final String commandId) {
        final OSCCommandsStatus commandsStatus = new OSCCommandsStatus(commandId);
        commandsStatus.setListener(new HttpAsyncTask.OnHttpListener() {
            @Override
            public void onResponse(OSCReturnType type, final Object response) {
                if (type == OSCReturnType.SUCCESS) {
                    String state = Utils.getCommandState((String) response);
                    Log.v(TAG, "state = " + state);
                    if (state != null && state.equals(OSCParameterNameMapper.STATE_INPROGRESS)) {
                        checkCommandsStatus(commandId);
                    } else {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.cancel();
                        Utils.showTextDialog(mContext, getString(R.string.response),
                                Utils.parseString(response));
                        updateUIBasedOnResponse(response);
                    }
                } else {
                    Utils.showTextDialog(mContext, getString(R.string.response),
                            Utils.parseString(response));
                }
            }
        });
        commandsStatus.execute();
    }

    private void updateUIBasedOnResponse(Object response) {
        String commandName = Utils.getCommandName(response);
        if (!commandName.equals("camera._liveSnapshot")) {
            setRecordingState(commandName);
        }
    }

    /**
     * set recording state
     * @param command START / RESUME / PAUSE / STOP
     */
    private void setRecordingState(String command) {
        //change recording status and UI button
        Log.v(TAG, "Change recording state from " + command);
        if (command.equals(START)) {
            currentRecordState = recordState.IS_RECORDING;
        } else if (command.equals(RESUME)) {
            currentRecordState = recordState.IS_RECORDING;
        } else if (command.equals(PAUSE)) {
            currentRecordState = recordState.PAUSE_RECORDING;
        } else { //camera._stopRecording
            currentRecordState = recordState.STOP_RECORDING;
        }
        setRecordingButton();
    }

    /**
     * set recording button
     */
    private void setRecordingButton() {
        if (currentRecordState == recordState.STOP_RECORDING) {
            buttonRecording.setText(R.string.start_recording);
        } else if (currentRecordState == recordState.PAUSE_RECORDING) {
            buttonRecording.setText(R.string.resume_recording);
        } else {   //IS_RECORDING
            buttonRecording.setText(R.string.pause_recording);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
