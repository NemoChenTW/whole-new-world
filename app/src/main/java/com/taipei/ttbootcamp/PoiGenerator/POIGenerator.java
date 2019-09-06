package com.taipei.ttbootcamp.PoiGenerator;

import android.util.Log;

import com.taipei.ttbootcamp.MyDriveAPI.MyDriveHelper;
import com.taipei.ttbootcamp.MyDriveAPI.PublicItinerary;
import com.taipei.ttbootcamp.Utils.Utlis;
import com.taipei.ttbootcamp.data.LocationPoint;
import com.taipei.ttbootcamp.data.TripData;
import com.taipei.ttbootcamp.interfaces.IPOISearchResult;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.common.location.LatLngAcc;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    public static void queryWithCategory(SearchApi searchApi, TripData tripData,
                                         POITYPE poitype, int radius, IPOISearchResult searchResultCallback) {
        String textToSearch = convertPOITypeToText(poitype);

        final Integer QUERY_LIMIT = 10;
        searchApi.search(new FuzzySearchQueryBuilder(textToSearch)
                .withPreciseness(new LatLngAcc(tripData.getStartPoint(), radius))
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
                        tripData.setFuzzySearchResults(Utlis.toFuzzySearchResultArraylist(fuzzySearchResponse.getResults()));
                        searchResultCallback.onPOISearchResult(tripData);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Search error: " + e.getMessage());
                    }
                });
    }

    public static void queryWithMyDriveAPI(LatLng aLatLng, String aTagName, int aMaxResult, TripData tripData, IPOISearchResult searchResultCallback) {
        MyDriveHelper.getPublicItinerariesQuery(aLatLng, aTagName, aMaxResult).enqueue(new Callback<List<PublicItinerary>>() {
            @Override
            public void onResponse(Call<List<PublicItinerary>> call, Response<List<PublicItinerary>> response) {
                List<PublicItinerary> publicItineraries = response.body();
                Log.e("MyDrive", "Public Itinerary results.");
                if (!publicItineraries.isEmpty()) {
                    PublicItinerary itinerary = publicItineraries.get(0);

                    if (itinerary == null) {
                        Log.e(TAG, "Cannot convert MyDrive Itinerary.");
                        return;
                    }

                    tripData.removeWaypoints();
                    tripData.setTripTitle(itinerary.getName());

                    Log.e("MyDrive", "Itinerary ID: " + itinerary.getId() + ", Name: " + itinerary.getName());
                    int i = 0;
                    for (PublicItinerary.SegmentsBean segment : itinerary.getSegments()) {
                        Log.e("MyDrive", "Segment Size: " + itinerary.getSegments().size());
                        for (PublicItinerary.SegmentsBean.WaypointsBean waypoint : segment.getWaypoints()) {
                            Log.e("MyDrive", "waypoint Size: " + segment.getWaypoints().size());

                            //if (waypoint.getLocationInfo().getPoiName() != null
                            //        && !waypoint.getLocationInfo().getPoiName().isEmpty()) {
                            //    Log.e("MyDrive", "Add POI Name: " + waypoint.getLocationInfo().getPoiName());
                            if (waypoint.getLocationInfo() != null && waypoint.getLocationInfo().getPoint() != null) {
                                if (waypoint.getLocationInfo().getPoiName() != null && !waypoint.getLocationInfo().getPoiName().isEmpty()) {
                                    Log.e("MyDrive", "Add WP: " + i + " Name: " + waypoint.getLocationInfo().getPoiName());
                                    ++i;
                                    tripData.addWaypoints(new LocationPoint(
                                            new LatLng(waypoint.getLocationInfo().getPoint().get(0)
                                                    , waypoint.getLocationInfo().getPoint().get(1))
                                                , waypoint.getLocationInfo().getPoiName()));
                                }
                            }
                            //}
                        }
                    }
                    searchResultCallback.onPOISearchResult(tripData);
                }
                else {
                    Log.e("MyDrive", "Empty public Itinerary.");
                }
            }

            @Override
            public void onFailure(Call<List<PublicItinerary>> call, Throwable t) {
                Log.e("MyDrive", "Fail Query public Itinerary.");
            }
        });
    }
}
