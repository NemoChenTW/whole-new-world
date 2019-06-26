package com.taipei.ttbootcamp.RoutePlanner;

import com.taipei.ttbootcamp.interfaces.IFirstPlanResultListener;
import com.taipei.ttbootcamp.interfaces.IPOIWithTravelTimeResult;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.routing.RoutingApi;
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

    private RoutingApi routingApi;
    private IFirstPlanResultListener firstPlanResultListener;
    private ArrayList<FuzzySearchResult> mInputSearchResults;

    public RoutePlanner(RoutingApi routingApi, IFirstPlanResultListener firstPlanResultListener)
    {
        this.routingApi = routingApi;
        this.firstPlanResultListener = firstPlanResultListener;
    }

    public void planRoute(LatLng start, LatLng end, ArrayList<FuzzySearchResult> fuzzySearchResults, int i) {
        this.mInputSearchResults = new ArrayList<FuzzySearchResult>(fuzzySearchResults);
        ArrayList<LatLng> waypoints = new ArrayList<LatLng>();
        for (FuzzySearchResult fresult : mInputSearchResults) {
            waypoints.add(fresult.getPosition());
        }
        waypoints.remove(waypoints.size() - 1);
        planRoute(start, end, waypoints.toArray(new LatLng[0]));
    }

    public void planRoute(LatLng start, LatLng end, LatLng[] waypoints) {
        if (start != null && end != null) {
            RouteQuery routeQuery = createRouteQuery(start, end, waypoints);
            routingApi.planRoute(routeQuery)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<RouteResponse>() {
                        @Override
                        public void onSuccess(RouteResponse routeResult) {
                            firstPlanResultListener.onRoutePlanComplete(routeResult, mInputSearchResults);
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
}
