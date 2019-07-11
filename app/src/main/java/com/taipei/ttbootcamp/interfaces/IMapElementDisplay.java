package com.taipei.ttbootcamp.interfaces;

import com.taipei.ttbootcamp.data.TripData;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.routing.data.FullRoute;

import java.util.List;

public interface IMapElementDisplay {
    /**
     * Draw route on the map
     * @param routes Input route list
     * @param tripData
     */
    void displayRoutes(List<FullRoute> routes, TripData tripData);

    /**
     * Update map markers
     * @param tripData
     */
    void updateMarkers(TripData tripData);

    /**
     * Remove map markers
     */
    void removeMarkers();

    void addPositionUpdateListener(IPositionUpdateListener positionUpdateListener);
    void removePositionUpdateListener(IPositionUpdateListener positionUpdateListener);
    interface IPositionUpdateListener {
        void onPositionUpdate(TripData tripData);
    }

    TomtomMap getTomtomMap();

}
