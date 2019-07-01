package com.taipei.ttbootcamp.Utils;

import com.google.common.collect.ImmutableList;
import com.taipei.ttbootcamp.interfaces.POIWithTravelTime;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;

public class Utlis {
    public static ArrayList<FuzzySearchResult> toFuzzySearchResultArraylist(ImmutableList<FuzzySearchResult> searchResults) {
        ArrayList<FuzzySearchResult> resultList = new ArrayList<>();
        for (FuzzySearchResult fuzzySearchResult : searchResults) {
            resultList.add(fuzzySearchResult);
        }
        return resultList;
    }

    public static ArrayList<LatLng> toLatLngArrayList(ArrayList<FuzzySearchResult> fuzzySearchResults) {
        ArrayList<LatLng> resultList = new ArrayList<LatLng>();
        for (FuzzySearchResult fresult : fuzzySearchResults) {
            resultList.add(fresult.getPosition());
        }
        return resultList;
    }

    public static ArrayList<FuzzySearchResult> getFuzzySearchResultsfromPOIWithTravelTime(ArrayList<POIWithTravelTime> poiWithTravelTimeList) {
        ArrayList<FuzzySearchResult> resultsList = new ArrayList<>();
        poiWithTravelTimeList.forEach(poi -> resultsList.add(poi.fuzzySearchResult));
        return resultsList;
    }

}
