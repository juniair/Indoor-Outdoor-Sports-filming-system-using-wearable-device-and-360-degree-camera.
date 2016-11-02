package com.lge.friendsCamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

public class CaddyActivity extends Activity implements View.OnClickListener {
    public int np1_num = 0;
    public int np2_num = 0;
    public int np3_num = 0;
    public int num_sum =0;
    private int pos;
    public String arrayCaddyIdxToSting[] = new String[99];
    public int arrayCaddyIdx[] = new int[99];
    public String arrayCaddyName[] = new String[99];

    String myJSON;

    private static final String TAG_RESULTS="result";
    private static final String TAG_ID = "idx";
    private static final String TAG_NAME = "name";

    JSONArray peoples = null;

    ArrayList<HashMap<String, String>> personList;

    ListView list;
    public String a,b;
    public int aa;
    ////////////////////////////////
    @Override
    // 회원번호 NumberPicker로 설정
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.caddy_layout);
//        Intent intent = getIntent();
//        String send[] = intent.getStringArrayExtra("UserNumAndName");   // send[0]은 회원 인덱스, send[1]은 회원 이름
//        Global.GlobalUserIdx=send[0];
//        Global.GlobalUserName=send[1];
        ////////////////////////
        list = (ListView) findViewById(R.id.listView);
        personList = new ArrayList<HashMap<String,String>>();
        getData();
        ////////////////////////
        //setContentView(R.layout.alert_layout);
        ///////////////////////////////////////////
        Button btn = (Button) findViewById(R.id.button);
        Button btn_quit = (Button) findViewById(R.id.button_quit);
        btn_quit.setOnClickListener(this);
        btn.setOnClickListener(this);

        //Get the widgets reference from XML layout
        final TextView tv = (TextView) findViewById(R.id.tv);
        //final TextView sum = (TextView) findViewById(R.id.sum);
        //NumberPicker np1 = (NumberPicker) findViewById(R.id.np1);
        NumberPicker np2 = (NumberPicker) findViewById(R.id.np2);
        NumberPicker np3 = (NumberPicker) findViewById(R.id.np3);
        //Set TextView text color
        //tv.setTextColor(Color.parseColor("#ffd32b3b"));

        //Populate NumberPicker values from minimum and maximum value range
        //Set the minimum value of NumberPicker
        //np1.setMinValue(0);
        np2.setMinValue(0);
        np3.setMinValue(0);
        //Specify the maximum value/number of NumberPicker
        //np1.setMaxValue(9);
        np2.setMaxValue(9);
        np3.setMaxValue(9);
        //Gets whether the selector wheel wraps when reaching the min/max value.
        //np1.setWrapSelectorWheel(true);
        np2.setWrapSelectorWheel(true);
        np3.setWrapSelectorWheel(true);
        //Set a value change listener for NumberPicker
