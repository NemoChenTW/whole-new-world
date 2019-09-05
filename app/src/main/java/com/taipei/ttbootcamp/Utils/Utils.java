package com.taipei.ttbootcamp.Utils;

import com.google.common.collect.ImmutableList;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;
import java.util.Locale;

public class Utils {
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

    public static String secondToHourMinute(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;

        return String.format(Locale.ENGLISH,"%02d:%02d", hours, minutes);
    }
}
