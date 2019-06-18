package com.taipei.ttbootcamp.interfaces;

import com.tomtom.online.sdk.common.location.LatLng;
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
     */
    void updateMarkers();


    LatLng getDeparturePosition();
    LatLng getDestinationPosition();
    LatLng getCurrentLatLng();
    ArrayList<LatLng> getAllWaypoints();

    void setDeparturePosition(LatLng position);
    void setDestinationPosition(LatLng position);
    void addWaypoint(LatLng position);
    void clearWaypoints();
}
