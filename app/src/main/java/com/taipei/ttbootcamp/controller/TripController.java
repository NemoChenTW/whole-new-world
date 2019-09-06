package com.taipei.ttbootcamp.controller;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.taipei.ttbootcamp.BuildConfig;
import com.taipei.ttbootcamp.Entities.GoogleGeocode;
import com.taipei.ttbootcamp.PoiGenerator.POIGenerator;
import com.taipei.ttbootcamp.RoutePlanner.RoutePlanner;
import com.taipei.ttbootcamp.data.LocationPoint;
import com.taipei.ttbootcamp.data.TripData;
import com.taipei.ttbootcamp.interfaces.IGoogleApiService;
import com.taipei.ttbootcamp.interfaces.IMapElementDisplay;
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

public class TripController implements IPOISearchResult, IPlanResultListener,
                                        IMapElementDisplay.IPositionUpdateListener,
                                        IPublicItinerarySearchResultCallack, IOptimizeResultListener {
    static private final String TAG = "TripController";

    private RoutingApi mRoutingApi;
    private SearchApi mSearchApi;
    private IGoogleApiService mGoogleApiService;
    private PlacesClient mGooglePlaceClient;
    private IMapElementDisplay mMapElementDisplay;
    private RoutePlanner mRoutePlanner;
    private ITripOptimizer mTripOptimizer;

    public TripController(RoutingApi routingApi, SearchApi searchApi, IGoogleApiService googleApiService, PlacesClient googlePlaceClient, IMapElementDisplay mapElementDisplay, ITripOptimizer tripOptimizer) {
        mRoutingApi = routingApi;
        mSearchApi = searchApi;
        mGoogleApiService = googleApiService;
        mGooglePlaceClient = googlePlaceClient;
        mMapElementDisplay = mapElementDisplay;
        mRoutePlanner = new RoutePlanner(mRoutingApi, this);
        mMapElementDisplay.addPositionUpdateListener(this);
        mTripOptimizer = tripOptimizer;
    }

    @Override
    public void onPOISearchResult(TripData tripData) {
        tripData.updateWaypointFromSearchResults();
        ArrayList<FuzzySearchResult> searchResult = tripData.getFuzzySearchResults();

        if (searchResult != null && !searchResult.isEmpty()) {
            tripData.setEndPoint(new LatLng(searchResult.get(searchResult.size() - 1).getPosition().toLocation()));
        }
        else {
            // From MyDrive
            if (tripData.getWayPoints().isEmpty()) return;
            LocationPoint lastWayPoint = tripData.getWayPoints().get(tripData.getWayPoints().size() - 1);
            tripData.setEndPoint(new LatLng(lastWayPoint.getPosition().toLocation()));
        }

        updatePOIDetails(tripData);
        mRoutePlanner.planRoute(tripData, true);
    }

    private void updatePOIDetails(TripData tripData) {
        for (LocationPoint point : tripData.getWayPoints()) {
            Log.d(TAG, "Point= " + point.getName());
        }
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
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete");
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

    @Override
    public void onRoutePlanComplete(RouteResponse routeResult, TripData tripData, boolean needOptimize) {
        Log.e(TAG, "onRoutePlanComplete: " + needOptimize);
        //needOptimize = false;
        if (needOptimize) {
            tripData.setFuzzySearchResultTravelTimes(prepareOptimizeData(routeResult, tripData.getFuzzySearchResults()));
            mTripOptimizer.optimizeTrip(tripData);
        } else {
            if (mMapElementDisplay != null) {
                // Remove the markers which set by press
                mMapElementDisplay.removeMarkers();
                mMapElementDisplay.displayRoutes(routeResult.getRoutes(), tripData);
            }
        }
    }

    private ArrayList<Integer> prepareOptimizeData(RouteResponse routeResult, ArrayList<FuzzySearchResult> originalSearchResult) {
        ArrayList<Integer> fuzzySearchResultTravelTimes = new ArrayList<Integer>();

        for (FullRoute route: routeResult.getRoutes())
        {
            int lastTravelTime = 0;
            for (Instruction instruction : route.getGuidance().getInstructions())
            {
                if (instruction.getInstructionType().equals("LOCATION_WAYPOINT") ||
                        instruction.getInstructionType().equals("LOCATION_ARRIVAL"))
                {
                    fuzzySearchResultTravelTimes.add(instruction.getTravelTimeInSeconds() - lastTravelTime);
                    Log.d(TAG, "Found waypoint or arrival! Time: " + instruction.getTravelTimeInSeconds()
                                            + " interval: " + (instruction.getTravelTimeInSeconds() - lastTravelTime));
                    fuzzySearchResultTravelTimes.add(instruction.getTravelTimeInSeconds() - lastTravelTime);
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
        Log.e(TAG, "onPublicItinerarySearchResult Itineraries size: " + tripData.getMyDriveItineraries().size());
        //tripData.getMyDriveItineraries().get(0).getName()

        // UI call this
        //SelectMyDriveItineraryWithIndex(tripData, 0);
    }
}
