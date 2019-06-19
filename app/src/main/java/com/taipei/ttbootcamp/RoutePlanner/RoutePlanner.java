package com.taipei.ttbootcamp.RoutePlanner;

import android.util.Log;

import com.taipei.ttbootcamp.interfaces.IMapElementDisplay;
import com.taipei.ttbootcamp.interfaces.IPOIWithTravelTimeResult;
import com.taipei.ttbootcamp.interfaces.POIWithTravelTime;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.Instruction;
import com.tomtom.online.sdk.routing.data.InstructionsType;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.routing.data.RouteType;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class RoutePlanner {

    public RoutePlanner(RoutingApi routingApi, IMapElementDisplay mapElementDisplay, IPOIWithTravelTimeResult poiWithTravelTimeResult)
    {
        this.routingApi = routingApi;
        this.mapElementDisplay = mapElementDisplay;
        this.poiWithTravelTimeResult = poiWithTravelTimeResult;
    }

    public void planRoute(LatLng start, LatLng end, ArrayList<FuzzySearchResult> fuzzySearchResults, int i) {
        this.mFuzzySearchResults = new ArrayList<FuzzySearchResult>(fuzzySearchResults);
        ArrayList<LatLng> waypoints = new ArrayList<LatLng>();
        for (FuzzySearchResult fresult : mFuzzySearchResults) {
            waypoints.add(fresult.getPosition());
        }
        waypoints.remove(waypoints.size() - 1);
        planRoute(start, end, waypoints.toArray(new LatLng[0]));
    }

    public void planRoute(LatLng start, LatLng end, LatLng[] waypoints) {
        ArrayList<POIWithTravelTime> result = new ArrayList<POIWithTravelTime>();
        if (start != null && end != null) {
            RouteQuery routeQuery = createRouteQuery(start, end, waypoints);
            routingApi.planRoute(routeQuery)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<RouteResponse>() {
                        @Override
                        public void onSuccess(RouteResponse routeResult) {
                            if (mapElementDisplay != null) {
                                mapElementDisplay.displayRoutes(routeResult.getRoutes());
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
                                        Log.d("Nick", "Found waypoint or arrival! Time: " + instruction.getTravelTimeInSeconds() + " interval: " + (instruction.getTravelTimeInSeconds() - lastTravelTime));
                                        if (mFuzzySearchResults == null) {
                                            result.add(new POIWithTravelTime(mFuzzySearchResults.get(fuzzySearchResultIndex), instruction.getTravelTimeInSeconds() - lastTravelTime));

                                        } else {
                                            result.add(new POIWithTravelTime(new FuzzySearchResult(), instruction.getTravelTimeInSeconds() - lastTravelTime));
                                        }
                                        lastTravelTime = instruction.getTravelTimeInSeconds();
                                        fuzzySearchResultIndex++;
                                    }
                                }
                                if (poiWithTravelTimeResult != null) {
                                    poiWithTravelTimeResult.onPOIWithTravelTimeResult(result);
                                }
                                break;
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            //handleApiError(e);
                            //clearMap();
                        }
                    });
        }
    }

    private RouteQuery createRouteQuery(LatLng start, LatLng stop, LatLng[] wayPoints) {
        return (wayPoints != null && wayPoints.length != 0) ?
                new RouteQueryBuilder(start, stop).withWayPoints(wayPoints).withRouteType(RouteType.FASTEST)
                        .withInstructionsType(InstructionsType.TAGGED).build():
                new RouteQueryBuilder(start, stop).withRouteType(RouteType.FASTEST)
                        .withInstructionsType(InstructionsType.TAGGED).build();
    }

    private RoutingApi routingApi;
    private IMapElementDisplay mapElementDisplay;
    private IPOIWithTravelTimeResult poiWithTravelTimeResult;
    private ArrayList<FuzzySearchResult> mFuzzySearchResults;
}
