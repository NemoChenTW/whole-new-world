package com.taipei.ttbootcamp.Presenter;

import com.taipei.ttbootcamp.data.TripData;
import com.taipei.ttbootcamp.interfaces.IMapElementDisplay;
import com.taipei.ttbootcamp.interfaces.MainActivityView;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.TomtomMapCallback;

public class MainActivityPresenter {
    IMapElementDisplay mMapElementDisplay;
    MainActivityView mView;
    TripData mTripData;
    LatLng mClickPosition;

    public MainActivityPresenter(MainActivityView view) {
        mView = view;
        mTripData = new TripData();
    }

    public void initMainActivityPresenter(IMapElementDisplay mapElementDisplay) {
        mMapElementDisplay = mapElementDisplay;
        mMapElementDisplay.getTomtomMap().addOnMapLongClickListener(onMapLongClickListener);
    }

    private TomtomMapCallback.OnMapLongClickListener onMapLongClickListener =
            latLng -> handleLongClick(latLng);

    private void handleLongClick(LatLng latLng) {
        mView.showMarkerFeatureMenu();
        mClickPosition = latLng;
    }

    public void onDepartureButtonClick() {
        mTripData.setStartPoint(mClickPosition);
        mMapElementDisplay.updateMarkers(mTripData);
        mView.hideMarkerFeatureMenu();
        tryRoutePlan();
    }

    public void onDestinationButtonClick() {
        mTripData.setEndPoint(mClickPosition);
        mMapElementDisplay.updateMarkers(mTripData);
        mView.hideMarkerFeatureMenu();
        tryRoutePlan();
    }
    public void onAddWaypointButtonClick() {
        mTripData.addWaypoints(mClickPosition);
        mMapElementDisplay.updateMarkers(mTripData);
        mView.hideMarkerFeatureMenu();
        tryRoutePlan();
    }

    public void onClearWaypointButtonClick() {
        mTripData.removeWaypoints();
        mMapElementDisplay.updateMarkers(mTripData);
        mView.hideMarkerFeatureMenu();
        tryRoutePlan();
    }

    private void tryRoutePlan() {
        if (mTripData.isAvailableForPlan()) {
            ((IMapElementDisplay.IPositionUpdateListener) mMapElementDisplay).onPositionUpdate(mTripData);
        }
    }

}
