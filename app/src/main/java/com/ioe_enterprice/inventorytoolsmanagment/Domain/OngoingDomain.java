package com.ioe_enterprice.inventorytoolsmanagment.Domain;

public class OngoingDomain {
    private String title;
    private String date;
    private int progressPercent;
    private int counted;
    private int total;
    private String picPath;

    public OngoingDomain(String title, String date, int progressPercent, String picPath, int counted, int total) {
        this.title = title;
        this.date = date;
        this.progressPercent = progressPercent;
        this.picPath = picPath;
        this.counted = counted;
        this.total = total;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getCounted() { return counted; }
    public void setCounted(int counted) { this.counted = counted; }
    public String getPicPath() { return picPath; }
    public void setPicPath(String picPath) { this.picPath = picPath; }
}