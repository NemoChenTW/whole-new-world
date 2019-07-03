package com.taipei.ttbootcamp.interfaces;

import com.taipei.ttbootcamp.data.TripData;

public interface ITripOptimizer {
    void setOptimizeResultListener(IOptimizeResultListener optimizeResultListener);
    void optimizeTrip(TripData tripData);
}
