package com.jins_meme.swing;


import java.util.Date;

/**
 * YawPeak.
 * <p>
 * The MIT License
 * Copyright 2017 JINS Corp.
 */
public class YawPeak {

    private long id = 0;
    private Date date;
    private int startFlag = 0;
    private int count = 0;
    private int sign = 0;
    private int summaryFlag = 0;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getStartFlag() {
        return startFlag;
    }

    public void setStartFlag(int startFlag) {
        this.startFlag = startFlag;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    public int getSummaryFlag() {
        return summaryFlag;
    }

    public void setSummaryFlag(int summaryFlag) {
        this.summaryFlag = summaryFlag;
    }

    public YawPeak() {
        this.date = new Date();
    }
}
