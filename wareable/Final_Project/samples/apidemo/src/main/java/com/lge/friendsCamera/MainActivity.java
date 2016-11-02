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

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lge.octopus.ConnectionManager;
import com.lge.octopus.OctopusManager;
import com.lge.octopus.tentacles.wifi.client.WifiClient;
import com.lge.osclibrary.HTTP_SERVER_INFO;
import com.lge.osclibrary.HttpAsyncTask;
import com.lge.osclibrary.OSCCheckForUpdates;
import com.lge.osclibrary.OSCInfo;
import com.lge.osclibrary.OSCParameterNameMapper;
import com.lge.osclibrary.OSCState;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Main Activity
 * Info, State, CheckForUpdates APIs are executed in this activity
 * Other APIs are executed in other activity
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = MainActivity.class.getSimpleName();


    private WifiReceiver receiver;
    public Context mContext = this;

    private Button buttonConnect;
    //private Button buttonCameraVideo;
    private Button buttonRecordVideo;

    private TextView connectStatus;
    private TextView user_num;
   //0816 private TextView caddy_num;
    private TextView text_CamNum;
    private String fingerPrint;

    private ProgressDialog mProgressDialog;
    ///////////////////////
    private ConnectionManager mConnectionManager; // connection manager to use connection library
    private ScanResult selectedDevice;

    ///////////////////
    /// getString 해온 데이터를 저장하는 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Main onCreate");
        super.onCreate(savedInstanceState);
        setupViews();
        initialize();

        user_num.setText("User Number : "+ Global.GlobalUserIdx + " Name : "+Global.GlobalUserName);
        //0816caddy_num.setText("Caddy Number : "+Global.GlobalCaddyIdx+" Caddy Name : "+Global.GlobalCaddyName);
    }

    private void initialize() {
        receiver = new WifiReceiver();

        /////////
        mConnectionManager = OctopusManager.getInstance(this).getConnectionManager();
        /////////
        FriendsCameraApplication.setContext(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiClient.ACTION_WIFI_STATE);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(receiver, filter);

        checkFileWritePermission();
        fingerPrint = "";
    }

    private void setupViews() {
        setContentView(R.layout.main_layout);

        connectStatus = (TextView) findViewById(R.id.text_state);
        user_num = (TextView) findViewById(R.id.user_num);
        text_CamNum = (TextView) findViewById(R.id.text_camNum);
        //0816 caddy_num = (TextView)findViewById(R.id.caddy_num);
        //1. Connect Button
        buttonConnect = (Button) findViewById(R.id.button_connect);
        buttonConnect.setOnClickListener(this);

//        //3. Get video list
//        buttonCameraVideo = (Button) findViewById(R.id.button_cameravideo);
//        buttonCameraVideo.setOnClickListener(this);

        //11. Record video
        buttonRecordVideo = (Button) findViewById(R.id.button_recordVideo);
        buttonRecordVideo.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent i;
        switch (view.getId()) {
            case R.id.button_connect:
                String currentConnection = connectStatus.getText().toString();
                if (currentConnection.equals(getResources().getString(R.string.wifi_status_connect))) {
                    //disconnect with camera
                    Log.d("connectStatus22",currentConnection);
                    updateStateBasedOnWifiConnection(false);
                    WifiReceiver.disconnectWifi(mContext);

                    //  DISCONNECT할 때 다른 AP로 접속시킴
                    WifiConfiguration wifiConfig = new WifiConfiguration();
//                    wifiConfig.SSID = String.format("\"%s\"", "NGN_LAB");
//                    wifiConfig.preSharedKey = String.format("\"%s\"", "NGNNGN10");
                    wifiConfig.SSID = String.format("\"%s\"", "Gyungsoo");
                    wifiConfig.preSharedKey = String.format("\"%s\"", "tn1258963");
                    Toast.makeText(mContext," NGN_LAB연결됨!!", Toast.LENGTH_SHORT).show();
                    WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
                    int netId = wifiManager.addNetwork(wifiConfig);
                    //wifiManager.disconnect();
                    wifiManager.enableNetwork(netId, true);
                    wifiManager.reconnect();
                    /////////////////////////////////////////////////////
                    Log.d("jaehwan's connect","연결됨");
                    /////////
                    Global.GlobalCamNum=0;      // 캠을 연결해제했을 때 캠 연결한 것이 없다고 출력
                    Log.d("GlobalCamNum========= ",""+Global.GlobalCamNum);


                } else {
                    //connect with camera           // Connect 라고 써져있으면 카메라 연결 클래스로 이동
                    //WifiReceiver.disconnectWifi(mContext);
                    updateStateBasedOnWifiConnection(true);
                    WifiConfiguration wifiConfig = new WifiConfiguration();
//                    wifiConfig.SSID = String.format("\"%s\"", "LGR105_051490.OSC");
//                    wifiConfig.preSharedKey = String.format("\"%s\"", "00051490");
                    wifiConfig.SSID = String.format("\"%s\"", "LGR105_051490.OSC");
                    wifiConfig.preSharedKey = String.format("\"%s\"", "00051490");
                    Log.d("LGR105_051490.OSC","연결됨");
                    Global.GlobalCamNum=1;
                    WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
                    int netId = wifiManager.addNetwork(wifiConfig);
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(netId, true);
                    wifiManager.reconnect();
//                    i = new Intent(mContext, ConnectionActivity.class);
//                    startActivity(i);
                }
                break;

            case R.id.button_recordVideo:
                i = new Intent(mContext, RecordVideoActivity.class);

                startActivity(i);
                break;
        }
    }
    /////////////////// 7/19
    private String getDefaultPassphrase() {

        //return selectedDevice.getIsFactory() ? "00" + selectedDevice.getSerial() : null;
        return "woghksl1";

    }
    ///////////////////
    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "Main onResume");
        FriendsCameraApplication.setContext(this);
        updateStateBasedOnWifiConnection(WifiReceiver.isConnected());

        if(Global.GlobalCamNum==1)
            text_CamNum.setText("CAM1 ");
        else if(Global.GlobalCamNum==2)
            text_CamNum.setText("CAM2 ");
        else if(Global.GlobalCamNum==3)
            text_CamNum.setText("CAM3 ");
        else
            text_CamNum.setText("");

    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "Main onStop");
        // 화면이 꺼졌을때
        WifiReceiver.disconnectWifi(mContext);

        //  DISCONNECT할 때 다른 AP로 접속시킴
        WifiConfiguration wifiConfig = new WifiConfiguration();
