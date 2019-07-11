package com.taipei.ttbootcamp.Presenter;

import android.util.Log;

import com.taipei.ttbootcamp.data.LocationPoint;
import com.taipei.ttbootcamp.data.TripData;
import com.taipei.ttbootcamp.interfaces.IMapElementDisplay;
import com.taipei.ttbootcamp.interfaces.MainActivityView;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.TomtomMapCallback;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchQueryBuilder;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchResponse;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivityPresenter {
    private static final String TAG = "MainActivityPresenter";
    private IMapElementDisplay mMapElementDisplay;
    private SearchApi mSearchApi;
    private MainActivityView mView;
    private TripData mTripData;
    private LatLng mClickPosition;
    private LocationPoint mLocationPoint;

    public MainActivityPresenter(MainActivityView view) {
        mView = view;
        mTripData = new TripData();
    }

    public void initMainActivityPresenter(IMapElementDisplay mapElementDisplay, SearchApi searchApi) {
        mMapElementDisplay = mapElementDisplay;
        mSearchApi = searchApi;
        mMapElementDisplay.getTomtomMap().addOnMapLongClickListener(onMapLongClickListener);
    }

    private TomtomMapCallback.OnMapLongClickListener onMapLongClickListener =
            latLng -> handleLongClick(latLng);

    private void handleLongClick(LatLng latLng) {
        mView.showMarkerFeatureMenu();
        mClickPosition = latLng;
        mLocationPoint = new LocationPoint(latLng);
        mSearchApi.reverseGeocoding(ReverseGeocoderSearchQueryBuilder.create(latLng.getLatitude(), latLng.getLongitude()).build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<ReverseGeocoderSearchResponse>() {

                    @Override
                    public void onSuccess(ReverseGeocoderSearchResponse reverseGeocoderSearchResponse) {
                        String streetName = reverseGeocoderSearchResponse.getAddresses().get(0).getAddress().getStreetName();
                        mLocationPoint.setName(streetName);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Search error: " + e.getMessage());
                    }
                });
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
        mTripData.addWaypoints(mLocationPoint);
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
