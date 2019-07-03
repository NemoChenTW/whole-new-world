package com.taipei.ttbootcamp.interfaces;

import com.taipei.ttbootcamp.data.TripData;
import com.tomtom.online.sdk.routing.data.RouteResponse;

public interface IPlanResultListener {
    void onRoutePlanComplete(RouteResponse routeResult, TripData tripData, boolean needOptimize);
}
