package com.taipei.ttbootcamp.PoiGenerator;

import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.taipei.ttbootcamp.Utils.Utlis;
import com.taipei.ttbootcamp.interfaces.IPOISearchResult;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.common.location.LatLngAcc;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class POIGenerator {
    static private final String TAG = "POIGenerator";

    public enum POITYPE {
        MUSEUM,
        ART,
        FOOD,
        HOTEL
    }

    private static final int STANDARD_RADIUS = 30 * 1000; //30 km

    private static String convertPOITypeToText(POITYPE poitype) {
        String ret = "";
        switch (poitype)
        {
            case MUSEUM:
                ret = "museum";
                break;
            case ART:
                ret = "art";
                break;
            case FOOD:
                ret = "food";
                break;
            case HOTEL:
                ret = "hotel";
                break;
            default:
                break;
        }
        return ret;
    }

    public static void getPOIsWithType(SearchApi searchApi, LatLng currentPosition,
                                       POITYPE poitype, int radius, IPOISearchResult searchResultCallback) {
        String textToSearch = convertPOITypeToText(poitype);

        final Integer QUERY_LIMIT = 10;
        searchApi.search(new FuzzySearchQueryBuilder(textToSearch)
                .withPreciseness(new LatLngAcc(currentPosition, radius))
                .withTypeAhead(true)
                .withCategory(true)
                .withLimit(QUERY_LIMIT).build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<FuzzySearchResponse>() {

                    @Override
                    public void onSuccess(FuzzySearchResponse fuzzySearchResponse) {
                        for (FuzzySearchResult fuzzySearchResult : fuzzySearchResponse.getResults()) {
                            Log.d(TAG, "fuzzySearchResult: " + fuzzySearchResult.toString());
                        }
                        searchResultCallback.onPOISearchResult(Utlis.toFuzzySearchResultArraylist(fuzzySearchResponse.getResults()));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Search error: " + e.getMessage());
                    }
                });
    }


}
