package com.lge.friendsCamera;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class Caddy_Activity_index4 extends AppCompatActivity {

    ////////////////////////////////
    String myJSON;

    ///////////////////////////////////



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.caddy_activity_index4);

        Global.GlobalForSendIdx = 0;    // 송신할때 같이 보내는 회원인덱스 초기화
        Log.d("GlobalForSendIdx == ",""+Global.GlobalForSendIdx);
        Button button1 =(Button)findViewById(R.id.button_idx1);
        Button button2 =(Button)findViewById(R.id.button_idx2);
        Button button3 =(Button)findViewById(R.id.button_idx3);
        Button button4 =(Button)findViewById(R.id.button_idx4);

//        Button button6 =(Button)findViewById(R.id.button6);
//        button6.setOnClickListener((View.OnClickListener) this);

        Intent intent=getIntent();

        String id=intent.getExtras().getString("id");
        String name=intent.getExtras().getString("name");
        Caddy_Global.CaddyIdx = id;


        TextView txtId=(TextView) findViewById(R.id.textid);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "Typo_SsangmunDongB.ttf");
        TextView textView = (TextView) findViewById(R.id.textid);
        textView.setTypeface(typeFace);

        txtId.setText("안녕하세요! 캐디번호"+id+" "+name+"입니다.");


        Caddy_Global.idx= intent.getExtras().getStringArray("idx");
        Caddy_Global.uname= intent.getExtras().getStringArray("uname");

        Log.d("11111", "" + Caddy_Global.idx[0]);
        Log.d("2222", "" + Caddy_Global.uname[0]);

        /*Typeface c1 = Typeface.createFromAsset(getAssets(), "HoonWhitecatR.ttf");
        TextView c1_1 = (TextView) findViewById(R.id.button_startscan);
        textView.setTypeface(c1);*/


        button1.setText("회원번호: "+ Caddy_Global.idx[0]+" "+" 이름 : "+Caddy_Global.uname[0]);

        button2.setText("회원번호: "+ Caddy_Global.idx[1]+" "+" 이름 : "+Caddy_Global.uname[1]);

        button3.setText("회원번호: "+ Caddy_Global.idx[2]+" "+" 이름 : "+Caddy_Global.uname[2]);

        button4.setText("회원번호: "+ Caddy_Global.idx[3]+" "+" 이름 : "+Caddy_Global.uname[3]);


    }

    public void clicked1(View view){
        Global.GlobalForSendIdx = Integer.parseInt(Caddy_Global.idx[0]);            // 파일과 함께 보내기 위한 인덱스를 인트화시킴
        Global.GloabalForSendName = Caddy_Global.uname[0];
        Log.d("GlobalForSendafter ",""+Global.GlobalForSendIdx);
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra("id1",Caddy_Global.idx[0]);
        intent.putExtra("uname1",Caddy_Global.uname[0]);

        Log.d("idxtest3", "" + Caddy_Global.idx[0]);
        startActivity(intent);


    }

    public void clicked2(View view){
        Global.GlobalForSendIdx = Integer.parseInt(Caddy_Global.idx[1]);            // 파일과 함께 보내기 위한 인덱스를 인트화시킴
        Global.GloabalForSendName = Caddy_Global.uname[1];
        Log.d("GlobalForSendafter ",""+Global.GlobalForSendIdx);
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra("id1",Caddy_Global.idx[1]);
        intent.putExtra("uname1",Caddy_Global.uname[1]);

        Log.d("idxtest3", "" + Caddy_Global.idx[1]);
        startActivity(intent);


    }

    public void clicked3(View view){
        Global.GlobalForSendIdx = Integer.parseInt(Caddy_Global.idx[2]);            // 파일과 함께 보내기 위한 인덱스를 인트화시킴
        Global.GloabalForSendName = Caddy_Global.uname[2];
        Log.d("GlobalForSendafter ",""+Global.GlobalForSendIdx);
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra("id1",Caddy_Global.idx[2]);
        intent.putExtra("uname1",Caddy_Global.uname[2]);

        Log.d("idxtest3", "" + Caddy_Global.idx[2]);
        startActivity(intent);

    }

    public void clicked4(View view){
        Global.GlobalForSendIdx = Integer.parseInt(Caddy_Global.idx[3]);            // 파일과 함께 보내기 위한 인덱스를 인트화시킴
        Global.GloabalForSendName = Caddy_Global.uname[3];
        Log.d("GlobalForSendafter ",""+Global.GlobalForSendIdx);
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra("id1",Caddy_Global.idx[3]);
        intent.putExtra("uname1",Caddy_Global.uname[3]);

        Log.d("idxtest3", "" + Caddy_Global.idx[3]);
        startActivity(intent);


    }
//    public void onClick(View v) {
//        switch (v.getId()) {
//
//            case R.id.button6:
//                class GetDataJSON extends AsyncTask<String, Void, String> {
//
//                    // 백그라운드에서 작동되는 함수
//                    @Override
//                    protected String doInBackground(String... params) {
//                        String uri = params[0];
//
//                        BufferedReader bufferedReader;
//                        try {
//                            URL url = new URL(uri);
//                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//                            StringBuilder sb = new StringBuilder();
//                            Log.d("test",sb.toString());
//                            bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//
//                            String json;
//                            while((json = bufferedReader.readLine()) != null) {
//                                sb.append(json+"\n");
//                            }
//                            return sb.toString().trim();
//
//                        } catch (MalformedURLException e) {
//                            e.printStackTrace();
//                            return null;
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            return null;
//                        }
//                    }
//
//                    // execute() 실행시 해당 함수가 호출
//                    @Override
//                    protected void onPostExecute(String result) {
//                        Log.d("jh",result);
//                        myJSON = result;
//                        initialize();
//                    }
//                }
//
//                GetDataJSON g = new GetDataJSON();
//                g.execute(" http://192.168.0.26/moons/mobile/reset.php?caddyReset="+Caddy_Global.CaddyIdx);
//                Log.d("caddyIdx==",""+Caddy_Global.CaddyIdx);
//
//                ////////
//
//                break;
//        }
//    }

    public void initialize()
    {
        ////////////////////

        try {
            JSONObject jsonObj = new JSONObject(myJSON);

            boolean isError = jsonObj.getBoolean("error");


            Log.d("error",""+isError);
            if(isError == false)
            {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
            else {
                Log.d("is Error= false,,,,,,",""+Caddy_Global.CaddyIdx);
                String error_msg = jsonObj.getString("error_msg");
                Toast.makeText(getApplicationContext(), error_msg, Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        ////////////////////

    }


}
