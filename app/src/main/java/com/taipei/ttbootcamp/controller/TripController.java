package com.taipei.ttbootcamp.controller;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.Place;
import com.taipei.ttbootcamp.BuildConfig;
import com.taipei.ttbootcamp.Entities.GoogleGeocode;
import com.taipei.ttbootcamp.PoiGenerator.POIGenerator;
import com.taipei.ttbootcamp.RoutePlanner.RoutePlanner;
import com.taipei.ttbootcamp.data.LocationPoint;
import com.taipei.ttbootcamp.data.TripData;
import com.taipei.ttbootcamp.interfaces.IGoogleApiService;
import com.taipei.ttbootcamp.interfaces.IInteractionDialog;
import com.taipei.ttbootcamp.interfaces.IMapElementDisplay;
import com.taipei.ttbootcamp.interfaces.IOptimizeResultCallBack;
import com.taipei.ttbootcamp.interfaces.IOptimizeResultListener;
import com.taipei.ttbootcamp.interfaces.IPOISearchResult;
import com.taipei.ttbootcamp.interfaces.IPlanResultListener;
import com.taipei.ttbootcamp.interfaces.IPublicItinerarySearchResultCallack;
import com.taipei.ttbootcamp.interfaces.ITripOptimizer;
import com.taipei.ttbootcamp.wrapper.GmsTaskWrapper;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.Instruction;
import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class TripController implements IPOISearchResult, IPlanResultListener, IMapElementDisplay.IPositionUpdateListener,
                                    IPublicItinerarySearchResultCallack, IOptimizeResultListener, IOptimizeResultCallBack {

    static private final String TAG = "TripController";

    private RoutingApi mRoutingApi;
    private SearchApi mSearchApi;
    private IGoogleApiService mGoogleApiService;
    private PlacesClient mGooglePlaceClient;
    private IMapElementDisplay mMapElementDisplay;
    private RoutePlanner mRoutePlanner;
    private ITripOptimizer mTripOptimizer;
    private IInteractionDialog mInteractionDialog;
    public boolean hadDoneOptimize;

    public TripController(RoutingApi routingApi, SearchApi searchApi, IGoogleApiService googleApiService, PlacesClient googlePlaceClient, IMapElementDisplay mapElementDisplay, ITripOptimizer tripOptimizer, IInteractionDialog interactionDialog) {
        mRoutingApi = routingApi;
        mSearchApi = searchApi;
        mGoogleApiService = googleApiService;
        mGooglePlaceClient = googlePlaceClient;
        mMapElementDisplay = mapElementDisplay;
        mRoutePlanner = new RoutePlanner(mRoutingApi, this);
        mMapElementDisplay.addPositionUpdateListener(this);
        mTripOptimizer = tripOptimizer;
        mInteractionDialog = interactionDialog;
        hadDoneOptimize = false;
    }

    @Override
    public void onPOISearchResult(TripData tripData) {
        tripData.updateWaypointFromSearchResults();
        ArrayList<FuzzySearchResult> searchResult = tripData.getFuzzySearchResults();

        if (!tripData.isUseMyDriveData() && searchResult != null && !searchResult.isEmpty()) {
            tripData.setEndPoint(new LatLng(searchResult.get(searchResult.size() - 1).getPosition().toLocation()));
        }
        else if (tripData.isUseMyDriveData()){
            // From MyDrive
            if (tripData.getWayPoints().isEmpty()) return;
            LocationPoint lastWayPoint = tripData.getWayPoints().get(tripData.getWayPoints().size() - 1);
            tripData.setEndPoint(new LatLng(lastWayPoint.getPosition().toLocation()));
        }
        //tripData.setEndPoint(new LatLng(searchResult.get(searchResult.size() - 1).getPosition().toLocation()));
        // TODO Add updateWaypointFromMyDirveResults
        LocationPoint lastWayPoint = tripData.getWayPoints().get(tripData.getWayPoints().size() - 1);
        tripData.setEndPoint(new LatLng(lastWayPoint.getPosition().toLocation()));
        /* From search
        tripData.setEndPoint(new LatLng(searchResult.get(searchResult.size() - 1).getPosition().toLocation()));
        tripData.updateWaypointFromSearchResults();
        */
        updatePOIDetails(tripData);
        mRoutePlanner.planRoute(tripData, true);
    }

    private void updatePOIDetails(TripData tripData) {

        final int[] index = {0};

        for (LocationPoint point : tripData.getWayPoints()) {
            Log.d(TAG, "Point= " + point.getName());
        }

        ArrayList<LocationPoint> finalWayPoints = tripData.getWayPoints();

        Observable.fromIterable(tripData.getWayPoints())
                .concatMap(wayPoint -> mGoogleApiService.getGeocode(wayPoint.getName(), BuildConfig.ApiKey))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .concatMap(googleGeocode -> getPlaceResponseObservable(mGooglePlaceClient, googleGeocode)
                        .onErrorReturnItem(new FetchPlaceResponse() {
                            @NonNull
                            @Override
                            public Place getPlace() {
                                return null;
                            }
                        })
                        .onErrorReturnItem(new FetchPlaceResponse() {
                            @NonNull
                            @Override
                            public Place getPlace() {
                                return null;
                            }
                        }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<FetchPlaceResponse>() {
                    @Override
                    public void onNext(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        if (place != null) {
                            Log.d(TAG, "Place found: name: " + place.getName()
                                    + ", rating: " + place.getRating());
                        } else {
                            Log.d(TAG, "Empty place");
                        }

//                        int openingTime = place.getOpeningHours().getPeriods().get(index[0]).getOpen().getTime().getHours() * 3600 +
//                                place.getOpeningHours().getPeriods().get(index[0]).getOpen().getTime().getMinutes() * 60;
//                        int closedTime = place.getOpeningHours().getPeriods().get(index[0]).getClose().getTime().getHours() * 3600 +
//                                place.getOpeningHours().getPeriods().get(index[0]).getOpen().getTime().getMinutes() * 60;

//                        finalWayPoints.get(index[0]).setOpeningHours(openingTime, closedTime);

                        index[0]++;

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete");
//                        tripData.removeWaypoints();
//                        for (LocationPoint wayPoint : finalWayPoints) {
//                            tripData.addWaypoints(wayPoint);
//                        }
                    }
                });
    }


    private Observable<FetchPlaceResponse> getPlaceResponseObservable(final PlacesClient placesClient, final GoogleGeocode googleGeocode) {
        if (googleGeocode.getResults().isEmpty()) {
            return Observable.error(new RuntimeException("Empty googleGeocode"));
        }
        String placeId = googleGeocode.getResults().get(0).getPlace_id();
        String name = googleGeocode.getResults().get(0).getFormatted_address();
        Log.d(TAG, "Place_id = " + placeId + ", addr= " + name);

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.OPENING_HOURS, Place.Field.RATING);
        // Construct a request object, passing the place ID and fields array.
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        return GmsTaskWrapper.asObservable(placesClient.fetchPlace(request));
    }

    public void planRoute(TripData tripData) {
        mRoutePlanner.planRoute(tripData, false);
    }

    public void PlanTrip(TripData tripData, POIGenerator.POITYPE poitype, int radius)
    {
        POIGenerator.queryWithCategory(mSearchApi, tripData, poitype, radius, this);
    }

    public void PlanTripFromMyDrive(TripData tripData, LatLng currentLatLng, String tagName) {
        POIGenerator.queryWithMyDriveAPI(currentLatLng, tagName, 5, tripData, this);
    }

    public void SelectMyDriveItineraryWithIndex(TripData tripData, int index) {
        tripData.setSelectedItineraryIndex(index);
        onPOISearchResult(tripData);
    }

    //
    private boolean isEatingTime(int arrivalTime){
        int secondOfHour = 3600;
        if(arrivalTime >= 11 * secondOfHour && arrivalTime <= 13 * secondOfHour){
            Log.d(TAG, "Eating Time!");
            return true;
        }
    //        if(arrivalTime >= 18 * secondOfHour && arrivalTime <= 20 * secondOfHour){
    //            return true;
    //        }
        return false;
    }

    private boolean isOpeningHour(int arrivalTime, int openingTime, int closedTime){
        if(arrivalTime >= openingTime && arrivalTime <= closedTime){
            return true;
        }
        return false;
    }

    @Override
    public void onRoutePlanComplete(RouteResponse routeResult, TripData tripData, boolean needOptimize) {

        int stayTime = tripData.getStayTime();
        ArrayList<Integer> fuzzySearchResultTravelTimes = prepareOptimizeData(routeResult, tripData.getFuzzySearchResults(), stayTime);
        ArrayList<Integer> fuzzySearchResultArrivalTimes = new ArrayList<Integer>();

        tripData.setFuzzySearchResultTravelTimes(fuzzySearchResultTravelTimes);

        int second = 0;
        int minute = 0;
        int hour = 9;
        int currentTime = second + minute * 60 + hour * 3600;

        int restaurantIdx = -1;

        for(int i = 0; i < fuzzySearchResultTravelTimes.size(); i++){
            currentTime += fuzzySearchResultTravelTimes.get(i);
            Log.d(TAG, "CurrentTime: " + currentTime);
            fuzzySearchResultArrivalTimes.add(currentTime);
        }

        tripData.setFuzzySearchResultArrivalTimes(fuzzySearchResultArrivalTimes);

        Log.d(TAG, "searchResultArrivalTimes: " + tripData.getFuzzySearchResultArrivalTimes());

        ArrayList<Integer> openingTime = tripData.getWaypointsOpeningTime();
        ArrayList<Integer> closedTime = tripData.getWaypointsClosedTime();

        for(int i = 0; i < fuzzySearchResultArrivalTimes.size(); i++){
            if(isEatingTime(fuzzySearchResultArrivalTimes.get(i))){
                restaurantIdx = i;
                break;
            }
        }

        if(restaurantIdx == -1){
            needOptimize = false;
        }
        else{
            needOptimize = true;
        }

        Log.d(TAG, "Optimized: " + needOptimize);

        if (mMapElementDisplay != null) {
            // Remove the markers which set by press
            mMapElementDisplay.removeMarkers();
            mMapElementDisplay.displayRoutes(routeResult.getRoutes(), tripData);
        }

        mInteractionDialog.setResultDialog(tripData, !hadDoneOptimize && needOptimize, restaurantIdx);
        hadDoneOptimize = true;
    }

    @Override
    public void optimizeWithRestaurant(TripData tripData, boolean isOptimize, int restaurantIdx) {
        if (isOptimize){
            Log.d(TAG, "Optimize Trip: " + restaurantIdx);
            mTripOptimizer.optimizeTrip(tripData, restaurantIdx);
        }
    }

    private ArrayList<Integer> prepareOptimizeData(RouteResponse routeResult, ArrayList<FuzzySearchResult> originalSearchResult, int stayTime) {
        ArrayList<Integer> fuzzySearchResultTravelTimes = new ArrayList<Integer>();

        for (FullRoute route: routeResult.getRoutes())
        {
            int lastTravelTime = 0;
            for (Instruction instruction : route.getGuidance().getInstructions())
            {
                if (instruction.getInstructionType().equals("LOCATION_WAYPOINT") ||
                        instruction.getInstructionType().equals("LOCATION_ARRIVAL"))
                {
                    fuzzySearchResultTravelTimes.add(instruction.getTravelTimeInSeconds() + stayTime - lastTravelTime);
                    //
                    Log.d(TAG, "Found waypoint or arrival! Time: " + instruction.getTravelTimeInSeconds()
                            + " interval: " + (instruction.getTravelTimeInSeconds() - lastTravelTime));
                    // remove this for only adding travel time once
    //                    fuzzySearchResultTravelTimes.add(instruction.getTravelTimeInSeconds() - lastTravelTime);
                    lastTravelTime = instruction.getTravelTimeInSeconds();
                }
            }
        }

        Log.d(TAG, "fuzzySearchResultTravelTimes= " + fuzzySearchResultTravelTimes);

        return fuzzySearchResultTravelTimes;
    }


    @Override
    public void onPositionUpdate(TripData tripData) {
        planRoute(tripData);
    }

    @Override
    public void onOptimizeResult(TripData tripData) {
        Log.e(TAG, "On optimize result");
        if (tripData.getFuzzySearchResults() != null) {
            for (FuzzySearchResult result : tripData.getFuzzySearchResults()) {
                Log.d(TAG, "onOptimizeResult= " + result.getPoi().getName());
            }
        }
        mRoutePlanner.planRoute(tripData, false);
    }

    @Override
    public void onPublicItinerarySearchResult(TripData tripData) {
//        Log.e(TAG, "onPublicItinerarySearchResult Itineraries size: " + tripData.getMyDriveItineraries().size());
        //tripData.getMyDriveItineraries().get(0).getName()

        // UI call this
        //SelectMyDriveItineraryWithIndex(tripData, 0);
    }

}
