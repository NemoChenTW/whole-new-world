package com.taipei.ttbootcamp.MyDriveAPI;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MyDriveApiService {
    String BASE_URL = "https://api.mydrive.tomtom.com";


    //https://api.mydrive.tomtom.com/gor-webapp/ws/rest/V1/itineraries/published?sortBy=NEAREST&tags=tomtomcommunityroutes&nearestTo=45.413441%2C5.873900&maxResults=1&view=FULL&locale=en_gb
    @GET("/gor-webapp/ws/rest/V1/itineraries/published?sortBy=NEAREST&view=FULL&locale=en_gb")
    Call<List<PublicItinerary>> getPublicItineries(@Query("nearestTo") String aLatLng, @Query("tags") String aTagName, @Query("maxResults") String aMaxResult);

    //https://api.mydrive.tomtom.com/gor-webapp/ws/rest/V1/itineraries/7771c937-fb7d-4043-ae4e-c6737e27b82c?view=FULL&locale=en_gb&autoTranslate=false
    @GET("/gor-webapp/ws/rest/V1/itineraries/{ItineraryID}?view=FULL&locale=en_gb&autoTranslate=false")
    Call<Itinerary> getItinerary(@Path("ItineraryID") String aItineraryID);
}