//        wifiConfig.SSID = String.format("\"%s\"", "NGN_LAB");
//        wifiConfig.preSharedKey = String.format("\"%s\"", "NGNNGN10");
        wifiConfig.SSID = String.format("\"%s\"", "Gyungsoo");
        wifiConfig.preSharedKey = String.format("\"%s\"", "tn1258963");
        Toast.makeText(mContext," NGN_LAB연결됨!!", Toast.LENGTH_SHORT).show();
        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        int netId = wifiManager.addNetwork(wifiConfig);
        //wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "Main onPause");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "Main onDestroy");
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.d(TAG, "Fail to unregister receiver");
        }
    }

    /**
     * Set IP address and Port number
     * @param ip
     */
    private void setIPPort(String ip) {
        String[] temp = ip.split(":");
        HTTP_SERVER_INFO.IP = temp[0];
        if (temp.length == 2) {
            HTTP_SERVER_INFO.PORT = temp[1];
        } else {
            HTTP_SERVER_INFO.PORT = "6624";
        }
    }


    /**
     * Get the basic information of the LG 360 CAM device
     * API : /osc/info
     */
    private void getCameraInfo() {
        final OSCInfo oscInfo = new OSCInfo();
        oscInfo.setListener(new OSCInfo.OnHttpListener() {
            @Override
            public void onResponse(OSCReturnType type, Object response) {
                String title = getString(R.string.camera_info);
                Utils.showTextDialog(mContext, title, Utils.parseString(response));
            }
        });

        oscInfo.execute();
    }

    /**
     * Get the device information that change over time such as battery level, battery state, etc.
     * API : /osc/state
     */
    private void getCameraState() {
        final OSCState oscState = new OSCState();
        oscState.setListener(new OSCInfo.OnHttpListener() {
            @Override
            public void onResponse(OSCReturnType type, Object response) {
                String title = getString(R.string.camera_state);
                if (type == OSCReturnType.SUCCESS) {
                    try {
                        JSONObject jObject = new JSONObject((String) response);
                        fingerPrint = jObject.getString(OSCParameterNameMapper.FINGERPRINT);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Utils.showTextDialog(mContext, title, Utils.parseString(response));
            }
        });
        oscState.execute();
    }


    /**
     * Update the fingerprint to reflect the current camera state by comparing it with the fingerprint held by the client.
     * API : /osc/checkForUpdate
     */
    private void getCameraCheckForUpdate() {
        final String title = getString(R.string.check_update);
        mProgressDialog = ProgressDialog.show(mContext, null, "Checking...", true, false);
        final OSCCheckForUpdates oscCheckForUpdates = new OSCCheckForUpdates(fingerPrint, 1);
        oscCheckForUpdates.setListener(new HttpAsyncTask.OnHttpListener() {
            @Override
            public void onResponse(OSCReturnType type, Object response) {
                if (type == OSCReturnType.SUCCESS) {
                    JSONObject jObject = null;
                    try {
                        mProgressDialog.cancel();
                        jObject = new JSONObject(response.toString());

                        String responseFingerprint = jObject.getString(OSCParameterNameMapper.LOCAL_FINGERPRINT);
                        if (fingerPrint.equals(responseFingerprint)) {
                            Utils.showAlertDialog(mContext, title, "State is same\n\n" + Utils.parseString(response), null);
                        } else {
                            Utils.showAlertDialog(mContext, title,
                                    "State is updated, Please check state by /osc/state\n\n" + Utils.parseString(response), null);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Utils.showTextDialog(mContext, title, Utils.parseString(response));
                }
            }
        });
        oscCheckForUpdates.execute();
    }

    /**
     * Update Main UI.
     * Buttons are enable when camera is connected
     * @param state
     */
    public void updateStateBasedOnWifiConnection(boolean state) {
        //        buttonCameraImage.setEnabled(state);
        //buttonCameraVideo.setEnabled(state);
//        buttonDownloadVideo.setEnabled(state);
//        buttonInfo.setEnabled(state);
//        buttonState.setEnabled(state);
//        buttonCheckForUpdate.setEnabled(state);
//        buttonOptions.setEnabled(state);
//        buttonTakePicture.setEnabled(state);
        buttonRecordVideo.setEnabled(state);
//        buttonCaptureInterval.setEnabled(state);
//        buttonSettings.setEnabled(state);
//        buttonPreview.setEnabled(state);

        if (state) {
            connectStatus.setText(R.string.wifi_status_connect);
            buttonConnect.setText(R.string.button_disconnect);

        } else {
            connectStatus.setText(R.string.wifi_status_disconnect);
            buttonConnect.setText(R.string.button_connect);
            text_CamNum.setText("");
        }
    }

    private final int MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    /**
     * Ask "write storage" permission to user
     */
    private void checkFileWritePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission for write external storage is granted");
                } else {
                    Log.d(TAG, "Permission for write external storage is denied");
                }
        }
    }

}
