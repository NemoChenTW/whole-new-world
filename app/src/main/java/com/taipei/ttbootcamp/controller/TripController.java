package com.taipei.ttbootcamp.controller;

import android.util.Log;

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
import com.taipei.ttbootcamp.interfaces.ITripOptimizer;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.Instruction;
import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripController implements IPOISearchResult, IPlanResultListener,
                                        IMapElementDisplay.IPositionUpdateListener,
                                        IOptimizeResultListener {
    static private final String TAG = "TripController";

    private RoutingApi mRoutingApi;
    private SearchApi mSearchApi;
    private IGoogleApiService mGoogleApiService;
    private IMapElementDisplay mMapElementDisplay;
    private RoutePlanner mRoutePlanner;
    private ITripOptimizer mTripOptimizer;

    public TripController(RoutingApi routingApi, SearchApi searchApi, IGoogleApiService googleApiService, IMapElementDisplay mapElementDisplay, ITripOptimizer tripOptimizer) {
        mRoutingApi = routingApi;
        mSearchApi = searchApi;
        mGoogleApiService = googleApiService;
        mMapElementDisplay = mapElementDisplay;
        mRoutePlanner = new RoutePlanner(mRoutingApi, this);
        mMapElementDisplay.addPositionUpdateListener(this);
        mTripOptimizer = tripOptimizer;
    }

    @Override
    public void onPOISearchResult(TripData tripData) {
        ArrayList<FuzzySearchResult> searchResult = tripData.getFuzzySearchResults();
        //tripData.setEndPoint(new LatLng(searchResult.get(searchResult.size() - 1).getPosition().toLocation()));
        LocationPoint lastWayPoint = tripData.getWayPoints().get(tripData.getWayPoints().size() - 1);
        tripData.setEndPoint(new LatLng(lastWayPoint.getPosition().toLocation()));
        mRoutePlanner.planRoute(tripData, true);
    }

    public void planRoute(TripData tripData) {
        mRoutePlanner.planRoute(tripData, false);
    }

    public void PlanTrip(TripData tripData, POIGenerator.POITYPE poitype, int radius)
    {
        POIGenerator.queryWithCategory(mSearchApi, tripData, poitype, radius, this);
    }

    public void PlanTripFromMyDrive(TripData tripData, LatLng currentLatLng, String tagName) {
        POIGenerator.queryWithMyDriveAPI(currentLatLng, tagName, 1, tripData, this);
    }

    @Override
    public void onRoutePlanComplete(RouteResponse routeResult, TripData tripData, boolean needOptimize) {
        needOptimize = false;
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
        for (FuzzySearchResult result : tripData.getFuzzySearchResults()) {
            Log.d(TAG, "onOptimizeResult= " + result.getPoi().getName());
        }
        mRoutePlanner.planRoute(tripData, false);
    }
}
