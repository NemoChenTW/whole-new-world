package com.taipei.ttbootcamp.data;

import com.tomtom.online.sdk.common.location.LatLng;

public class LocationPoint {
    private String mName;
    private LatLng mPosition;

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

    public void setName(String name) {
        mName = name;
    }

    public LatLng getPosition() {
        return mPosition;
    }

    public void setPosition(LatLng position) {
        mPosition = position;
    }
}