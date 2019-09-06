package com.taipei.ttbootcamp.interfaces;

import com.taipei.ttbootcamp.data.TripData;

public interface IOptimizeResultCallBack {
    void optimizeWithRestaurant(TripData tripData, boolean isOptimize, int restaurantIdx);
}
