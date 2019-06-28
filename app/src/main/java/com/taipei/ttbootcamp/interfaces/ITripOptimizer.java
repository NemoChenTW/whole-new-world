package com.taipei.ttbootcamp.interfaces;

import java.util.ArrayList;

public interface ITripOptimizer {
    void setOptimizeResultListener(IOptimizeResultListener optimizeResultListener);
    void optimizeTrip(ArrayList<POIWithTravelTime> poiWithTravelTimeList);
}
