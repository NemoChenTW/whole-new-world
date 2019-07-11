package com.taipei.ttbootcamp.implementations;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Log;

import com.google.common.base.Optional;
import com.taipei.ttbootcamp.R;
import com.taipei.ttbootcamp.data.LocationPoint;
import com.taipei.ttbootcamp.data.TripData;
import com.taipei.ttbootcamp.interfaces.IMapElementDisplay;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.map.SimpleMarkerBalloon;
import com.tomtom.online.sdk.map.TextBalloonViewAdapter;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.map.TomtomMapCallback;
import com.tomtom.online.sdk.routing.data.FullRoute;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class MapElementDisplayer implements IMapElementDisplay, IMapElementDisplay.IPositionUpdateListener {
    static private final String TAG = "MapElementDisplay";

    Context mContext;
    TomtomMap mTomtomMap = null;

    private static final String MARK_LOCATION_TAG = "mark_location_tag";
    private static final String WAY_POINT_TAG = "way_point_tag";
    private Icon mIconDeparture;
    private Icon mIconDestination;
    private Icon mIconMarkLocation;

    private Set<IPositionUpdateListener> mPositionUpdateListeners = new CopyOnWriteArraySet<>();

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
        mIconMarkLocation = Icon.Factory.fromResources(mContext, R.drawable.ic_markedlocation);
    }

    public void displayRoutes(List<FullRoute> routes, TripData tripData) {
        mTomtomMap.clearRoute();
        for (FullRoute fullRoute : routes) {
            mTomtomMap.addRoute(new RouteBuilder(
                    fullRoute.getCoordinates()).startIcon(mIconDeparture).endIcon(mIconDestination).isActive(true));
        }

        if (tripData.hasWaypoints()) {
            displayWayPoints(tripData);
        }
    }

    private void displayWayPoints(TripData tripData) {
        mTomtomMap.removeMarkerByTag(WAY_POINT_TAG);
        mTomtomMap.getMarkerSettings().setMarkerBalloonViewAdapter(new TextBalloonViewAdapter());
        for (LocationPoint locationPoint : tripData.getWayPoints()) {
            MarkerBuilder markerBuilder = new MarkerBuilder(locationPoint.getPosition())
                    .markerBalloon(new SimpleMarkerBalloon(locationPoint.getName()))
                    .tag(WAY_POINT_TAG);
            mTomtomMap.addMarker(markerBuilder);
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

    public void updateMarkers(TripData tripData) {
        removeMarkers();

        LatLng departurePosition = tripData.getStartPoint();
        LatLng destinationPosition = tripData.getEndPoint();

        if (departurePosition != null) {
            createMarkerIfNotPresent(departurePosition, mIconDeparture);
        }
        if (destinationPosition != null) {
            createMarkerIfNotPresent(destinationPosition, mIconDestination);
        }
    }

    @Override
    public void removeMarkers() {
        mTomtomMap.removeMarkers();
    }

    @Override
    public void addPositionUpdateListener(IPositionUpdateListener positionUpdateListener) {
        mPositionUpdateListeners.add(positionUpdateListener);
    }

    @Override
    public void removePositionUpdateListener(IPositionUpdateListener positionUpdateListener) {
        mPositionUpdateListeners.remove(positionUpdateListener);
    }

    @Override
    public TomtomMap getTomtomMap() {
        return mTomtomMap;
    }

    @Override
    public void onPositionUpdate(TripData tripData) {
        if (!mPositionUpdateListeners.isEmpty()) {
            for (IPositionUpdateListener listener : mPositionUpdateListeners) {
                listener.onPositionUpdate(tripData);
            }
        }
    }

    private void createMarkerIfNotPresent(LatLng position, Icon icon) {
        Optional optionalMarker = mTomtomMap.findMarkerByPosition(position);
        if (!optionalMarker.isPresent()) {
            mTomtomMap.addMarker(new MarkerBuilder(position)
                    .icon(icon));
        }
    }


}
