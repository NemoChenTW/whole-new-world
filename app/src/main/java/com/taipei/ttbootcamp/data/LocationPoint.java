package com.taipei.ttbootcamp.data;

import com.tomtom.online.sdk.common.location.LatLng;

public class LocationPoint {
    private String mName;
    private LatLng mPosition;

    //
    private int stayTimeToSeconds = 3600;
    private int openingToSeconds = 0;
    private int closedToSeconds = 0;

    public LocationPoint(final LatLng position) {
        mPosition = position;
    }

    public LocationPoint(final LatLng position, final String name) {
        mPosition = position;
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public String getFirstName() {
        try {
            return mName.split(",")[0];
        } catch (Exception e) {
            return mName;
        }
    }

    public void setName(String name) {
        mName = name;
    }

    public LatLng getPosition() {
        return mPosition;
    }

    public void setPosition(LatLng position) {
        mPosition = position;
    }

    //
    public void setStaytime(int stayTime){
        stayTimeToSeconds = stayTime;
    }

    public int getStaytimeToSeconds(){ return stayTimeToSeconds; }

    public void setOpeningHours(int openingTime, int closedTime){
        openingToSeconds = openingTime;
        closedToSeconds = closedTime;
    }

    public int getOpeningToSeconds(){ return openingToSeconds; }

    public int getClosedToSeconds(){ return closedToSeconds; }
}
