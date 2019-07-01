package com.taipei.ttbootcamp.implementations;

import android.util.Log;

import com.taipei.ttbootcamp.Utils.Utlis;
import com.taipei.ttbootcamp.interfaces.IOptimizeResultListener;
import com.taipei.ttbootcamp.interfaces.ITripOptimizer;
import com.taipei.ttbootcamp.interfaces.POIWithTravelTime;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.common.location.LatLngAcc;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.InstructionsType;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.routing.data.RouteType;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class TripOptimizer implements ITripOptimizer {
    private static final String TAG = "TripOptimizer";
    private SearchApi mSearchApi;
    private RoutingApi mRoutingApi;
    private IOptimizeResultListener mOptimizeResultListener;

    public TripOptimizer(SearchApi searchApi, RoutingApi routingApi) {
        mSearchApi = searchApi;
        mRoutingApi = routingApi;
    }

    public void setOptimizeResultListener(IOptimizeResultListener optimizeResultListener) {
        mOptimizeResultListener = optimizeResultListener;
    }

    public void optimizeTrip(ArrayList<POIWithTravelTime> poiWithTravelTimeList) {
        optimizeWithPetrolStation(poiWithTravelTimeList);

    }

    private void optimizeWithPetrolStation(ArrayList<POIWithTravelTime> poiWithTravelTimeList) {
        ArrayList<FuzzySearchResult> storedSearchResult = Utlis.getFuzzySearchResultsfromPOIWithTravelTime(poiWithTravelTimeList);
        FuzzySearchResult targetLocation = storedSearchResult.get(storedSearchResult.size() / 2);
        Log.d(TAG, "targetLocation= " + targetLocation.getId());
        LatLng targetSearchCenter = targetLocation.getPosition();

        mSearchApi.search(new FuzzySearchQueryBuilder("petrol")
                .withPreciseness(new LatLngAcc(targetSearchCenter, 100000))
                .withTypeAhead(true)
                .withCategory(true)
                .withLimit(1).build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                // Plan route with search result
                .flatMap(fuzzySearchResponse -> mRoutingApi.planRoute(
                        createRouteQuery(new LatLng(25.046570, 121.515313), fuzzySearchResponse, storedSearchResult)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<RouteResponse>() {
                    @Override
                    public void onSuccess(RouteResponse routeResult) {
                        submitOptimizeResult(storedSearchResult);
                    }

                    @Override
                    public void onError(Throwable e) {
                        //handleApiError(e);
                        //clearMap();
                    }
                });
    }

    private RouteQuery createRouteQuery(LatLng start, FuzzySearchResponse fuzzySearchResponse, ArrayList<FuzzySearchResult> storedSearchResult) {
        FuzzySearchResult newPosition = fuzzySearchResponse.getResults().get(0);

        final int targetIndex = storedSearchResult.size() / 2;
        storedSearchResult.add(targetIndex + 1, newPosition);

        ArrayList<LatLng> wayPoints = Utlis.toLatLngArrayList(storedSearchResult);
        final int wayPointSize = wayPoints.size();
        LatLng stop = new LatLng(wayPoints.get(wayPointSize - 1).toLocation());
        wayPoints.remove(wayPointSize - 1);
        LatLng waypointArray[] = new LatLng[wayPoints.size()];
        waypointArray = wayPoints.toArray(waypointArray);

        return new RouteQueryBuilder(start, stop).withRouteType(RouteType.FASTEST)
                                                .withInstructionsType(InstructionsType.TAGGED)
                                                .withWayPoints(waypointArray).build();
    }

    private void submitOptimizeResult(ArrayList<FuzzySearchResult> optimizeSearchResult) {
        mOptimizeResultListener.onOptimizeResult(optimizeSearchResult);
    }
}
