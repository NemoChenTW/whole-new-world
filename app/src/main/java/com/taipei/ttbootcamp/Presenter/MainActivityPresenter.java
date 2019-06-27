package com.taipei.ttbootcamp.Presenter;

import com.taipei.ttbootcamp.interfaces.IMapElementDisplay;
import com.taipei.ttbootcamp.interfaces.MainActivityView;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.TomtomMapCallback;

public class MainActivityPresenter {
    IMapElementDisplay mMapElementDisplay;
    MainActivityView mView;

    public MainActivityPresenter(MainActivityView view) {
        mView = view;
    }

    public void initMainActivityPreserter(IMapElementDisplay mapElementDisplay) {
        mMapElementDisplay = mapElementDisplay;
        mMapElementDisplay.getTomtomMap().addOnMapLongClickListener(onMapLongClickListener);
    }

    private TomtomMapCallback.OnMapLongClickListener onMapLongClickListener =
            latLng -> handleLongClick(latLng);

    private void handleLongClick(LatLng latLng) {
        mView.showMarkerFeatureMenu();
    }

    public void onDepartureButtonClick() {
        if (mMapElementDisplay.getCurrentLatLng() != null) {
            mMapElementDisplay.setDeparturePosition(new LatLng(mMapElementDisplay.getCurrentLatLng().toLocation()));
        }
        mMapElementDisplay.updateMarkers();
        mView.hideMarkerFeatureMenu();
    }

    public void onDestinationButtonClick() {
        if (mMapElementDisplay.getCurrentLatLng() != null) {
            mMapElementDisplay.setDestinationPosition(new LatLng(mMapElementDisplay.getCurrentLatLng().toLocation()));
        }
        mMapElementDisplay.updateMarkers();
        mView.hideMarkerFeatureMenu();
    }
    public void onAddWaypointButtonClick() {
        if (mMapElementDisplay.getCurrentLatLng() != null) {
            mMapElementDisplay.addWaypoint(new LatLng(mMapElementDisplay.getCurrentLatLng().toLocation()));
        }
        mMapElementDisplay.updateMarkers();
        mView.hideMarkerFeatureMenu();
    }

    public void onClearWaypointButtonClick() {
        mMapElementDisplay.clearWaypoints();
        mMapElementDisplay.updateMarkers();
        mView.hideMarkerFeatureMenu();
    }
}
