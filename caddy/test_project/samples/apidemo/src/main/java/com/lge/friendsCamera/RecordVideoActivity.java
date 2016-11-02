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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.lge.osclibrary.HttpAsyncTask;
import com.lge.osclibrary.OSCCommandsExecute;
import com.lge.osclibrary.OSCCommandsStatus;
import com.lge.osclibrary.OSCParameterNameMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
/**
 * Record video, check recording status and live snapshot during recording
 * Before start recording video, 'captureMode' should be set as 'video'
 */
public class RecordVideoActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = RecordVideoActivity.class.getSimpleName();

    ////////////////////
    private String currentDownloadFile; //Recent download file name
    CustomListAdapter adapter;
    private ListView mListView;
    private final int entryCount = 1;

    ProgressDialog downloading;

    private Handler mHandler;
    private Runnable mRunnable;

    //Array list for file information HashMap
    ArrayList<HashMap<String, String>> itemInfo = new ArrayList<HashMap<String, String>>();
    //Array list for thumbnail id
    ArrayList<Integer> itemBitmap = new ArrayList<>();

    private static final String IMAGE = "image";
    private static final String VIDEO = "video";

    private String mediaType = VIDEO;
    private boolean startDownloading;
    ////////////////////
    private Context mContext;

    enum recordState {STOP_RECORDING, IS_RECORDING, PAUSE_RECORDING}

    private recordState currentRecordState;

    private static final String START = "camera.startCapture";
    private static final String RESUME = "camera._resumeRecording";
    private static final String PAUSE = "camera._pauseRecording";
    private static final String STOP = "camera.stopCapture";

    private Button buttonRecording;
    private Button buttonStop;
    private Button buttonLiveSnapShot;

    private ProgressDialog mProgressDialog;

    final String optionCaptureMode = "captureMode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
        initialize();
        Log.d("GlobalFlag:",""+Global.GlobalFlag);

    }

    private void initialize() {
        mContext = this;
        FriendsCameraApplication.setContext(mContext);
        //////////////////////////////////////////
        if (mediaType == null) {
            Log.v(TAG, "ERROR: Need to set media type");
            return;
        }

        //Set adapter type based on media type
        if (mediaType.equals(IMAGE)) {
            adapter.setType(CustomListAdapter.selectedGalleryType.CAMERA_IMAGE);
        } else if (mediaType.equals(VIDEO)) {
            adapter.setType(CustomListAdapter.selectedGalleryType.CAMERA_VIDEO);
        }
        //Set adapter for list view
        //  Multiple choice mode for deleting multiple items
        //  Single choice mode for selecting an item to show info, download, and delete
        mListView.setAdapter(adapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setItemsCanFocus(false);
        mListView.setMultiChoiceModeListener(multiChoiceModeListener);
        mListView.setOnItemClickListener(itemClickListener);

        getListFiles(null);
        //////////////////////////////////////////
        //getOption
        getOptionCaptureMode();
        currentRecordState = recordState.STOP_RECORDING;
    }

    private void setupViews() {
        setContentView(R.layout.recordvideo_layout);
        ////////////////
        adapter = new CustomListAdapter(this, itemInfo, itemBitmap);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setVisibility(View.INVISIBLE);


        if (mediaType.equals(IMAGE)) {
            getSupportActionBar().setTitle(R.string.camera_image_gallery);
        } else {
            getSupportActionBar().setTitle(R.string.camera_video_gallery);
        }
        ////////////////


        getSupportActionBar().setTitle(R.string.recording);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Button buttonRecordingStatus = (Button) findViewById(R.id.button_recordingStatus);
        //buttonRecordingStatus.setOnClickListener(this);

        buttonRecording = (Button) findViewById(R.id.button_startVideo);
        buttonRecording.setOnClickListener(this);

        buttonStop = (Button) findViewById(R.id.button_stopVideo);
        buttonStop.setOnClickListener(this);

        //buttonLiveSnapShot = (Button) findViewById(R.id.button_liveSnapShot);
        //buttonLiveSnapShot.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.button_recordingStatus:
//                getRecordingStatus();
//                break;
            case R.id.button_startVideo:
                startVideo();
                break;
            case R.id.button_stopVideo:
                stopVideo();

                // 여기서 바로 0번째 다운로드
                //getFullFile(0);
                break;
//            case R.id.button_liveSnapShot:
//                liveSnapShot();
//                break;
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

    /**
     * start/resume/pause recording video
     */
    private void startVideo() {
        //mProgressDialog = ProgressDialog.show(mContext, "", "Waiting..", true, false);
        if (currentRecordState == recordState.STOP_RECORDING) {
            changeRecordingStatus(START);
        } else if (currentRecordState == recordState.PAUSE_RECORDING) {
            changeRecordingStatus(RESUME);
        } else {  //is recording
            changeRecordingStatus(PAUSE);
        }
    }

    /**
     * stop recording video
     */
    private void stopVideo() {
        //Stop Recording
        //mProgressDialog = ProgressDialog.show(mContext, "", "Waiting..", true, false);
        Global.GlobalisUpload = false;
        Log.d("isUpload ::" , Global.GlobalisUpload.toString());
        changeRecordingStatus(STOP);
        buttonRecording.setBackgroundResource(R.color.ColorBlack);
    }

    private void handleResponse(String fileName, String localUri) {
        //Set downloading flag as false
        //Update android gallery
        startDownloading = false;
        currentDownloadFile = "";
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + (String) localUri)));
        Toast.makeText(mContext, fileName + " is saved", Toast.LENGTH_SHORT).show();
        //mProgressDialog.cancel();
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
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "RecordVideo onResume");
        if (!WifiReceiver.isConnected()) {
            ((RecordVideoActivity) mContext).finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentRecordState != recordState.STOP_RECORDING) {
            changeRecordingStatus(STOP);
        }
        try {                                           // 레코딩 액티비티에서 뒤로가기 했을때 런타임 에러떠서 추가함
            mHandler.removeCallbacks(mRunnable);
        }catch (RuntimeException e){
            Log.d("Exception:: ","런타임에러");
            e.printStackTrace();
        }

        Global.GlobalFlag=0;
        Log.d("GlobalFlag ::",""+Global.GlobalFlag);
        Log.v(TAG, "RecordVideo onDestroy");
    }

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

                            Log.d("123123::",command);
                            Log.d("3333",""+ Boolean.toString(command.equals("camera.stopCapture")));


                            if(command.equals("camera.stopCapture")==true)
                            {
                                mRunnable = new Runnable() {            //  동영상 정지 누르면 동영상 저장을 위해 5초 후에 액티비티 전환 후 자동 저장
                                    @Override
                                    public void run() {
                                        Intent i;
                                        i = new Intent(mContext, CameraFileListViewActivity.class);
                                        i.putExtra("type", "video");
                                        startActivity(i);
                                        finish();
                                        //mProgressDialog = ProgressDialog.show(mContext, "", "Downloading...", true, false);
                                    }
                                };

                                mHandler = new Handler();
                                mHandler.postDelayed(mRunnable, 3000); // 3초 후에 실행
                            }

                            return;
                        } else {
                            // state == done
                            setRecordingState(command);
                            /*if (mProgressDialog.isShowing())
                                mProgressDialog.cancel();*/
                        }
                    }
                }
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
//                        if (mProgressDialog.isShowing())
//                            mProgressDialog.cancel();
//                        Utils.showTextDialog(mContext, getString(R.string.response),
//                                Utils.parseString(response));
                        updateUIBasedOnResponse(response);
                    }
                } else {
//                    Utils.showTextDialog(mContext, getString(R.string.response),
//                            Utils.parseString(response));
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
            buttonRecording.setBackgroundResource(R.color.ColorBlack);

        } else if (currentRecordState == recordState.PAUSE_RECORDING) {
            buttonRecording.setText(R.string.resume_recording);
        } else {   //IS_RECORDING
            buttonRecording.setText(R.string.pause_recording);
            buttonRecording.setBackgroundResource(R.color.red);
        }
    }



    ///////////////////////////////////
    /**
     * Item click listener for an item selected
     * Show dialog for the selected row item
     */
    AdapterView.OnItemClickListener itemClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    CharSequence[] list = new CharSequence[]{"Show Information", "Get File", "Delete"};

                    Utils.showListDialog(mContext, list, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0: {
                                    getFileMetadata(position);
                                    Log.d("testttttttt",""+position);
                                    // Log.d("testtestest",itemInfo)
                                    break;
                                }
                                case 1: {
                                    getFullFile(position);
                                    Log.d("test::::::",""+position);
                                    break;
                                }

                                case 2: {
                                    int[] selectedPosition = new int[1];
                                    selectedPosition[0] = position;
                                    deleteFilesFromCamera(selectedPosition, false);
                                }
                                default:
                                    break;
                            }
                        }
                    });
                }
            };

    /**
     * Listener for multiple choice mode
     */
    private AbsListView.MultiChoiceModeListener multiChoiceModeListener =
            new AbsListView.MultiChoiceModeListener() {
                private int nr = 0; //The number of selected items

                /**
                 * get View for selected row
                 * @param pos position of the selected row view
                 * @param listView list View which includes the selected row
                 * @return View row view at the position
                 */
                //Get row view at selected position
                private View getViewByPosition(int pos, ListView listView) {
                    final int firstListItemPosition = listView.getFirstVisiblePosition();
                    final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

                    if (pos < firstListItemPosition || pos > lastListItemPosition) {
                        return listView.getAdapter().getView(pos, null, listView);
                    } else {
                        final int childIndex = pos - firstListItemPosition;
                        return listView.getChildAt(childIndex);
                    }
                }

                /**
                 * Set background color for selected row                 *
                 */
                private void setRowViewBackgrounds() {
                    SparseBooleanArray checked = mListView.getCheckedItemPositions();
                    int colorHighlight = ContextCompat.getColor(mContext, R.color.colorHighlight);
                    int colorTransparent = ContextCompat.getColor(mContext, R.color.colorTransparent);
                    for (int i = 0; i < checked.size(); i++) {
                        int position = checked.keyAt(i);
                        Log.v(TAG, "position = " + checked.keyAt(i) + " i = " + i
                                + " value = " + checked.valueAt(i));

                        //Get row view
                        View rowView = getViewByPosition(position, mListView);
                        if (checked.valueAt(i)) {
                            rowView.setBackgroundColor(colorHighlight);
                        } else {
                            rowView.setBackgroundColor(colorTransparent);
                        }
                    }
                }

                @Override
                public void onItemCheckedStateChanged
                        (ActionMode mode, int position, long id, boolean checked) {
                    int mCheckedCount = mListView.getCheckedItemCount();

                    //Count the number of checked items
                    if (checked) {
                        nr++;
                    } else {
                        nr--;
                    }
                    String title = (mCheckedCount > 0) ? (nr + " selected") : (0 + " selected");

                    mode.setTitle(title);
                    setRowViewBackgrounds();
                }


                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    ((Activity) mContext).getMenuInflater().inflate(
                            R.menu.contextual_actions, menu);
                    //Removing the default item click listener
                    mListView.setOnItemClickListener(null);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                    int id = item.getItemId();
                    switch (id) {
                        //Delete all files from camera
                        case R.id.action_delete_all:
                            String message = "Do you want to delete all image files?";
                            DialogInterface.OnClickListener deleteAllListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteFilesFromCamera(null, true);
                                    nr = 0;
                                    mode.setTitle(0 + " selected");
                                }
                            };

                            Utils.showSelectDialog(mContext, null, message, deleteAllListener, null);
                            return true;

                        //Delete selected files from camera
                        case R.id.action_delete:
                            SparseBooleanArray checked = mListView.getCheckedItemPositions();
                            int[] selectedPosition = new int[checked.size()];

                            for (int i = 0; i < checked.size(); i++) {
                                if (checked.valueAt(i)) {
                                    selectedPosition[i] = checked.keyAt(i);
                                    mListView.setItemChecked(selectedPosition[i], false);
                                }
                            }
                            deleteFilesFromCamera(selectedPosition, false);
                            nr = 0;
                            return true;

                        default:
                            Log.v(TAG, "ERROR");
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    nr = 0;
                    mListView.setOnItemClickListener(itemClickListener);
                }

            };
    /**
     * Convert file info in JSONObject into HashMap
     * @param fileInfo information of a file (JSONObject)
     * @return information of a file (HashMap)
     */
    private HashMap<String, String> makeFileInfoMap(JSONObject fileInfo) {
        HashMap<String, String> info = new HashMap<>();

        Iterator it = fileInfo.keys();
        while (it.hasNext()) {
            try {
                String key = (String) it.next();
                Object tempValue = fileInfo.get(key);
                String value = tempValue.toString();
                info.put(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return info;
    }

    /**
     * Parse response of camera.listFiles request
     * Save file info in hash map
     *
     * if continuation token exists in response, then call getListFiles method
     * if not, update list adapter to show results
     * @param data response of camera.listFiles request
     */
    private void updateList(String data) {
        try {
            JSONObject jObject = new JSONObject(data);

            JSONObject resultData = jObject.getJSONObject(OSCParameterNameMapper.RESULTS);
            JSONArray entries = resultData.getJSONArray(OSCParameterNameMapper.ENTRIES);
            for (int i = 0; i < entries.length(); i++) {
                //Parse file info and save info in hash map
                JSONObject fileInfo = entries.getJSONObject(i);

                HashMap<String, String> info = makeFileInfoMap(fileInfo);
                //Set dummy value for bitmapId
                adapter.addItem(info, -1);
            }

            if (resultData.has(OSCParameterNameMapper.CONTINUATION_TOKEN)) {
                //if continuation token exists call the get list files
                //to get remaining list
                String token = resultData.getString(OSCParameterNameMapper.CONTINUATION_TOKEN);

                Log.v(TAG, "Current token = " + token);

                getListFiles(token);


            } else {
                Log.d(TAG, "Token END");
                // Update lists
                adapter.notifyDataSetChanged();
                //if (mProgressDialog.isShowing())
               //     mProgressDialog.cancel();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * Get file lists from camera
     * API: /osc/commands/execute (camera.listFiles)
     * @param token continuation token from camera response.
     */
    private void getListFiles(String token) {
        JSONObject parameters = new JSONObject();
        try {
            //Set parameter values
            parameters.put(OSCParameterNameMapper.ENTRYCOUNT, entryCount);
            parameters.put(OSCParameterNameMapper.MAXTHUMBSIZE, null);

            //Set fileType parameter (image or video)
            if (mediaType.equals(IMAGE)) {
                parameters.put(OSCParameterNameMapper.FILETYPE, IMAGE);
            } else if (mediaType.equals(VIDEO)) {
                parameters.put(OSCParameterNameMapper.FILETYPE, VIDEO);
            }

            Log.v(TAG, "get list token = " + token);
            if (token != null) {
                //Set continuation token if it exists
                parameters.put(OSCParameterNameMapper.CONTINUATION_TOKEN, token);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (parameters != null) {
            OSCCommandsExecute commandsExecute = null;
            commandsExecute = new OSCCommandsExecute("camera.listFiles", parameters);

            if (commandsExecute == null) {
                Log.v(TAG, "ERROR: media type error");
                return;
            }

            commandsExecute.setListener(new HttpAsyncTask.OnHttpListener() {
                @Override
                public void onResponse(HttpAsyncTask.OnHttpListener.OSCReturnType type, Object response) {
                    if (type == OSCReturnType.SUCCESS) {
                        updateList((String) response);
                    } else {
                        Utils.showTextDialog(mContext, getString(R.string.response), Utils.parseString(response));
                    }
//                    if(Global.GlobalFlag!=0)
//                    {
//                        getFullFile(0);
//                    }
                }
            });
            commandsExecute.execute();
        }
    }

    /**
     * Delete selected files
     * API: /osc/commands/execute (camera.delete)
     * @param positions positions of the selected row
     * @param removeAll removeAll flag. True = remove All / False = remove only selected items
     */
    private void deleteFilesFromCamera(final int[] positions, final boolean removeAll) {
        String[] fileUrls;

        if (removeAll) {
            //Set parameter for remove all
            fileUrls = new String[1];
            if (mediaType.equals(IMAGE))
                fileUrls[0] = "image";
            else if (mediaType.equals(VIDEO))
                fileUrls[0] = "video";
        } else {
            //Set fileUrls parameter with file urls of selected items
            int len = positions.length;
            fileUrls = new String[len];
            for (int i = 0; i < len; i++) {
                fileUrls[i] = adapter.getInfo(positions[i], OSCParameterNameMapper.FileInfo.URL);
            }
        }

        JSONObject parameters = new JSONObject();
        try {
            JSONArray urlsParameter = new JSONArray(fileUrls);
            parameters.put(OSCParameterNameMapper.FILEURLS, urlsParameter);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final OSCCommandsExecute cmdExecute = new OSCCommandsExecute("camera.delete", parameters);
        cmdExecute.setListener(new HttpAsyncTask.OnHttpListener() {
            @Override
            public void onResponse(HttpAsyncTask.OnHttpListener.OSCReturnType type, Object response) {
                if (type == OSCReturnType.SUCCESS) {
                    //Update adapter
                    if (removeAll) {
                        adapter.removeAllItems();
                    } else {
                        adapter.removeItems(positions);
                    }
                    adapter.notifyDataSetChanged();
                }

                if (mProgressDialog.isShowing()) {
                    mProgressDialog.cancel();
                }
                Utils.showTextDialog(mContext, getString(R.string.response), Utils.parseString(response));
            }
        });
        cmdExecute.execute();
        mProgressDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
    }
    /**
     * Get metadata of a selected file
     * API: /osc/commands/execute (camera.getMetadata)
     * @param position a position of the selected row
     */
    private void getFileMetadata(int position) {
        String url = adapter.getInfo(position, OSCParameterNameMapper.FileInfo.URL);
        JSONObject parameters = new JSONObject();
        try {
            parameters.put(OSCParameterNameMapper.FILEURL, url);

            final OSCCommandsExecute commandsExecute = new OSCCommandsExecute("camera.getMetadata", parameters);

            commandsExecute.setListener(new HttpAsyncTask.OnHttpListener() {
                @Override
                public void onResponse(HttpAsyncTask.OnHttpListener.OSCReturnType type, Object response) {
                    //Show response in Dialog
                    Utils.showTextDialog(mContext, getString(R.string.response), Utils.parseString(response));
                }
            });
            commandsExecute.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get full-size video or full-size image from camera
     * API: /osc/commands/execute (camera.getFile)
     * @param position  a position of the selected row
     */
    private void getFullFile(final int position) {
        currentDownloadFile = adapter.getInfo(position, OSCParameterNameMapper.FileInfo.NAME);
        JSONObject parameters = new JSONObject();
        try {
            String url = adapter.getInfo(position, OSCParameterNameMapper.FileInfo.URL);
            parameters.put(OSCParameterNameMapper.FILEURL, url);

            OSCCommandsExecute commandsExecute;

            //Set the data type for request (image or video)
            //It will set different http request header property
            if (mediaType.equals(IMAGE)) {
                commandsExecute = new OSCCommandsExecute("camera.getFile", parameters,
                        OSCCommandsExecute.CommandType.IMAGE);
            } else if (mediaType.equals(VIDEO)) {
                commandsExecute = new OSCCommandsExecute("camera.getFile", parameters,
                        OSCCommandsExecute.CommandType.VIDEO);
            } else {
                Log.d(TAG, "Media type should be image or video");
                return;
            }
            commandsExecute.setListener(new HttpAsyncTask.OnHttpListener() {
                @Override
                public void onResponse(HttpAsyncTask.OnHttpListener.OSCReturnType type, Object response) {
                    if (type == OSCReturnType.SUCCESS) {
                        //Get binary data from camera and save successfully
                        //Response of getFile is the fileUri of the saved file
                        String name = adapter.getInfo(position, OSCParameterNameMapper.FileInfo.NAME);
                        handleResponse(name, (String) response);
                    } else {
                        Utils.showTextDialog(mContext, getString(R.string.response), Utils.parseString(response));
                    }
                }
            });
            commandsExecute.execute();
            //Set downloading flag as true
            startDownloading = true;
            //mProgressDialog = ProgressDialog.show(mContext, "", "Downloading...", true, false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete the last downloaded file
     * Call this method when the downloading ends unsuccessfully
     * (ex) loose connection during downloading
     */
    private void deleteUnfinishedDownloadFile() {
        String mTargetDirectory = OSCCommandsExecute.getFileLocation();
        String fileUri = mTargetDirectory + "/" + currentDownloadFile;
        File file = new File(fileUri);
        if (file.exists()) {
            file.delete();
        }
    }

    ///////////////////////////////////
}
