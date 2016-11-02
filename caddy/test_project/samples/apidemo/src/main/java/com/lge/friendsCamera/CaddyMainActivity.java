//package com.lge.friendsCamera;
//
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.graphics.Typeface;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ListAdapter;
//import android.widget.ListView;
//import android.widget.SimpleAdapter;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.HashMap;
//
///**
// * Created by 김재환 on 2016-08-08.
// */
//public class CaddyMainActivity extends AppCompatActivity implements View.OnClickListener{
//    String myJSON;
//    EditText editText1;
//    private static final String TAG_RESULTS="result";
//    private static final String TAG_ID = "idx";
//    private static final String TAG_NAME = "name";
//
//    JSONArray peoples = null;
//    JSONArray userArray = null;
//    ArrayList<HashMap<String, String>> personList;
//
//    ListView list;
//    public String a,b;
//    public int aa;
//
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.caddy_input_layout);
//        Global.GlobalForSendIdx = 0;    // 송신할때 같이 보내는 회원인덱스 초기화
//        Log.d("GlobalForSendIdx Main: ",""+Global.GlobalForSendIdx);
//        startActivity(new Intent(this,Splash.class));
//        Log.v("알림", "메세지");
//
//
//        getData();
//        Button btn = (Button) findViewById(R.id.button);
//        btn.setOnClickListener(this);
//
//        Log.d("test","message");
//
//
//
//        Typeface typeFace = Typeface.createFromAsset(getAssets(), "Hanoded-Heavy.ttf");
//        TextView textView = (TextView) findViewById(R.id.tv);
//        textView.setTypeface(typeFace);
//
//
//        editText1 = (EditText) findViewById(R.id.editText2_2);
//
//        Log.d("num_sum==", "" + Caddy_Global.num_sum);
//        Log.d("알림", "메세지");
//
//    }
//
//
//    public void getData() {
//
//        // 비동기식 내부 클래스
//        class GetDataJSON extends AsyncTask<String, Void, String> {
//
//            // 백그라운드에서 작동되는 함수
//            @Override
//            protected String doInBackground(String... params) {
//                String uri = params[0];
//
//                BufferedReader bufferedReader;
//                try {
//                    URL url = new URL(uri);
//                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//                    StringBuilder sb = new StringBuilder();
//                    Log.d("test",sb.toString());
//                    bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//
//                    String json;
//                    while((json = bufferedReader.readLine()) != null) {
//                        sb.append(json+"\n");
//                    }
//
//                    return sb.toString().trim();
//
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                    return null;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//
//            // execute() 실행시 해당 함수가 호출
//            @Override
//            protected void onPostExecute(String result) {
////                Log.d("jh",result);
//                myJSON = result;
//                showList();
//            }
//        }
//        GetDataJSON g = new GetDataJSON();
//        g.execute("http://192.168.0.26/test1/testing/test_caddy_login.php");
//    }
//
//
//    protected void showList() {
//
//
//
//        try {
//            JSONObject jsonObj = new JSONObject(myJSON);
//            peoples = jsonObj.getJSONArray(TAG_RESULTS);
//
//            for (int i = 0; i < peoples.length(); i++) {
//                JSONObject c = peoples.getJSONObject(i);
//
//                String id = c.getString(TAG_ID);
//                String name = c.getString(TAG_NAME);
//                Caddy_Global.arrayName[i]=name;
//                Caddy_Global.arrayIdx[i]= Integer.parseInt(id);       // arrayIdx 배열에 숫자로 회원번호 저장
//                Caddy_Global.arrayIdxToString[i] = id;               // arrayIdxToString 배열에 문자로 회원번호 저장
//
//                HashMap<String, String > persons = new HashMap<String, String>();
//                persons.put(TAG_ID, id);
//                persons.put(TAG_NAME, name);
//                if(i==0)
//                {
//                    a=id;
//                    aa = Integer.parseInt(a)-1;
//                    Log.d("a=",a);
//                    Log.d("num_sum = ",""+Caddy_Global.num_sum);
//                    Log.d("aa = ",""+aa);
//                    if(Caddy_Global.num_sum==aa)
//                    {
//                        Log.d("same====",""+aa);
//                    }
//                }
//                else
//                {
//                    b=id;
//                }
//
//                //personList.add(persons);
//            }
//            // 잘 들어갔나 확인한 로그
//            Log.d("arrayName 1 = ",""+Caddy_Global.arrayName[0]);
//            Log.d("arrayName 2 = ",""+Caddy_Global.arrayName[1]);
//            Log.d("arrayIdx 1 = ",""+Caddy_Global.arrayIdx[0]);
//            Log.d("arrayIdx 2 = ",""+Caddy_Global.arrayIdx[1]);
//
//
//            ListAdapter adapter = new SimpleAdapter(
//                    CaddyMainActivity.this, personList, R.layout.list_item,
//                    new String[]{TAG_ID, TAG_NAME},
//                    new int[]{R.id.idx, R.id.name}
//            );
//
////            list.setAdapter(adapter);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public void onClick(View v)
//    {
//        switch(v.getId())
//        {
//            case R.id.button:
//                Caddy_Global.num_sum = Integer.parseInt(editText1.getText().toString());
//                alertButton();
//                break;
//        }
//    }
//
//    public void change(){
////        Intent intent = new Intent(this, Activity_index2.class);
////
////        String give[] = {arrayIdxToString[num_sum-1],arrayName[num_sum-1]};       // 배열로 입력한 회원번호와 회원이름을 넘겨준다.
////        intent.putExtra("UserNumAndName",give);
//
//
//        ///////////////////////////////////////////
//
//        class GetDataJSON extends AsyncTask<String, Void, String> {
//
//            // 백그라운드에서 작동되는 함수
//            @Override
//            protected String doInBackground(String... params) {
//                String uri = params[0];
//
//                BufferedReader bufferedReader;
//                try {
//                    URL url = new URL(uri);
//                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//                    StringBuilder sb = new StringBuilder();
//                    Log.d("test",sb.toString());
//                    bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//
//                    String json;
//                    while((json = bufferedReader.readLine()) != null) {
//                        sb.append(json+"\n");
//                    }
//                    return sb.toString().trim();
//
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                    return null;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//
//            // execute() 실행시 해당 함수가 호출
//            @Override
//            protected void onPostExecute(String result) {
//                Log.d("jh",result);
//                myJSON = result;
//                check_userNum();
//            }
//        }
//
//        GetDataJSON g = new GetDataJSON();
//        g.execute("http://192.168.0.26/test1/testing/test_select_player.php?caddyIdx="+Caddy_Global.num_sum);
//        Log.d("caddyIdx==",""+Caddy_Global.num_sum);
//
//        ///////////////////////////////////////////
//
//
//
//    }
//
//    private void check_userNum()
//    {
//        try {
//            JSONObject jsonObj = new JSONObject(myJSON);
//
//            boolean isError = jsonObj.getBoolean("error");
//            int totaluser = jsonObj.getInt("totalUser");
//            userArray= jsonObj.getJSONArray("userInfo");
//
//            for(int i =0; i < totaluser;i++)
//            {
//                JSONObject c = userArray.getJSONObject(i);
//                String idx = c.getString(TAG_ID);
//                String name = c.getString(TAG_NAME);
//                Caddy_Global.arrayUserInfo_idx[i] = idx;
//                Caddy_Global.arrayUserInfo_name[i] = name;
//                Log.d("idx = name ==== ", idx+name);
//            }
//
//
//            Log.d("error",""+isError);
//            Log.d("Totaluser = ",""+totaluser);
//            if(isError == false)
//            {
//                if(totaluser==2){
//                    Log.d("Success","totaluser====2");
//                    Intent intent = new Intent(this, Caddy_Activity_index2.class);
//
//                    String give[] = {Caddy_Global.arrayIdxToString[Caddy_Global.num_sum - 1], Caddy_Global.arrayName[Caddy_Global.num_sum - 1]};       // 배열로 입력한 회원번호와 회원이름을 넘겨준다.
//                    intent.putExtra("UserNumAndName", give);
//
//                    intent.putExtra("id", Caddy_Global.arrayIdxToString[Caddy_Global.num_sum - 1]);
//                    intent.putExtra("name", Caddy_Global.arrayName[Caddy_Global.num_sum - 1]);
//
//                    intent.putExtra("idx",Caddy_Global.arrayUserInfo_idx);
//                    intent.putExtra("uname",Caddy_Global.arrayUserInfo_name);
//
//
//                    //intent.putStringArrayListExtra("userid" ,<arrayUserInfo_idx>);
//                    Log.d("is Error= true,,,,,,", "" + Caddy_Global.num_sum);
//                    startActivity(intent);
//                    finish();
//                    return;
//
//                }
//                else if(totaluser==3){
//                    Log.d("Success","totaluser====3");
//                    Intent intent = new Intent(this, Caddy_Activity_index3.class);
//
//                    String give[] = {Caddy_Global.arrayIdxToString[Caddy_Global.num_sum - 1], Caddy_Global.arrayName[Caddy_Global.num_sum - 1]};       // 배열로 입력한 회원번호와 회원이름을 넘겨준다.
//                    intent.putExtra("UserNumAndName", give);
//
//                    intent.putExtra("id", Caddy_Global.arrayIdxToString[Caddy_Global.num_sum - 1]);
//                    intent.putExtra("name", Caddy_Global.arrayName[Caddy_Global.num_sum - 1]);
//
//                    intent.putExtra("idx",Caddy_Global.arrayUserInfo_idx);
//                    intent.putExtra("uname",Caddy_Global.arrayUserInfo_name);
//
//
//                    //intent.putStringArrayListExtra("userid" ,<arrayUserInfo_idx>);
//                    Log.d("is Error= true,,,,,,", "" + Caddy_Global.num_sum);
//                    startActivity(intent);
//                    finish();
//                    return;
//                }
//                else if(totaluser==4) {
//                    Log.d("Success","totaluser====4");
//                    Intent intent = new Intent(this, Caddy_Activity_index4.class);
//
//                    String give[] = {Caddy_Global.arrayIdxToString[Caddy_Global.num_sum - 1], Caddy_Global.arrayName[Caddy_Global.num_sum - 1]};       // 배열로 입력한 회원번호와 회원이름을 넘겨준다.
//                    intent.putExtra("UserNumAndName", give);
//
//                    intent.putExtra("id", Caddy_Global.arrayIdxToString[Caddy_Global.num_sum - 1]);
//                    intent.putExtra("name", Caddy_Global.arrayName[Caddy_Global.num_sum - 1]);
//
//                    intent.putExtra("idx",Caddy_Global.arrayUserInfo_idx);
//                    intent.putExtra("uname",Caddy_Global.arrayUserInfo_name);
//
//
//                    //intent.putStringArrayListExtra("userid" ,<arrayUserInfo_idx>);
//                    Log.d("is Error= true,,,,,,", "" + Caddy_Global.num_sum);
//                    startActivity(intent);
//                    finish();
//                    return;
//                }
//
//            }
//            else {
//                Log.d("is Error= false,,,,,,",""+Caddy_Global.num_sum);
//                String error_msg = jsonObj.getString("error_msg");
//                Toast.makeText(getApplicationContext(), error_msg, Toast.LENGTH_SHORT).show();
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void alertButton()
//    {
//        boolean isFind = false;
//        int pos = 0;
//        for(int i = 0; i < Caddy_Global.arrayIdx.length; i++) {
//            if(Caddy_Global.num_sum == Caddy_Global.arrayIdx[i]) {
//                isFind = true;
//                pos = i;
//                break;
//            }
//        }
//        if(isFind == true)          // 넣은 값이 배열에 있으면 ( 회원번호가 존재하면 )
//        {
//            new AlertDialog.Builder(this)
//                    .setMessage("캐디번호 "+Caddy_Global.num_sum + "  " +Caddy_Global.arrayName[pos]+  " 회원이 맞습니까?")
//                    .setPositiveButton("확인", new DialogInterface.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which)          // 회원번호와 이름이 맞으면 확인을 눌러 회원번호를 가지고 다음 메인화면으로 넘어감
//                        {
//                            change();
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
//        else if(isFind==false || Caddy_Global.num_sum ==0)               // 입력한 회원번호가 존재하지 않으면
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
//}
package com.lge.friendsCamera;

