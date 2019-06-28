package com.taipei.ttbootcamp.implementations;

import com.taipei.ttbootcamp.interfaces.ITripOptimizer;
import com.taipei.ttbootcamp.interfaces.IOptimizeResultListener;
import com.taipei.ttbootcamp.interfaces.POIWithTravelTime;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;

public class TripOptimizer implements ITripOptimizer {
    private SearchApi mSearchApi;
    private IOptimizeResultListener mOptimizeResultListener;

    public TripOptimizer(SearchApi searchApi) {
        mSearchApi = searchApi;
    }

    public void setOptimizeResultListener(IOptimizeResultListener optimizeResultListener) {
        mOptimizeResultListener = optimizeResultListener;
    }

    public void optimizeTrip(ArrayList<POIWithTravelTime> poiWithTravelTimeList) {
        optimizeWithPetrolStation(poiWithTravelTimeList);

    }

    private void optimizeWithPetrolStation(ArrayList<POIWithTravelTime> poiWithTravelTimeList) {
        ArrayList<FuzzySearchResult> optimizelSearchResult = new ArrayList<>();
        for (POIWithTravelTime poi : poiWithTravelTimeList) {
            optimizelSearchResult.add(poi.fuzzySearchResult);
        }
        submitOptimizeResult(optimizelSearchResult);
    }

    private void submitOptimizeResult(ArrayList<FuzzySearchResult> optimizelSearchResult) {
        mOptimizeResultListener.onOptimizeResult(optimizelSearchResult);
    }

}
