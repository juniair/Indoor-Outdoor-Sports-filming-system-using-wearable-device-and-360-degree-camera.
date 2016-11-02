package com.juniair.calendarview.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.juniair.calendarview.R;
import com.juniair.calendarview.model.DailySchedule;

import java.util.ArrayList;
import java.util.HashSet;

public class CalendarAdapter extends ArrayAdapter<DailySchedule>
{
    // for view inflation
    private LayoutInflater inflater;
    private Context context;
    private HashSet<DailySchedule> events;

    public CalendarAdapter(Context context, ArrayList<DailySchedule> days, HashSet<DailySchedule> events)
    {
        super(context, R.layout.control_calendar_day, days);
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.events = events;
    }



    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        // day in question
        DailySchedule schedule = getItem(position);

        // inflate item if it does not exist yet
        if (view == null)
            view = inflater.inflate(R.layout.control_calendar_day, parent, false);

        // if this day has an event, specify event image
        view.setBackgroundResource(0);

        if(events != null) {
            for(DailySchedule eventSchedule : events) {
                if(schedule.getDate() == eventSchedule.getDate()
                        && eventSchedule.getCommit() != null) {
                    view.setBackgroundResource(R.drawable.reminder);
                }
            }
        }

        // clear styling
        ((TextView)view).setTypeface(null, Typeface.NORMAL);

        if(schedule.isMonth()) {
            switch (position % 7) {
                case 0:
                    ((TextView)view).setTextColor(Color.RED);
                    break;
                case 6:
                    ((TextView)view).setTextColor(Color.BLUE);
                    break;
                default:
                    ((TextView)view).setTextColor(Color.BLACK);
            }
        }
        else {
            ((TextView)view).setTextColor(context.getResources().getColor(R.color.greyed_out));
        }

        ((TextView)view).setText(String.valueOf(schedule.getDate().getDate()));

        return view;
    }
}
