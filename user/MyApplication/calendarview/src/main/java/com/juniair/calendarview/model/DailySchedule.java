package com.juniair.calendarview.model;

import java.util.Date;

public class DailySchedule {
    private Date date;
    private String commit;
    private boolean isMonth;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public boolean isMonth() {
        return isMonth;
    }

    public void setMonth(boolean month) {
        isMonth = month;
    }
}
