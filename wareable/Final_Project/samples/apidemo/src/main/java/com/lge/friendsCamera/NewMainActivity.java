package com.lge.friendsCamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class NewMainActivity extends Activity implements View.OnClickListener {
    public int userID = 0;      // 유저 아이디
    public int np1_num = 0;
    public int np2_num = 0;
    public int np3_num = 0;
    public int num_sum =0;

    private int pos;
    public Context mContext = this;

    public int arrayIdx[] = new int[999];
    public String arrayIdxToString[] = new String[999];
    public String arrayName[] = new String[999];
    ////////////////////////////////
    String myJSON;

    private static final String TAG_RESULTS="result";
    private static final String TAG_ID = "idx";
    private static final String TAG_NAME = "name";

    JSONArray peoples = null;

    ArrayList<HashMap<String, String>> personList;

    ListView list;
    public String a,b;
    public int aa;

    @Override
    // 회원번호 NumberPicker로 설정
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_layout);
        startActivity(new Intent(this,Splash.class));
        ////////////////////////
        list = (ListView) findViewById(R.id.listView);
        personList = new ArrayList<HashMap<String,String>>();
        getData();
        list.setVisibility(View.INVISIBLE); // 리스트 안보이게
        Button btn = (Button) findViewById(R.id.button);
        Button btn_exit = (Button)findViewById(R.id.button_exit);
        btn_exit.setOnClickListener(this);
        btn.setOnClickListener(this);

        //Get the widgets reference from XML layout
        final TextView tv = (TextView) findViewById(R.id.tv);
        //final TextView sum = (TextView) findViewById(R.id.sum);
        NumberPicker np1 = (NumberPicker) findViewById(R.id.np1);
        NumberPicker np2 = (NumberPicker) findViewById(R.id.np2);
        NumberPicker np3 = (NumberPicker) findViewById(R.id.np3);

        //Populate NumberPicker values from minimum and maximum value range
        //Set the minimum value of NumberPicker
        np1.setMinValue(0);
        np2.setMinValue(0);
        np3.setMinValue(0);
        //Specify the maximum value/number of NumberPicker
        np1.setMaxValue(9);
        np2.setMaxValue(9);
        np3.setMaxValue(9);
        //Gets whether the selector wheel wraps when reaching the min/max value.
        np1.setWrapSelectorWheel(true);
        np2.setWrapSelectorWheel(true);
        np3.setWrapSelectorWheel(true);
        //Set a value change listener for NumberPicker
        np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //Display the newly selected number from picker
                //tv.setText("Selected Number : " + newVal);
                np1_num = newVal * 100;
                num_sum = np1_num + np2_num + np3_num;
                Log.d("num_sum==", "" + num_sum);
                //sum.setText("회원번호 : " + num_sum);
            }
        });
        np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //Display the newly selected number from picker
                np2_num = newVal * 10;
                num_sum = np1_num + np2_num + np3_num;
                Log.d("num_sum==", "" + num_sum);
                //sum.setText("회원번호 : " + num_sum);
            }
        });
        np3.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //Display the newly selected number from picker
                np3_num = newVal * 1;
                num_sum = np1_num + np2_num + np3_num;
                Log.d("num_sum==", "" + num_sum);
                //sum.setText("회원번호 : " + num_sum);
            }
        });
    }

    /////////////////////////////////////////////////
    public void getData() {

        // 비동기식 내부 클래스
        class GetDataJSON extends AsyncTask<String, Void, String> {

            // 백그라운드에서 작동되는 함수
            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];
                BufferedReader bufferedReader;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    String json;
                    while((json = bufferedReader.readLine()) != null) {
                        sb.append(json+"\n");
                    }
                    return sb.toString().trim();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            // execute() 실행시 해당 함수가 호출
            @Override
            protected void onPostExecute(String result) {
                //Log.d("jh",result);
                myJSON = result;
                showList();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute("http://218.150.183.1/moons/mobile/check.php");
    }

    protected void showList() {
        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            peoples = jsonObj.getJSONArray(TAG_RESULTS);

            for (int i = 0; i < peoples.length(); i++) {
                JSONObject c = peoples.getJSONObject(i);

                String id = c.getString(TAG_ID);
                Log.d("test",id);
                String name = c.getString(TAG_NAME);
                arrayName[i]=name;
                arrayIdx[i]= Integer.parseInt(id);       // arrayIdx 배열에 숫자로 회원번호 저장
                arrayIdxToString[i] = id;               // arrayIdxToString 배열에 문자로 회원번호 저장
                HashMap<String, String > persons = new HashMap<String, String>();
                persons.put(TAG_ID, id);
                persons.put(TAG_NAME, name);
                if(i==0)
                {
                    a=id;
                    aa = Integer.parseInt(a)-1;
                    Log.d("a=",a);
                    Log.d("num_sum = ",""+num_sum);
                    Log.d("aa = ",""+aa);
                    if(num_sum==aa)
                    {
                        Log.d("same====",""+aa);
                    }
                }
                else
                {
                    b=id;
                }

                personList.add(persons);
            }
            ListAdapter adapter = new SimpleAdapter(
                    NewMainActivity.this, personList, R.layout.list_item,
                    new String[]{TAG_ID, TAG_NAME},
                    new int[]{R.id.idx, R.id.name}
            );

            list.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e){
            WifiReceiver.disconnectWifi(mContext);

            //  DISCONNECT할 때 다른 AP로 접속시킴
            WifiConfiguration wifiConfig = new WifiConfiguration();
//            wifiConfig.SSID = String.format("\"%s\"", "NGN_LAB");
//            wifiConfig.preSharedKey = String.format("\"%s\"", "NGNNGN10");
            wifiConfig.SSID = String.format("\"%s\"", "Gyungsoo");
            wifiConfig.preSharedKey = String.format("\"%s\"", "tn1258963");
            Toast.makeText(mContext," NGN_LAB연결됨!!", Toast.LENGTH_SHORT).show();
            WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
            int netId = wifiManager.addNetwork(wifiConfig);
            //wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
            //startActivity(new Intent(this,NewMainActivity.class));

        }

    }
    /////////////////////////////////////////////////
    public void change(){
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);
    }

    private void alertButton()
    {
        boolean isFind = false;
        for(int i = 0; i < arrayIdx.length; i++)
        {
            if(num_sum == arrayIdx[i]) {
                isFind = true;
                pos = i;
                Global.GlobalUserIdx = arrayIdxToString[pos];
                Global.GlobalUserName = arrayName[pos];
                break;
            }
        }
        if(isFind == true && num_sum != 0)          // 넣은 값이 배열에 있으면 ( 회원번호가 존재하면 )
        {
            new AlertDialog.Builder(this)
                    .setMessage(num_sum +"번 "+ arrayName[pos]+  " 회원이 맞습니까?")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)          // 회원번호와 이름이 맞으면 확인을 눌러 회원번호를 가지고 다음 메인화면으로 넘어감
                        {
                            change();

                            //alertButton2();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
        else if(isFind == false || num_sum == 0)                // 입력한 회원번호가 존재하지 않으면
        {
            new AlertDialog.Builder(this)
                    .setMessage("회원번호가 존재하지 않습니다!")
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
    private void alertButton2()
    {
        new AlertDialog.Builder(this)
                .setMessage("종료하시겠습니까? ")
                .setPositiveButton("확인", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)          // 회원번호와 이름이 맞으면 확인을 눌러 회원번호를 가지고 다음 메인화면으로 넘어감
                    {
                        System.runFinalizersOnExit(true);

                        System.exit(0);

                        //alertButton2();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .show();
    }
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.button:
                alertButton();
                break;
            case R.id.button_exit:
                alertButton2();
                break;
        }
    }
}