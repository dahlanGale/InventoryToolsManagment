package com.ioe_enterprice.inventorytoolsmanagment.Domain;

public class OngoingDomain {
    private String title;
    private String date;
    private int progressPercent;
    private String picPath;

    public OngoingDomain(String tittle, String date, int progressPercent, String picPath) {
        this.title = title;
        this.date = date;
        this.progressPercent = progressPercent;
        this.picPath = picPath;
    }

    public String getTittle() {
        return title;
    }

    public void setTittle(String tittle) {
        this.title = tittle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }
}
