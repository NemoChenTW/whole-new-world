package com.taipei.ttbootcamp.interfaces;

import com.taipei.ttbootcamp.data.TripData;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.routing.data.FullRoute;

import java.util.ArrayList;
import java.util.List;

public interface IMapElementDisplay {
    /**
     * Draw route on the map
     * @param routes Input route list
     */
    void displayRoutes(List<FullRoute> routes);

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
