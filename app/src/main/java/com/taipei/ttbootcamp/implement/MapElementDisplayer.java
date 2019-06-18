package com.taipei.ttbootcamp.implement;

import android.content.Context;

import com.taipei.ttbootcamp.R;
import com.taipei.ttbootcamp.interfaces.IMapElementDisplay;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.routing.data.FullRoute;

import java.util.List;

public class MapElementDisplayer implements IMapElementDisplay {
    static private final String TAG = "MapElementDisplay";

    Context mContext;
    TomtomMap mTomtomMap = null;

    private Icon mIconDeparture;
    private Icon mIconDestination;
    private Icon mIconWaypoint;
    private Icon mIconMarkLocation;

    public MapElementDisplayer(Context context, TomtomMap tomtomMap) {
        mContext = context;
        mTomtomMap = tomtomMap;

        initMapRelatedElement();
    }

    private void initMapRelatedElement() {
        mIconDeparture = Icon.Factory.fromResources(mContext, R.drawable.ic_map_route_departure);
        mIconDestination = Icon.Factory.fromResources(mContext, R.drawable.ic_map_route_destination);
        mIconWaypoint = Icon.Factory.fromResources(mContext, R.drawable.ic_map_traffic_danger_midgrey_small);
        mIconMarkLocation = Icon.Factory.fromResources(mContext, R.drawable.ic_markedlocation);
    }

    public void displayRoutes(List<FullRoute> routes) {
        for (FullRoute fullRoute : routes) {
            mTomtomMap.addRoute(new RouteBuilder(
                    fullRoute.getCoordinates()).startIcon(mIconDeparture).endIcon(mIconDestination).isActive(true));
        }
    }
}