/**
 * Created by NGN_LAB on 2016-07-14.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
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


public class CaddyMainActivity extends AppCompatActivity implements View.OnClickListener {
    public int userID = 0;      // 유저 아이디


    ///////////////////////////////
    String myJSON;
    EditText editText1;
    private static final String TAG_RESULTS="result";
    private static final String TAG_ID = "idx";
    private static final String TAG_NAME = "name";

    JSONArray peoples = null;
    JSONArray userArray = null;
    ArrayList<HashMap<String, String>> personList;

    ListView list;
    public String a,b;
    public int aa;
    ////////////////////////////////

    // Toolbar toolbar;

    Toolbar toolbar;
    DrawerLayout dlDrawer;
    ActionBarDrawerToggle dtToggle;


    ////////////////////////////////
    @Override
    // 회원번호 NumberPicker로 설정
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.caddy_input_layout);
        Global.GlobalForSendIdx = 0;    // 송신할때 같이 보내는 회원인덱스 초기화
        Log.d("GlobalForSendIdx Main: ",""+Global.GlobalForSendIdx);
        startActivity(new Intent(this,Splash.class));
        Log.v("알림", "메세지");
        ////////////////////////
        //list = (ListView) findViewById(R.id.listView);
        //personList = new ArrayList<HashMap<String, String>>();


        getData();
        ////////////////////////
        //setContentView(R.layout.alert_layout);
        ///////////////////////////////////////////
        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(this);

        Log.d("test","message");

        //Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/Hanoded-Heavy.ttf"));04



        Typeface typeFace = Typeface.createFromAsset(getAssets(), "Hanoded-Heavy.ttf");
        TextView textView = (TextView) findViewById(R.id.tv);
        textView.setTypeface(typeFace);


        editText1 = (EditText) findViewById(R.id.editText2_2);
//        String strText = editText1.getText().toString();
//        num_sum = Integer.parseInt(strText);

     /*   toolbar = (Toolbar) findViewById(R.id.toolbar);
        dlDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        setSupportActionBar(toolbar);

        dtToggle = new ActionBarDrawerToggle(this, dlDrawer, R.string.app_name, R.string.app_name);
        dlDrawer.setDrawerListener(dtToggle);*/

        Log.d("num_sum==", "" + Caddy_Global.num_sum);
        Log.d("알림", "메세지");

    }

    /*  public boolean onCreateOptionsMenu(Menu menu) {
          getMenuInflater().inflate(R.menu.main, menu);
          return true;
      }

      @Override
      protected void onPostCreate(Bundle savedInstanceState) {
          super.onPostCreate(savedInstanceState);
          dtToggle.syncState();
      }

      @Override
      public void onConfigurationChanged(Configuration newConfig) {
          super.onConfigurationChanged(newConfig);
          dtToggle.onConfigurationChanged(newConfig);
      }

      @Override
      public boolean onOptionsItemSelected(MenuItem item) {
          if (dtToggle.onOptionsItemSelected(item)) {
              return true;
          }

          return super.onOptionsItemSelected(item);
      }
  */
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
                    Log.d("test",sb.toString());
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
//                Log.d("jh",result);
                myJSON = result;
                showList();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute("http://218.150.183.1/moons/mobile/caddy_login.php");
