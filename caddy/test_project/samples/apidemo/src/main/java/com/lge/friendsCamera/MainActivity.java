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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.lge.octopus.tentacles.wifi.client.WifiClient;
import com.lge.osclibrary.HTTP_SERVER_INFO;
import com.lge.osclibrary.HttpAsyncTask;
import com.lge.osclibrary.OSCCheckForUpdates;
import com.lge.osclibrary.OSCInfo;
import com.lge.osclibrary.OSCParameterNameMapper;
import com.lge.osclibrary.OSCState;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Main Activity
 * Info, State, CheckForUpdates APIs are executed in this activity
 * Other APIs are executed in other activity
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    private WifiReceiver receiver;
    public Context mContext ;

    private Handler mHandler;
    private Runnable mRunnable;

    private Button buttonConnect;
    private Button buttonConnect_Cam1;
    // private Button buttonCameraImage;
    private Button buttonCameraVideo;
    private Button buttonDownloadImage;
    private Button buttonDownloadVideo;
    private Button buttonInfo;
    private Button buttonState;
    private Button buttonCheckForUpdate;
    private Button buttonOptions;
    private Button buttonTakePicture;
    private Button buttonRecordVideo;
    private Button buttonUploading;
    private Button buttonNetwork;
    private Button buttonCaptureInterval;
    private Button buttonSettings;
    private Button buttonPreview;

    private TextView connectStatus;

    private String fingerPrint;

    private ProgressDialog mProgressDialog;



    ////////////////////////////////////////////////////////
    //private Button buttonChoose;
    //private Button buttonUpload;
    private TextView textView;
    private TextView textView_idx;
    private TextView textViewResponse;

    private static final int SELECT_VIDEO = 3;

    //    private String selectedPath;
    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {

        }

        @Override
        public void onPermissionDenied(ArrayList<String> arrayList) {

        }
    };
    ////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext= this;

        Log.d(TAG, "Main onCreate");
        super.onCreate(savedInstanceState);
        setupViews();
        initialize();

    }

    private void initialize() {
        receiver = new WifiReceiver();
        textView_idx.setText("회원번호 : "+Global.GlobalForSendIdx+ "  "+Global.GloabalForSendName+"회원님 촬영모드");
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

        textView = (TextView) findViewById(R.id.textView);
        textViewResponse = (TextView) findViewById(R.id.textViewResponse);
        textView_idx = (TextView)findViewById(R.id.textConnectionStatus);

        new TedPermission(this)
                .setPermissionListener(permissionListener)
                .setDeniedCloseButtonText("서버에 전송하기 위해 권한을 부여 할려고 합니다.")
                .setPermissions(Manifest.permission.INTERNET, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_CONTACTS, Manifest.permission.INTERNET)
                .check();


        //////////////////////////////////////////////////
        connectStatus = (TextView) findViewById(R.id.text_state);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "Typo_SsangmunDongB.ttf");
        connectStatus.setTypeface(typeFace);
        //txtId.setTypeface(typeFace);
        //1. Connect Button
        buttonConnect = (Button) findViewById(R.id.button_connect);
        buttonConnect.setOnClickListener(this);
        buttonConnect_Cam1 = (Button)findViewById(R.id.button_connect_cam1);
        buttonConnect_Cam1.setOnClickListener(this);

        //11. Record video
        buttonRecordVideo = (Button) findViewById(R.id.button_recordVideo);
        buttonRecordVideo.setOnClickListener(this);

        //12. Uploading video
        buttonUploading = (Button)findViewById(R.id.button_upload);
        buttonUploading.setOnClickListener(this);

        //13. Network AP
        buttonNetwork = (Button)findViewById(R.id.button_network);
        buttonNetwork.setOnClickListener(this);
    }

    /////////////////////////////////////////////////
    private void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a Video "), SELECT_VIDEO);
    }

    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        cursor.close();

        return path;
    }
    public static final int progress_bar_type = 0;
    private ProgressDialog pDialog;

    /**
     * Showing Dialog
     * */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0

                pDialog = new ProgressDialog(this);
                pDialog.setMessage("서버로 파일 송신중입니다..");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }
    private void uploadVideo() {
        class UploadVideo extends AsyncTask<String, String, String> {

            ProgressDialog uploading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showDialog(progress_bar_type);
//                uploading = ProgressDialog.show(MainActivity.this, "Uploading File", "Please wait...", false, false);
            }

            @Override
            protected void onPostExecute(String s) {
                dismissDialog(progress_bar_type);
                textViewResponse.setText(Html.fromHtml("<b>Uploaded at <a href='" + s + "'>" + s + "</a></b>"));
                Global.GlobalSendFlag=0;
                connectStatus.setText(R.string.wifi_status_disconnect);
                buttonConnect.setText(R.string.button_connect);
                buttonConnect.setBackgroundResource(R.color.ColorBlack);
                textViewResponse.setMovementMethod(LinkMovementMethod.getInstance());
                Log.d("Log:  ",s);
            }

            protected void onProgressUpdate(String... values) {
                pDialog.setProgress(Integer.parseInt(values[0]));
            }

            @Override
            protected String doInBackground(String... params) {

                String msg;

                // 파일 업로드
                int serverResponseCode = 0;
                String fileName = Global.selectedPath;
                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(fileName);
                if (!sourceFile.isFile()) {
                    Log.e("Huzza", "Source File Does not exist");
                    return null;
                }

                try {
                    if(Global.GlobalSendFlag==0)
                    {
                        Global.GlobalSendFlag++;
                        FileInputStream fileInputStream = new FileInputStream(sourceFile);
                        URL url = new URL("http://218.150.183.1/moons/mobile/upload.php");
//                        URL url = new URL("http://ngn.koreatech.ac.kr:80/moons/mobile/upload.php");
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setUseCaches(false);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);


                        conn.setRequestProperty("myFile", fileName);
                        Log.d("idx:: ",""+Global.GlobalForSendIdx);
                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"idx\"\r\n\r\n"+""+Global.GlobalForSendIdx);
                        dos.writeBytes("\r\n--" + boundary + "\r\n");

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"myFile\";filename=\"" + fileName + "\"" + lineEnd);
                        dos.writeBytes(lineEnd);


                        bytesAvailable = fileInputStream.available();
                        Log.i("Huzza", "Initial .available : " + bytesAvailable);

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        int lenghtOFfile = bytesAvailable;
                        long total = 0;
                        while (bytesRead > 0) {
                            total += bytesRead;
                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                            publishProgress(""+(int)((total*100)/lenghtOFfile));
                        }

                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                        serverResponseCode = conn.getResponseCode();

                        fileInputStream.close();
                        dos.flush();
                        dos.close();
                    }
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (serverResponseCode == 200) {
                    StringBuilder sb = new StringBuilder();
                    try {
                        Global.GlobalSendFlag=0;
                        BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                                .getInputStream()));
                        String line;
                        while ((line = rd.readLine()) != null) {
                            sb.append(line);
                        }
                        rd.close();
                    } catch (IOException ioex) {
                    }
                    msg = sb.toString();
                }else {
                    try {
                        Global.GlobalSendFlag = 0;
                        msg = "Could not upload";
                        Toast.makeText(getApplicationContext(), "전송실패!!", Toast.LENGTH_SHORT).show();
                        //uploadVideo();
                    }catch (Exception e){
                        Intent i;
                        i = new Intent(mContext, MainActivity.class);
                        startActivity(i);

                        msg = "Could not connected";
                    }
                }

                return msg;
            }
        }
        UploadVideo uv = new UploadVideo();
        uv.execute();
    }

    ////////////////////////////////////////////////
    @Override
    public void onClick(View view) {
        Intent i;
        String currentConnection = connectStatus.getText().toString();
        switch (view.getId()) {
            case R.id.button_connect:
//                String currentConnection = connectStatus.getText().toString();
                Log.d("connectStatus",currentConnection);
                if (currentConnection.equals(getResources().getString(R.string.wifi_status_connect))&&Global.usingCam_cam1==false) {
                    //disconnect with camera  카메라 연결 끊으면
                    updateStateBasedOnWifiConnection(false);
                    Global.usingCam_caddy=false;
                    connectStatus.setText(R.string.wifi_status_disconnect);
                    buttonConnect.setText(R.string.button_connect);
                    buttonConnect.setBackgroundResource(R.color.ColorBlack);

                    WifiReceiver.disconnectWifi(mContext);
                    Log.d("connectStatus",currentConnection);
                    //  DISCONNECT할 때 다른 AP로 접속시킴
                    WifiConfiguration wifiConfig2 = new WifiConfiguration();
//                    wifiConfig2.SSID = String.format("\"%s\"", "NGN_LAB");
//                    wifiConfig2.preSharedKey = String.format("\"%s\"", "NGNNGN10");
                    wifiConfig2.SSID = String.format("\"%s\"", "Gyungsoo");
                    wifiConfig2.preSharedKey = String.format("\"%s\"", "tn1258963");

                    WifiManager wifiManager1 = (WifiManager)getSystemService(WIFI_SERVICE);

                    int netId1 = wifiManager1.addNetwork(wifiConfig2);
                    wifiManager1.disconnect();
                    wifiManager1.enableNetwork(netId1, true);
//                    wifiManager1.disableNetwork(netId2,true);
                    wifiManager1.reconnect();
                }
                else {      // 카메라 연결 하면
                    if(Global.usingCam_cam1==true){        // 이미 캠1을 사용중이면
                        buttonConnect_Cam1.setText(R.string.button_connect_cam1);
                        buttonConnect_Cam1.setBackgroundResource(R.color.ColorBlack);
                    }

                    updateStateBasedOnWifiConnection(true);

                    connectStatus.setText(R.string.wifi_status_connect);
                    buttonConnect.setText(R.string.button_disconnect);
                    buttonConnect.setBackgroundResource(R.color.red);


                    Global.usingCam_caddy= true;
                    Global.usingCam_cam1=false;

                    WifiReceiver.disconnectWifi(mContext);
                    WifiConfiguration wifiConfig2 = new WifiConfiguration();
//                    wifiConfig2.SSID = String.format("\"%s\"", "LGR105_051490.OSC");
//                    wifiConfig2.preSharedKey = String.format("\"%s\"", "00051490");
                    wifiConfig2.SSID = String.format("\"%s\"", "LGR105_027157.OSC");
                    wifiConfig2.preSharedKey = String.format("\"%s\"", "00027157");
                    WifiManager wifiManager2 = (WifiManager)getSystemService(WIFI_SERVICE);
                    int netId2 = wifiManager2.addNetwork(wifiConfig2);
                    wifiManager2.disconnect();
                    wifiManager2.enableNetwork(netId2, true);
//                    wifiManager2.disableNetwork(netId1,true);
                    wifiManager2.reconnect();
                }
                break;

            case R.id.button_connect_cam1:
                Log.d("connectStatus",currentConnection);
                Global.GlobalisUpload=false;
                if (currentConnection.equals(getResources().getString(R.string.wifi_status_connect))&&Global.usingCam_caddy==false) {
                    //disconnect with camera
                    updateStateBasedOnWifiConnection(false);
                    Global.usingCam_cam1=false;
                    connectStatus.setText(R.string.wifi_status_disconnect);
                    buttonConnect_Cam1.setText(R.string.button_connect_cam1);
                    buttonConnect_Cam1.setBackgroundResource(R.color.ColorBlack);

                    WifiReceiver.disconnectWifi(mContext);
                    Log.d("connectStatus",currentConnection);
                    //  DISCONNECT할 때 다른 AP로 접속시킴
                    WifiConfiguration wifiConfig2 = new WifiConfiguration();
//                    wifiConfig2.SSID = String.format("\"%s\"", "NGN_LAB");
//                    wifiConfig2.preSharedKey = String.format("\"%s\"", "NGNNGN10");
                    wifiConfig2.SSID = String.format("\"%s\"", "Gyungsoo");
                    wifiConfig2.preSharedKey = String.format("\"%s\"", "tn1258963");

                    WifiManager wifiManager1 = (WifiManager)getSystemService(WIFI_SERVICE);

                    int netId1 = wifiManager1.addNetwork(wifiConfig2);
                    wifiManager1.disconnect();
                    wifiManager1.enableNetwork(netId1, true);
//                    wifiManager1.disableNetwork(netId2,true);
                    wifiManager1.reconnect();
                } else {


                    if(Global.usingCam_caddy==true){
                        buttonConnect.setText(R.string.button_connect);
                        buttonConnect.setBackgroundResource(R.color.ColorBlack);
                    }
                    Global.usingCam_cam1=true;
                    Global.usingCam_caddy=false;

                    updateStateBasedOnWifiConnection(true);


                    connectStatus.setText(R.string.wifi_status_connect);
                    //buttonConnect_Cam1.setText(R.string.button_disconnect_cam1);
                    buttonConnect_Cam1.setBackgroundResource(R.color.red);

                    WifiReceiver.disconnectWifi(mContext);
                    WifiConfiguration wifiConfig2 = new WifiConfiguration();
//                    wifiConfig2.SSID = String.format("\"%s\"", "LGR105_027157.OSC");
//                    wifiConfig2.preSharedKey = String.format("\"%s\"", "00027157");
                    wifiConfig2.SSID = String.format("\"%s\"", "LGR105_051490.OSC");
                    wifiConfig2.preSharedKey = String.format("\"%s\"", "00051490");
                    WifiManager wifiManager2 = (WifiManager)getSystemService(WIFI_SERVICE);
                    int netId2 = wifiManager2.addNetwork(wifiConfig2);
                    wifiManager2.disconnect();
                    wifiManager2.enableNetwork(netId2, true);
//                    wifiManager2.disableNetwork(netId1,true);
                    wifiManager2.reconnect();

                    mRunnable = new Runnable() {            //  동영상 정지 누르면 동영상 저장을 위해 5초 후에 액티비티 전환 후 자동 저장
                        @Override
                        public void run() {
                            Intent i;
                            i = new Intent(mContext, CameraFileListViewActivity.class);
                            i.putExtra("type", "video");
                            startActivity(i);
//                            finish();
                            //mProgressDialog = ProgressDialog.show(mContext, "", "Downloading...", true, false);
                        }
                    };

                    mHandler = new Handler();
                    mHandler.postDelayed(mRunnable, 2000); // 2초 후에 실행
                }
                break;



            case R.id.button_recordVideo:
                textViewResponse.setText("");

                i = new Intent(mContext, RecordVideoActivity.class);
                startActivity(i);
                break;
            case R.id.button_upload:
                uploadVideo();
                break;
            case R.id.button_network:
                updateStateBasedOnWifiConnection(false);

                connectStatus.setText(R.string.wifi_status_disconnect);

                WifiReceiver.disconnectWifi(mContext);
                //  DISCONNECT할 때 다른 AP로 접속시킴
                WifiConfiguration wifiConfig3 = new WifiConfiguration();
//                wifiConfig3.SSID = String.format("\"%s\"", "NGN_LAB");
//                wifiConfig3.preSharedKey = String.format("\"%s\"", "NGNNGN10");
                wifiConfig3.SSID = String.format("\"%s\"", "Gyungsoo");
                wifiConfig3.preSharedKey = String.format("\"%s\"", "tn1258963");

                WifiManager wifiManager2 = (WifiManager)getSystemService(WIFI_SERVICE);

                int netId2 = wifiManager2.addNetwork(wifiConfig3);
                wifiManager2.disconnect();
                wifiManager2.enableNetwork(netId2, true);
                wifiManager2.reconnect();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "Main onResume");
        FriendsCameraApplication.setContext(this);
        updateStateBasedOnWifiConnection(WifiReceiver.isConnected());
        Log.d("state123","123");
        textView.setText(Global.selectedPath);

        Intent intent = new Intent();
        if(Global.selectedPath != "/storage/emulated/0/DCIM/friendsCameraSample/" &&
                Global.selectedPath != null && Global.GlobalisUpload == false) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    uploadVideo();
                    buttonConnect_Cam1.setBackgroundResource(R.color.ColorBlack);
                    Log.d("isUpload ::" , Global.GlobalisUpload.toString());
                    Global.GlobalisUpload = true;

                }
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable, 5000);   // 에러나면 5000으로 할것
        }

    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "Main onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "Main onPause");

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(pDialog!=null){
            pDialog.dismiss();
            pDialog=null;
        }
        Log.v(TAG, "Main onDestroy");
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.d(TAG, "Fail to unregister receiver");
        }
        //Global.GlobalForSendIdx= 0 ;
        Log.d("GlobalForSendafter ",""+Global.GlobalForSendIdx);

        if(Global.usingCam_caddy==true || Global.usingCam_cam1==true) {
            WifiReceiver.disconnectWifi(mContext);

            //  DISCONNECT할 때 다른 AP로 접속시킴
            WifiConfiguration wifiConfig2 = new WifiConfiguration();
//            wifiConfig2.SSID = String.format("\"%s\"", "NGN_LAB");
//            wifiConfig2.preSharedKey = String.format("\"%s\"", "NGNNGN10");
            wifiConfig2.SSID = String.format("\"%s\"", "Gyungsoo");
            wifiConfig2.preSharedKey = String.format("\"%s\"", "tn1258963");

            WifiManager wifiManager1 = (WifiManager) getSystemService(WIFI_SERVICE);

            int netId1 = wifiManager1.addNetwork(wifiConfig2);
            wifiManager1.disconnect();  //0907
            wifiManager1.enableNetwork(netId1, true);
            //                    wifiManager1.disableNetwork(netId2,true);
            wifiManager1.reconnect();
            Global.usingCam_caddy=false;
            Global.usingCam_cam1=false;
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

        Log.d("state:::::",String.valueOf(state));
        if(state==false)
        {
            buttonRecordVideo.setBackgroundResource(R.color.gray);
        }
        else{
            buttonRecordVideo.setBackgroundResource(R.color.ColorBlack);
        }
        buttonRecordVideo.setEnabled(state);

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
