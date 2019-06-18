package com.taipei.ttbootcamp.interfaces;

import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

public class POIWithTravelTime {
    public FuzzySearchResult result;
    public Integer travelTime;
    public POIWithTravelTime(FuzzySearchResult fuzzyResult, Integer travelTime) {
        this.result = fuzzyResult;
        this.travelTime = travelTime;
    }
}
