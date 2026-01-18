package com.example.fadejayaapp.model;

public class DashboardMenu {
    private String title;
    private int icon;

    public DashboardMenu(String title, int icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() { return title; }
    public int getIcon() { return icon; }
}