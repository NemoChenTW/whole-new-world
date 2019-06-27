package com.taipei.ttbootcamp.controller;

import android.util.Log;

import com.taipei.ttbootcamp.RoutePlanner.RoutePlanner;
import com.taipei.ttbootcamp.dailydecorator.DailyNeedDecorator;
import com.taipei.ttbootcamp.interfaces.IFirstPlanResultListener;
import com.taipei.ttbootcamp.interfaces.IMapElementDisplay;
import com.taipei.ttbootcamp.interfaces.IPOISearchResult;
import com.taipei.ttbootcamp.interfaces.IPOIWithTravelTimeResult;
import com.taipei.ttbootcamp.interfaces.POIWithTravelTime;
import com.taipei.ttbootcamp.poigenerator.POIGenerator;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.Instruction;
import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;

public class TripController implements IPOISearchResult, IFirstPlanResultListener {
    static private final String TAG = "TripController";

    private RoutingApi mRoutingApi;
    private SearchApi mSearchApi;
    private IMapElementDisplay mMapElementDisplay;
    private LatLng mCurrentPosition;
    private RoutePlanner mRoutePlanner;
    private DailyNeedDecorator dailyNeedDecorator;

    public TripController(RoutingApi routingApi, SearchApi searchApi, IMapElementDisplay mapElementDisplay)
    {
        mRoutingApi = routingApi;
        mSearchApi = searchApi;
        mMapElementDisplay = mapElementDisplay;
        mRoutePlanner = new RoutePlanner(mRoutingApi, this);
        dailyNeedDecorator = new DailyNeedDecorator(mRoutingApi, mSearchApi, new IPOIWithTravelTimeResult() {
            @Override
            public void onPOIWithTravelTimeResult(ArrayList<POIWithTravelTime> result) {

            }
        });
    }

    @Override
    public void onPOISearchResult(ArrayList<FuzzySearchResult> searchResult) {
        LatLng destination = new LatLng(searchResult.get(searchResult.size() - 1).getPosition().toLocation());
        mRoutePlanner.planRoute(mCurrentPosition, destination, searchResult, 0);
    }

    public void PlanTrip(LatLng currentPosition, POIGenerator.POITYPE poitype, int radius)
    {
        mCurrentPosition = currentPosition;
        POIGenerator.getPOIsWithType(mSearchApi, mCurrentPosition, poitype, radius, this);
    }

    @Override
    public void onRoutePlanComplete(RouteResponse routeResult, ArrayList<FuzzySearchResult> originalSearchResult) {
        ArrayList<POIWithTravelTime> result = new ArrayList<POIWithTravelTime>();
        if (mMapElementDisplay != null) {
            mMapElementDisplay.displayRoutes(routeResult.getRoutes());
            // Remove the markers which set by press
            mMapElementDisplay.removeMarkers();
        }
        for (FullRoute route: routeResult.getRoutes())
        {
            FuzzySearchResult fuzzySearchResult = new FuzzySearchResult();
            POIWithTravelTime poiWithTravelTime = new POIWithTravelTime(fuzzySearchResult, 0);
            result.add(poiWithTravelTime);
            int lastTravelTime = 0;
            int fuzzySearchResultIndex = 0;

            for (Instruction instruction : route.getGuidance().getInstructions())
            {
                if (instruction.getInstructionType().equals("LOCATION_WAYPOINT") ||
                        instruction.getInstructionType().equals("LOCATION_ARRIVAL"))
                {
                    Log.d(TAG, "Found waypoint or arrival! Time: " + instruction.getTravelTimeInSeconds()
                                            + " interval: " + (instruction.getTravelTimeInSeconds() - lastTravelTime));
                    if (originalSearchResult != null) {
                        result.add(new POIWithTravelTime(originalSearchResult.get(fuzzySearchResultIndex), instruction.getTravelTimeInSeconds() - lastTravelTime));

                    } else {
                        result.add(new POIWithTravelTime(new FuzzySearchResult(), instruction.getTravelTimeInSeconds() - lastTravelTime));
                    }
                    lastTravelTime = instruction.getTravelTimeInSeconds();
                    fuzzySearchResultIndex++;
                }
            }
        }
        Log.d(TAG, "result= " + result);
    }
}
