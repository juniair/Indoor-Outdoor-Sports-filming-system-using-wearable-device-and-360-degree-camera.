package com.crv.myapplication.model;

import java.util.Date;

public class DailySchedule {
    private Date date;
    private boolean isEvent;
    private boolean isMonth;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isEvent() {
        return isEvent;
    }

    public void setEvent(boolean event) {
        isEvent = event;
    }

    public boolean isMonth() {
        return isMonth;
    }

    public void setMonth(boolean month) {
        isMonth = month;
    }
}
