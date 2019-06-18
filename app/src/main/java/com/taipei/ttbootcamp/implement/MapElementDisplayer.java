package com.taipei.ttbootcamp.implement;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Log;

import com.google.common.base.Optional;
import com.taipei.ttbootcamp.R;
import com.taipei.ttbootcamp.interfaces.IMapElementDisplay;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.map.TomtomMapCallback;
import com.tomtom.online.sdk.routing.data.FullRoute;

import java.util.ArrayList;
import java.util.List;

public class MapElementDisplayer implements IMapElementDisplay {
    static private final String TAG = "MapElementDisplay";

    Context mContext;
    TomtomMap mTomtomMap = null;

    static final private String MARK_LOCATION_TAG = "mark_location_tag";
    private Icon mIconDeparture;
    private Icon mIconDestination;
    private Icon mIconWaypoint;
    private Icon mIconMarkLocation;

    private LatLng mCurrentLatLng;
    private LatLng mDeparturePosition;
    private LatLng mDestinationPosition;
    private ArrayList<LatLng> allWaypoints = new ArrayList<LatLng>();

    public MapElementDisplayer(Context context, TomtomMap tomtomMap) {
        mContext = context;
        mTomtomMap = tomtomMap;

        mTomtomMap.addOnMapClickListener(onMapClickListener);
        mTomtomMap.addOnMapLongClickListener(onMapLongClickListener);
        mTomtomMap.addOnMapViewPortChangedListener(onMapViewPortChangedListener);

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

    private TomtomMapCallback.OnMapClickListener onMapClickListener =
            latLng -> displayMessage(
                    R.string.menu_events_on_map_click,
                    latLng.getLatitude(),
                    latLng.getLongitude()
            );
    private TomtomMapCallback.OnMapLongClickListener onMapLongClickListener =
            latLng -> handleLongClick(latLng);

    private TomtomMapCallback.OnMapViewPortChanged onMapViewPortChangedListener =
            (focalLatitude, focalLongitude, zoomLevel, perspectiveRatio, yawDegrees) -> displayMessage(
                    R.string.menu_events_on_map_panning,
                    focalLatitude,
                    focalLongitude
            );
    private void displayMessage(@StringRes int titleId, double lat, double lon) {
        String title = mContext.getString(titleId);
        String message = String.format(java.util.Locale.getDefault(),
                "%s : %f : %f", title, lat, lon);
        Log.d(TAG, "displayMessage: " + message);
    }
    private void handleLongClick(@NonNull LatLng latLng) {
        updateMarkLocation(latLng);
        mCurrentLatLng = latLng;
    }

    private void updateMarkLocation(LatLng position) {
        mTomtomMap.removeMarkerByTag(MARK_LOCATION_TAG);
        Optional optionalMarker = mTomtomMap.findMarkerByPosition(position);
        if (!optionalMarker.isPresent()) {
            mTomtomMap.addMarker(new MarkerBuilder(position)
                    .icon(mIconMarkLocation)
                    .tag(MARK_LOCATION_TAG));
        }
    }

    public void updateMarkers() {
        mTomtomMap.removeMarkers();

        if (mDeparturePosition != null) {
            createMarkerIfNotPresent(mDeparturePosition, mIconDeparture);
        }

        if (mDestinationPosition != null) {
            createMarkerIfNotPresent(mDestinationPosition, mIconDestination);
        }

        for (LatLng wp : allWaypoints) {
            createMarkerIfNotPresent(wp, mIconWaypoint);
        }
    }

    @Override
    public LatLng getDeparturePosition() {
        return mDeparturePosition;
    }

    @Override
    public LatLng getDestinationPosition() {
        return mDestinationPosition;
    }

    @Override
    public LatLng getCurrentLatLng() {
        return mCurrentLatLng;
    }

    @Override
    public ArrayList<LatLng> getAllWaypoints() {
        return allWaypoints;
    }

    @Override
    public void setDeparturePosition(LatLng position) {
        mDeparturePosition = position;
    }

    @Override
    public void setDestinationPosition(LatLng position) {
        mDestinationPosition = position;
    }

    @Override
    public void addWaypoint(LatLng position) {
        allWaypoints.add(position);
    }

    @Override
    public void clearWaypoints() {
        allWaypoints.clear();
    }

    private void createMarkerIfNotPresent(LatLng position, Icon icon) {
        Optional optionalMarker = mTomtomMap.findMarkerByPosition(position);
        if (!optionalMarker.isPresent()) {
            mTomtomMap.addMarker(new MarkerBuilder(position)
                    .icon(icon));
        }
    }
}
