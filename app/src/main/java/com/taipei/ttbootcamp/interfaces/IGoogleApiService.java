package com.taipei.ttbootcamp.interfaces;

import com.taipei.ttbootcamp.Entities.GoogleGeocode;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IGoogleApiService {
    String BASE_URL = "https://maps.googleapis.com";
    @GET("maps/api/geocode/json")
    Observable<GoogleGeocode> getGeocode(@Query("address") String address, @Query("key") String key);
}
