package com.taipei.ttbootcamp.RoutePlanner;

import android.util.Log;

import com.taipei.ttbootcamp.data.TripData;
import com.taipei.ttbootcamp.interfaces.IPlanResultListener;
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

    private static final String TAG = "RoutePlanner";
    private RoutingApi routingApi;
    private IPlanResultListener mPlanResultListener;
    private ArrayList<FuzzySearchResult> mInputSearchResults;

    public RoutePlanner(RoutingApi routingApi, IPlanResultListener planResultListener)
    {
        this.routingApi = routingApi;
        this.mPlanResultListener = planResultListener;
    }

    public void planRoute(TripData tripData, boolean needOptimize) {
        Log.d(TAG, "planRoute with needOptimize= " + needOptimize);
        mInputSearchResults = tripData.getFuzzySearchResults();
        if (tripData.isWaypointsNeedUpdate()) {
            tripData.updateWaypointFromSearchResults();
        }

        if (tripData.isAvailableForPlan()) {
            RouteQuery routeQuery = createRouteQuery(tripData);
            routingApi.planRoute(routeQuery)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<RouteResponse>() {
                        @Override
                        public void onSuccess(RouteResponse routeResult) {
                            mPlanResultListener.onRoutePlanComplete(routeResult, tripData, needOptimize);
                        }

                        @Override
                        public void onError(Throwable e) {
                            //handleApiError(e);
                            //clearMap();
                        }
                    });
        }
    }

    private RouteQuery createRouteQuery(TripData tripData) {
        RouteQueryBuilder routeQueryBuilder = new RouteQueryBuilder(tripData.getStartPoint(), tripData.getEndPoint())
                                                    .withRouteType(RouteType.FASTEST)
                                                    .withInstructionsType(InstructionsType.TAGGED);
        return (tripData.hasWaypoints()) ? routeQueryBuilder.withWayPoints(tripData.getWaypointsLatLng()).build() : routeQueryBuilder.build();
    }
}
