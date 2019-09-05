package com.taipei.ttbootcamp.MyDriveAPI;

import android.util.Log;

import com.taipei.ttbootcamp.Entities.GoogleGeocode;
import com.taipei.ttbootcamp.MainActivity;
import com.tomtom.online.sdk.common.location.LatLng;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyDriveHelper {
    public static void getNearestPublicItineraries(LatLng aLatLng, String aTagName, int aMaxResult) {

        String maxResultString = (aMaxResult <= 0) ? "1" : Integer.toString(aMaxResult);
        String latLngString = aLatLng.getLatitudeAsString() + "," + aLatLng.getLongitudeAsString();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(MyDriveApiService.BASE_URL)
                .build();

        MyDriveApiService service = retrofit.create(MyDriveApiService.class);

        service.getPublicItineries(latLngString, aTagName, maxResultString).enqueue(new Callback<List<PublicItinerary>>() {
            @Override
            public void onResponse(Call<List<PublicItinerary>> call, Response<List<PublicItinerary>> response) {
                List<PublicItinerary> publicItineraries = response.body();
                Log.e("MyDrive", "Public Itinerary results.");
                if (!publicItineraries.isEmpty()) {
                    for (PublicItinerary publicItinerary : publicItineraries) {
                        Log.e("MyDrive", "ID: " + publicItinerary.getId() + ", Name: " + publicItinerary.getName());
                    }
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

    public static void getItineraryInfo(String aItineraryID) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(MyDriveApiService.BASE_URL)
                .build();

        MyDriveApiService service = retrofit.create(MyDriveApiService.class);

        service.getItinerary(aItineraryID).enqueue(new Callback<Itinerary>() {
            @Override
            public void onResponse(Call<Itinerary> call, Response<Itinerary> response) {
                Log.e("MyDrive", response.message());
                Itinerary itinerary = response.body();
                Log.e("MyDrive", "Itinerary detail results.");
                Log.e("MyDrive", "ID: " + itinerary.getId() + ", Name: " + itinerary.getName());
            }

            @Override
            public void onFailure(Call<Itinerary> call, Throwable t) {
                Log.e("MyDrive", "Fail Query Itinerary Detail.");
            }
        });
    }
}
