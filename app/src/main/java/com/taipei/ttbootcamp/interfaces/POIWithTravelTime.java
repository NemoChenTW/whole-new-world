package com.taipei.ttbootcamp.interfaces;

import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

public class POIWithTravelTime {
    public FuzzySearchResult fuzzySearchResult;
    public Integer travelTime;
    public POIWithTravelTime(FuzzySearchResult fuzzyResult, Integer travelTime) {
        this.fuzzySearchResult = fuzzyResult;
        this.travelTime = travelTime;
    }
}
