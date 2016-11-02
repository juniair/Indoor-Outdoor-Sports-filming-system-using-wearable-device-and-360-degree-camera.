package com.crv.myapplication.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crv.myapplication.R;
import com.crv.myapplication.adapter.CalendarAdapter;
import com.crv.myapplication.app.AppConfig;
import com.crv.myapplication.app.AppController;
import com.crv.myapplication.model.DailySchedule;
import com.crv.myapplication.util.EventHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CalendarView extends LinearLayout {
    // for logging
    private static final String TAG = CalendarView.class.getSimpleName();

    // how many days to show, defaults to six weeks, 42 days
    private static final int DAYS_COUNT = 42;

    // default date format
    private static final String DATE_FORMAT = "yyyy MMMM";

    // date format
    private String dateFormat;

    // current displayed month
    private Calendar currentDate = Calendar.getInstance();

    //event handling
    private EventHandler eventHandler = null;

    // internal components
    private LinearLayout header;
    private ImageView btnPrev;
    private ImageView btnNext;
    private TextView txtDate;
    private GridView grid;

    // seasons' rainbow
    int[] rainbow = new int[] {
            R.color.summer,
            R.color.fall,
            R.color.winter,
            R.color.spring
    };

    // month-season association (northern hemisphere, sorry australia :)
    int[] monthSeason = new int[] {2, 2, 3, 3, 3, 0, 0, 0, 1, 1, 1, 2};

    public CalendarView(Context context)
    {
        super(context);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initControl(context, attrs);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);
    }

    private void initControl(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.control_calendar, this);

        loadDateFormat(attrs);
        assignUiElements();
        assignClickHandlers();

        updateCalendar();
    }

    private void loadDateFormat(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarView);

        try {
            // try to load provided date format, and fallback to default otherwise
            dateFormat = typedArray.getString(R.styleable.CalendarView_dateFormat);
            if (dateFormat == null)
                dateFormat = DATE_FORMAT;
        } finally {
            typedArray.recycle();
        }
    }

    private void assignUiElements() {
        // layout is inflated, assign local variables to components
        header = (LinearLayout)findViewById(R.id.calendar_header);
        btnPrev = (ImageView)findViewById(R.id.calendar_prev_button);
        btnNext = (ImageView)findViewById(R.id.calendar_next_button);
        txtDate = (TextView)findViewById(R.id.calendar_date_display);
        grid = (GridView)findViewById(R.id.calendar_grid);
    }

    private void assignClickHandlers() {
        // add one month and refresh UI
        btnNext.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                currentDate.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), 1);
                currentDate.add(Calendar.MONTH, 1);
                updateCalendar();
            }
        });
        // subtract one month and refresh UI
        btnPrev.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                currentDate.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), 1);
                currentDate.add(Calendar.MONTH, -1);
                AppController.getInstance().setYear(new SimpleDateFormat("yyyy").format(currentDate.getTime()));
                AppController.getInstance().setMonth(new SimpleDateFormat("MM").format(currentDate.getTime()));
                updateCalendar();
            }
        });

        // long-pressing a day
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (eventHandler == null)
                    return;
                eventHandler.OnItemClickListener((DailySchedule)parent.getItemAtPosition(position));
            }
        });
    }

    /**
     * Display dates correctly in grid
     */
    public void updateCalendar() {
        updateCalendar(new HashSet<DailySchedule>());
    }

    /**
     * Display dates correctly in grid
     * @param events 경기가 있는 날자에 대한 HashSet<DailySchedule>이다.
     *
     */
    public void updateCalendar(final HashSet<DailySchedule> events) {

        final ArrayList<DailySchedule> cells = new ArrayList<>();
        Calendar calendar = (Calendar)currentDate.clone();

        // determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int monthLastingCell = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        while (cells.size() < DAYS_COUNT) {
            DailySchedule schedule = new DailySchedule();
            schedule.setDate(calendar.getTime());

            if( monthBeginningCell <= cells.size() && cells.size() < monthLastingCell) {
                schedule.setMonth(true);
            } else {
                schedule.setMonth(false);
            }
            schedule.setEvent(false);
            cells.add(schedule);
            events.add(schedule);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        AppController.getInstance().setYear(new SimpleDateFormat("yyyy").format(currentDate.getTime()));
        AppController.getInstance().setMonth(new SimpleDateFormat("MM").format(currentDate.getTime()));

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                AppConfig.URL_GETINFO, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // 서버에서 년월 일을 받는다.
                        // events와 서버에서 온 데이터에서 년/ 월 / 일 이 일차하는 위치에 isEvent를 true로 설정 나머지는 false;

                        try{
                            //Dlog.d(response);
                            JSONObject jObj = new JSONObject(response);
                            //Dlog.d("res : " + response);
                            boolean error = jObj.getBoolean("error");

                            if(!error)
                            {
                                JSONArray jsonArray = jObj.getJSONArray("info");
                                for(int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    String event_year = object.getString("year");
                                    String event_month = object.getString("month");
                                    String event_day = object.getString("day");

                                    for(DailySchedule event : cells) {

                                        if(event_year.equals(new SimpleDateFormat("yyyy").format(event.getDate())) &&
                                                event_month.equals(new SimpleDateFormat("MM").format(event.getDate())) &&
                                                event_day.equals(new SimpleDateFormat("dd").format(event.getDate())) && event.isMonth()) {
//                                            event.setEvent(true);
                                            event.setEvent(true);
                                            break;
                                        }
                                    }
                                }

                            }
                            else {
                                // Error in login. Get the error message
                                String errorMsg = jObj.getString("error_msg");
                                //Log.d("# Error Message # ",""+errorMsg);
                            }
                        }catch (JSONException e){
                            //Log.d("** onResponse ** ","Catch~~");
                        }
                        // update grid
                        grid.setAdapter(new CalendarAdapter(getContext(), cells, events));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.d("onErrorResponse ::: "," What...?");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //return super.getParams();
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();

                params.put("idx", AppController.getInstance().getIdx());
                params.put("year" , AppController.getInstance().getYear());
                params.put("month" , AppController.getInstance().getMonth());

                return params;
            }

        };



        // update title
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        txtDate.setText(sdf.format(currentDate.getTime()));

        // set header color according to current season
        int month = currentDate.get(Calendar.MONTH);
        int season = monthSeason[month];
        int color = rainbow[season];

        header.setBackgroundColor(getResources().getColor(color));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest, "Get Event Day");
    }


    /**
     * Assign event handler to be passed needed events
     */
    public void setEventHandler(EventHandler eventHandler)
    {
        this.eventHandler = eventHandler;
    }

    /**
     * This interface defines what events to be reported to
     * the outside world
     */
}