//        np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
//            @Override
//            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                //Display the newly selected number from picker
//                //tv.setText("Selected Number : " + newVal);
//                np1_num = newVal * 100;
//                num_sum = np1_num + np2_num + np3_num;
//                Log.d("num_sum==", "" + num_sum);
//                //sum.setText("회원번호 : " + num_sum);
//            }
//        });
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
        g.execute("http://218.150.183.1/moons/mobile/caddy_login.php");
    }

    protected void showList() {
        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            peoples = jsonObj.getJSONArray(TAG_RESULTS);

            for (int i = 0; i < peoples.length(); i++) {
                JSONObject c = peoples.getJSONObject(i);

                String id = c.getString(TAG_ID);
                String name = c.getString(TAG_NAME);
                arrayCaddyName[i]=name;
                arrayCaddyIdx[i]= Integer.parseInt(id);       // arrayIdx 배열에 숫자로 회원번호 저장
                arrayCaddyIdxToSting[i] = id;               // arrayIdxToString 배열에 문자로 회원번호 저장
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
            // 잘 들어갔나 확인한 로그
//            Log.d("arrayName 1 = ",""+arrayName[0]);
//            Log.d("arrayName 2 = ",""+arrayName[1]);
//            Log.d("arrayIdx 1 = ",""+arrayIdx[0]);
//            Log.d("arrayIdx 2 = ",""+arrayIdx[1]);


            ListAdapter adapter = new SimpleAdapter(
                    CaddyActivity.this, personList, R.layout.list_item,
                    new String[]{TAG_ID, TAG_NAME},
                    new int[]{R.id.idx, R.id.name}
            );

            list.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    /////////////////////////////////////////////////
    public void change(){

        //intent.putExtra("userIdx",arrayName[num_sum-1]);


        ///////////////////////////


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
                Log.d("jh",result);
                myJSON = result;
                errorCheck();
            }
        }
        GetDataJSON g = new GetDataJSON();
        // GET 방식 작성법
        // http://서버주소/디텍토리/.../파일명.php?변수1=변수값1&변수2=변수값2&...&변수n=변수값n
        //0816 g.execute("http://192.168.0.26/test1/testing/test_select_caddy.php?userIdx="+Global.GlobalUserIdx+"&caddyIdx="+Global.GlobalCaddyIdx);
        ///////////////////////////



    }

    private void errorCheck()
    {
        try {
            JSONObject jsonObj = new JSONObject(myJSON);

            boolean isError = jsonObj.getBoolean("error");

            if(isError == false)
            {
                Intent intent1 = new Intent(this, MainActivity.class);

//                String give[] = {Global.GlobalUserIdx,Global.GlobalUserName,arrayCaddyIdxToSting[num_sum-1],arrayCaddyName[num_sum-1]};       // 배열로 입력한 회원번호와 회원이름을 넘겨준다.
//                intent1.putExtra("CaddyAndUser",give);
                Log.d("is Error= true,,,,,,",Global.GlobalUserIdx);
                startActivity(intent1);
                finish();
                return;
            }
            else {
                Log.d("is Error= false,,,,,,",
                        Global.GlobalUserIdx);
                String error_msg = jsonObj.getString("error_msg");
                Toast.makeText(getApplicationContext(), error_msg, Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void alertButton()
    {
        boolean isFind = false;

        for(int i = 0; i < arrayCaddyIdx.length; i++)
        {
            if(num_sum == arrayCaddyIdx[i]) {
                isFind = true;
                pos = i;
                //0816
//                Global.GlobalCaddyIdx = arrayCaddyIdxToSting[pos];
//                Global.GlobalCaddyName = arrayCaddyName[pos];
                break;
            }
        }
        if(isFind == true  && num_sum != 0)          // 넣은 값이 배열에 있으면 ( 회원번호가 존재하면 )
        {

            new AlertDialog.Builder(this)
                    .setMessage(num_sum +"번 "+ arrayCaddyName[pos]+  " 캐디가 맞습니까?")
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
                    .setMessage("캐디번호가 존재하지 않습니다!")
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
    ////////////////////////////////////////////////

//    private void alertButton()
//    {
//        if(num_sum == arrayCaddyIdx[num_sum-1])          // 넣은 값이 배열에 있으면 ( 회원번호가 존재하면 )
//        {
//            new AlertDialog.Builder(this)
//                    .setMessage("회원번호: "+num_sum + arrayCaddyName[num_sum-1]+  "회원이 맞습니까?")
//                    .setPositiveButton("확인", new DialogInterface.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which)          // 회원번호와 이름이 맞으면 확인을 눌러 회원번호를 가지고 다음 메인화면으로 넘어감
//                        {
//                            change();
//
//                            //alertButton2();
//                        }
//                    })
//                    .setNegativeButton("취소", new DialogInterface.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which)
//                        {
//                            dialog.dismiss();
//                        }
//                    })
//                    .show();
//        }
//        else                // 입력한 회원번호가 존재하지 않으면
//        {
//            new AlertDialog.Builder(this)
//                    .setMessage("회원번호가 존재하지 않습니다!")
//                    .setPositiveButton("확인", new DialogInterface.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which)
//                        {
//                            //dialog.dismiss();
//                            dialog.dismiss();
//                        }
//                    })
//                    .show();
//        }
//
//
//    }
    private void alertButton2()
    {
        new AlertDialog.Builder(this)

                .setMessage("확인 클릭")
                .setPositiveButton("확인", new DialogInterface.OnClickListener()
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
            case R.id.button_quit:
                finish();
                break;
        }
    }
}