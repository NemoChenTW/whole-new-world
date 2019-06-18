package com.taipei.ttbootcamp.RoutePlanner;

import android.util.Log;

import com.taipei.ttbootcamp.interfaces.IMapElementDisplay;
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

    public RoutePlanner(TomtomMap tomtomMap, RoutingApi routingApi, IMapElementDisplay mapElementDisplay)
    {
        this.tomtomMap = tomtomMap;
        this.routingApi = routingApi;
        this.mapElementDisplay = mapElementDisplay;
    }

    public void planRoute(LatLng start, LatLng end, LatLng[] waypoints) {
        ArrayList<POIWithTravelTime> result = new ArrayList<POIWithTravelTime>();
        if (start != null && end != null) {
            tomtomMap.clearRoute();
            RouteQuery routeQuery = createRouteQuery(start, end, waypoints);
            routingApi.planRoute(routeQuery)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<RouteResponse>() {
                        @Override
                        public void onSuccess(RouteResponse routeResult) {
                            mapElementDisplay.displayRoutes(routeResult.getRoutes());
                            Log.d("Nick", routeResult.toString());
                            for (FullRoute route: routeResult.getRoutes())
                            {
                                Log.d("Nick", route.getCoordinates().toString());
                                for (Instruction instruction : route.getGuidance().getInstructions())
                                {
                                    FuzzySearchResult fuzzySearchResult = new FuzzySearchResult();
                                    POIWithTravelTime poiWithTravelTime = new POIWithTravelTime(fuzzySearchResult, 0);
                                    poiWithTravelTime.travelTime = 0;
                                    result.add(poiWithTravelTime);
                                    if (instruction.getInstructionType().equals("LOCATION_WAYPOINT") ||
                                            instruction.getInstructionType().equals("LOCATION_ARRIVAL"))
                                    {
                                        poiWithTravelTime.travelTime = instruction.getTravelTimeInSeconds();
                                        result.add(poiWithTravelTime);
                                        Log.d("Nick", "Found waypoint or arrival! Time: " + instruction.getTravelTimeInSeconds());
                                    }
                                }
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


    private TomtomMap tomtomMap;
    private RoutingApi routingApi;
    private IMapElementDisplay mapElementDisplay;
}
