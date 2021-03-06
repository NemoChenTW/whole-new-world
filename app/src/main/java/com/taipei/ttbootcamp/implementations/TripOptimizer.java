package com.taipei.ttbootcamp.implementations;

import android.util.Log;

import com.taipei.ttbootcamp.data.LocationPoint;
import com.taipei.ttbootcamp.data.TripData;
import com.taipei.ttbootcamp.interfaces.IOptimizeResultListener;
import com.taipei.ttbootcamp.interfaces.ITripOptimizer;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import io.reactivex.Single;
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

    public void optimizeTrip(TripData tripData, int index) {
//        optimizeWithPetrolStation(tripData, index);
        optimizeWithRestaurants(tripData, index);
    }

    private void optimizeWithRestaurants(TripData tripData, int index) {
        //ArrayList<FuzzySearchResult> storedSearchResult = tripData.getFuzzySearchResults();
        //FuzzySearchResult targetLocation = storedSearchResult.get(storedSearchResult.size() / 2);
        //Log.d(TAG, "targetLocation= " + targetLocation.getId());

        LatLng targetSearchCenter = tripData.getWayPoints().get(index).getPosition();

        Log.d(TAG, "Optimized Point: " + targetSearchCenter);

        mSearchApi.search(new FuzzySearchQueryBuilder("restaurant")
                .withPreciseness(new LatLngAcc(targetSearchCenter, 100000))
                .withTypeAhead(true)
                .withCategory(true)
                .withLimit(1).build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                // Plan route with search result
                .flatMap(fuzzySearchResponse -> mRoutingApi.planRoute(
                        createRouteQuery(tripData, fuzzySearchResponse, index)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<RouteResponse>() {
                    @Override
                    public void onSuccess(RouteResponse routeResult) {
                        Log.d(TAG, "Complete optimize Trip!");
                        submitOptimizeResult(tripData);
                    }

                    @Override
                    public void onError(Throwable e) {
                        //handleApiError(e);
                        //clearMap();
                        Log.e(TAG, "Optimize restaurant error.");
                    }
                });
    }

    private void optimizeWithPetrolStation(TripData tripData, int index) {
        //ArrayList<FuzzySearchResult> storedSearchResult = tripData.getFuzzySearchResults();
        //FuzzySearchResult targetLocation = storedSearchResult.get(storedSearchResult.size() / 2);
        //Log.d(TAG, "targetLocation= " + targetLocation.getId());

        Log.e(TAG, "Optimize Petrol");
        LatLng targetSearchCenter = tripData.getWayPoints().get(tripData.getWayPoints().size() / 2).getPosition();

        mSearchApi.search(new FuzzySearchQueryBuilder("petrol")
                .withPreciseness(new LatLngAcc(targetSearchCenter, 100000))
                .withTypeAhead(true)
                .withCategory(true)
                .withLimit(1).build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                // Plan route with search result
                .flatMap(fuzzySearchResponse -> mRoutingApi.planRoute(
                        createRouteQuery(tripData, fuzzySearchResponse, index)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<RouteResponse>() {
                    @Override
                    public void onSuccess(RouteResponse routeResult) {
                        Log.e(TAG,"Petrol success");
                        submitOptimizeResult(tripData);
                    }

                    @Override
                    public void onError(Throwable e) {
                        //handleApiError(e);
                        //clearMap();
                        Log.e(TAG,"Petrol error");
                        submitOptimizeResult(tripData);
                    }
                });
    }

    private RouteQuery createRouteQuery(@NotNull TripData tripData) {
        RouteQueryBuilder routeQueryBuilder = new RouteQueryBuilder(tripData.getStartPoint(), tripData.getEndPoint())
                .withRouteType(RouteType.FASTEST)
                .withInstructionsType(InstructionsType.TAGGED);
        return (tripData.hasWaypoints()) ? routeQueryBuilder.withWayPoints(tripData.getWaypointsLatLng()).build() : routeQueryBuilder.build();
    }

    private RouteQuery createRouteQuery(@NotNull TripData tripData, @NotNull FuzzySearchResponse fuzzySearchResponse, int targetIndex) {
        FuzzySearchResult newPosition = fuzzySearchResponse.getResults().get(0);
        Log.e(TAG, "create route query " + newPosition.getPoi().getName());
        // Update FuzzySearchResults with new search point
        ArrayList<FuzzySearchResult> searchResults = tripData.getFuzzySearchResults();

        if (!tripData.isUseMyDriveData() && searchResults != null) {
            Log.e(TAG, "create route query searchResults: " + searchResults);
            Log.e(TAG, "create route query searchResults: " + searchResults.size());
//            final int targetIndex = searchResults.size() / 2;
            searchResults.add(targetIndex + 1, newPosition);

            tripData.setFuzzySearchResults(searchResults);

            if (tripData.isWaypointsNeedUpdate()) {
                tripData.updateWaypointFromSearchResults();
            }
            tripData.setEndPoint(new LatLng(searchResults.get(searchResults.size() - 1).getPosition().toLocation()));
        }
        else if (tripData.isUseMyDriveData() && tripData.getMyDriveItineraries() != null) {
            if (tripData.isWaypointsNeedUpdate()) {
                tripData.updateWaypointFromSearchResults();
            }

//            final int targetIndex = tripData.getWayPoints().size() / 2;
            tripData.addWaypoints(targetIndex + 1, new LocationPoint(newPosition.getPosition(), newPosition.getPoi().getName()));
            //tripData.setEndPoint(new LatLng(tripData.getWayPoints().get(tripData.getWayPoints().size() - 1).getPosition()));

            for (LocationPoint lp : tripData.getWayPoints()) {
                Log.e(TAG, "waypoint: " + lp.getName());
            }
        }
        return new RouteQueryBuilder(tripData.getStartPoint(), tripData.getEndPoint())
                                                .withRouteType(RouteType.FASTEST)
                                                .withInstructionsType(InstructionsType.TAGGED)
                                                .withWayPoints(tripData.getWaypointsLatLng()).build();
    }

    private void submitOptimizeResult(TripData tripData) {
        Log.e(TAG, "submitOptimizeResut");
        mOptimizeResultListener.onOptimizeResult(tripData);
    }
}