//        g.execute("http://ngn.koreatech.ac.kr/moons/mobile/caddy_login.php");
    }

    protected void showList() {



        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            peoples = jsonObj.getJSONArray(TAG_RESULTS);

            for (int i = 0; i < peoples.length(); i++) {
                JSONObject c = peoples.getJSONObject(i);

                String id = c.getString(TAG_ID);
                String name = c.getString(TAG_NAME);
                Caddy_Global.arrayName[i]=name;
                Caddy_Global.arrayIdx[i]= Integer.parseInt(id);       // arrayIdx 배열에 숫자로 회원번호 저장
                Caddy_Global.arrayIdxToString[i] = id;               // arrayIdxToString 배열에 문자로 회원번호 저장

                HashMap<String, String > persons = new HashMap<String, String>();
                persons.put(TAG_ID, id);
                persons.put(TAG_NAME, name);
                if(i==0)
                {
                    a=id;
                    aa = Integer.parseInt(a)-1;
                    Log.d("a=",a);
                    Log.d("num_sum = ",""+Caddy_Global.num_sum);
                    Log.d("aa = ",""+aa);
                    if(Caddy_Global.num_sum==aa)
                    {
                        Log.d("same====",""+aa);
                    }
                }
                else
                {
                    b=id;
                }

                //personList.add(persons);
            }
            // 잘 들어갔나 확인한 로그
            Log.d("arrayName 1 = ",""+Caddy_Global.arrayName[0]);
            Log.d("arrayName 2 = ",""+Caddy_Global.arrayName[1]);
            Log.d("arrayIdx 1 = ",""+Caddy_Global.arrayIdx[0]);
            Log.d("arrayIdx 2 = ",""+Caddy_Global.arrayIdx[1]);


            ListAdapter adapter = new SimpleAdapter(
                    CaddyMainActivity.this, personList, R.layout.list_item,
                    new String[]{TAG_ID, TAG_NAME},
                    new int[]{R.id.idx, R.id.name}
            );

//            list.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    /////////////////////////////////////////////////
    public void change(){
//        Intent intent = new Intent(this, Activity_index2.class);
//
//        String give[] = {arrayIdxToString[num_sum-1],arrayName[num_sum-1]};       // 배열로 입력한 회원번호와 회원이름을 넘겨준다.
//        intent.putExtra("UserNumAndName",give);


        ///////////////////////////////////////////

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
                    Log.d("test",sb.toString());
                    bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    String json;
                    while((json = bufferedReader.readLine()) != null) {
                        sb.append(json+"\n");
                    }
                    Log.d("sbsbsbsb",sb.toString().trim());

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
                check_userNum();
            }
        }

        GetDataJSON g = new GetDataJSON();
        g.execute("http://218.150.183.1/moons/mobile/select_player.php?caddyIdx="+Caddy_Global.num_sum);
        Log.d("caddyIdx==",""+Caddy_Global.num_sum);

        ///////////////////////////////////////////



    }

    private void check_userNum()
    {
        try {
            JSONObject jsonObj = new JSONObject(myJSON);

            boolean isError = jsonObj.getBoolean("error");
            int totaluser = jsonObj.getInt("totalUser");
            userArray= jsonObj.getJSONArray("userInfo");

            for(int i =0; i < totaluser;i++)
            {
                JSONObject c = userArray.getJSONObject(i);
                String idx = c.getString(TAG_ID);
                String name = c.getString(TAG_NAME);
                Caddy_Global.arrayUserInfo_idx[i] = idx;
                Caddy_Global.arrayUserInfo_name[i] = name;
                Log.d("idx = name ==== ", idx+name);
            }


            Log.d("error",""+isError);
            Log.d("Totaluser = ",""+totaluser);
            if(isError == false)
            {
                if(totaluser==1){
                    Log.d("Success","totaluser====1");
                    Intent intent = new Intent(this, Caddy_Activity_index1.class);

                    String give[] = {Caddy_Global.arrayIdxToString[Caddy_Global.num_sum - 1], Caddy_Global.arrayName[Caddy_Global.num_sum - 1]};       // 배열로 입력한 회원번호와 회원이름을 넘겨준다.
                    intent.putExtra("UserNumAndName", give);

                    intent.putExtra("id", Caddy_Global.arrayIdxToString[Caddy_Global.num_sum - 1]);
                    intent.putExtra("name", Caddy_Global.arrayName[Caddy_Global.num_sum - 1]);

                    intent.putExtra("idx",Caddy_Global.arrayUserInfo_idx);
                    intent.putExtra("uname",Caddy_Global.arrayUserInfo_name);


                    //intent.putStringArrayListExtra("userid" ,<arrayUserInfo_idx>);
                    Log.d("is Error= true,,,,,,", "" + Caddy_Global.num_sum);
                    startActivity(intent);
                    finish();
                    return;

                }

                if(totaluser==2){
                    Log.d("Success","totaluser====2");
                    Intent intent = new Intent(this, Caddy_Activity_index2.class);

                    String give[] = {Caddy_Global.arrayIdxToString[Caddy_Global.num_sum - 1], Caddy_Global.arrayName[Caddy_Global.num_sum - 1]};       // 배열로 입력한 회원번호와 회원이름을 넘겨준다.
                    intent.putExtra("UserNumAndName", give);

                    intent.putExtra("id", Caddy_Global.arrayIdxToString[Caddy_Global.num_sum - 1]);
                    intent.putExtra("name", Caddy_Global.arrayName[Caddy_Global.num_sum - 1]);

                    intent.putExtra("idx",Caddy_Global.arrayUserInfo_idx);
                    intent.putExtra("uname",Caddy_Global.arrayUserInfo_name);


                    //intent.putStringArrayListExtra("userid" ,<arrayUserInfo_idx>);
                    Log.d("is Error= true,,,,,,", "" + Caddy_Global.num_sum);
                    startActivity(intent);
                    finish();
                    return;

                }
                else if(totaluser==3){
                    Log.d("Success","totaluser====3");
                    Intent intent = new Intent(this, Caddy_Activity_index3.class);

                    String give[] = {Caddy_Global.arrayIdxToString[Caddy_Global.num_sum - 1], Caddy_Global.arrayName[Caddy_Global.num_sum - 1]};       // 배열로 입력한 회원번호와 회원이름을 넘겨준다.
                    intent.putExtra("UserNumAndName", give);

                    intent.putExtra("id", Caddy_Global.arrayIdxToString[Caddy_Global.num_sum - 1]);
                    intent.putExtra("name", Caddy_Global.arrayName[Caddy_Global.num_sum - 1]);

                    intent.putExtra("idx",Caddy_Global.arrayUserInfo_idx);
                    intent.putExtra("uname",Caddy_Global.arrayUserInfo_name);


                    //intent.putStringArrayListExtra("userid" ,<arrayUserInfo_idx>);
                    Log.d("is Error= true,,,,,,", "" + Caddy_Global.num_sum);
                    startActivity(intent);
                    finish();
                    return;
                }
                else if(totaluser==4) {
                    Log.d("Success","totaluser====4");
                    Intent intent = new Intent(this, Caddy_Activity_index4.class);

                    String give[] = {Caddy_Global.arrayIdxToString[Caddy_Global.num_sum - 1], Caddy_Global.arrayName[Caddy_Global.num_sum - 1]};       // 배열로 입력한 회원번호와 회원이름을 넘겨준다.
                    intent.putExtra("UserNumAndName", give);

                    intent.putExtra("id", Caddy_Global.arrayIdxToString[Caddy_Global.num_sum - 1]);
                    intent.putExtra("name", Caddy_Global.arrayName[Caddy_Global.num_sum - 1]);

                    intent.putExtra("idx",Caddy_Global.arrayUserInfo_idx);
                    intent.putExtra("uname",Caddy_Global.arrayUserInfo_name);


                    //intent.putStringArrayListExtra("userid" ,<arrayUserInfo_idx>);
                    Log.d("is Error= true,,,,,,", "" + Caddy_Global.num_sum);
                    startActivity(intent);
                    finish();
                    return;
                }

            }
            else {
                Log.d("is Error= false,,,,,,",""+Caddy_Global.num_sum);
                String error_msg = jsonObj.getString("error_msg");
                Toast.makeText(getApplicationContext(), error_msg, Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    ////////////////////////////////////////////////

    private void alertButton()
    {
        boolean isFind = false;
        int pos = 0;
        for(int i = 0; i < Caddy_Global.arrayIdx.length; i++) {
            if(Caddy_Global.num_sum == Caddy_Global.arrayIdx[i]) {
                isFind = true;
                pos = i;
                break;
            }
        }
        if(isFind == true)          // 넣은 값이 배열에 있으면 ( 회원번호가 존재하면 )
        {
            new AlertDialog.Builder(this)
                    .setMessage("캐디번호 "+Caddy_Global.num_sum + "  " +Caddy_Global.arrayName[pos]+  " 회원이 맞습니까?")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)          // 회원번호와 이름이 맞으면 확인을 눌러 회원번호를 가지고 다음 메인화면으로 넘어감
                        {
                            change();
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
        else if(isFind==false || Caddy_Global.num_sum ==0)               // 입력한 회원번호가 존재하지 않으면
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
                Caddy_Global.num_sum = Integer.parseInt(editText1.getText().toString());
                alertButton();
                break;
        }
    }
}