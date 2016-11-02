package com.crv.myapplication.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crv.myapplication.R;
import com.crv.myapplication.adapter.RecodeAdapter;
import com.crv.myapplication.app.AppConfig;
import com.crv.myapplication.app.AppController;
import com.crv.myapplication.assist.MD360PlayerActivity;
import com.crv.myapplication.model.DailySchedule;
import com.crv.myapplication.model.Recode;
import com.crv.myapplication.util.EventHandler;
import com.crv.myapplication.util.GridSpacingItemDecoration;
import com.crv.myapplication.util.OnItemClickListener;
import com.crv.myapplication.util.OnMenuItemClickListener;
import com.crv.myapplication.view.CalendarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private CalendarView compactCalendarView;
    private ActionBar toolbar;
    private RecodeAdapter adapter;
    private RecyclerView recyclerView;
    private List<Recode> recodeList;
    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("yyyy MMM", Locale.getDefault());
    private HashSet<DailySchedule> events;
    public String u;
    public String file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        initControl();

    }

    private void initControl() {
        recodeList = new ArrayList<>();
        adapter = new RecodeAdapter(this, recodeList);
        events = new HashSet<>();
        assignUiElements();
        assignRecyclerView();
        assignCalendar();

    }

    private void assignUiElements() {
        compactCalendarView = (CalendarView) findViewById(R.id.calendar_view);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);


    }

    private void assignRecyclerView() {
        adapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void itemClicked(TextView textView) {
                file = textView.getText().toString();
                u="http://218.150.183.1/moons/recode/" + AppController.getInstance().getFilePath() + file + ".mp4";

                MD360PlayerActivity.startVideo(LoginActivity.this, Uri.parse(u));
            }
        });
        adapter.setMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_add_favourite:
                        Toast.makeText(getApplicationContext(), "Add to favourite", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.action_play_next:
                        Toast.makeText(getApplicationContext(), "Play next", Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                }
                return false;
            }
        });
    }


    private void assignCalendar() {
        compactCalendarView.setEventHandler(new EventHandler() {
            @Override
            public void OnItemClickListener(DailySchedule schedule) {
                //AppController.getInstance().setIdx("" + 1);
                AppController.getInstance().setYear(new SimpleDateFormat("yyyy").format(schedule.getDate()));
                AppController.getInstance().setMonth(new SimpleDateFormat("MM").format(schedule.getDate()));
                AppController.getInstance().setDay(new SimpleDateFormat("dd").format(schedule.getDate()));
                StringRequest request = new StringRequest(Request.Method.POST, AppConfig.LIST_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            boolean isError = result.getBoolean("error");
                            if(!isError) {
                                JSONArray jsonArray = result.getJSONArray("result");
                                recodeList.clear();
                                for(int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    Recode recode = new Recode();
                                    recode.setTitle(object.getString("title"));
                                    recode.setThumbnailURL(object.getString("thumbnailURL"));
                                    recodeList.add(recode);
                                }
                                adapter.notifyDataSetChanged();
                            }
                            else {
                                //Dlog.d(result.getString("error_msg"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("idx", AppController.getInstance().getIdx());
                        params.put("year", AppController.getInstance().getYear());
                        params.put("month", AppController.getInstance().getMonth());
                        params.put("day", AppController.getInstance().getDay());
                        return params;
                    }
                };
                AppController.getInstance().addToRequestQueue(request, "req_search");
            }
        });
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                alert.setMessage("로그아웃 하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'YES'
                                finish();
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        }).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'No'
                                return ;
                            }
                        });
                AlertDialog alert_t = alert.create();
                alert.show();
                break;
        }

        return false;
    }

}


