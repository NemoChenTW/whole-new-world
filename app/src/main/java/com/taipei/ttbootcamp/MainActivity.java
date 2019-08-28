package com.taipei.ttbootcamp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.taipei.ttbootcamp.Entities.GoogleGeocode;
import com.taipei.ttbootcamp.PoiGenerator.POIGenerator;
import com.taipei.ttbootcamp.Presenter.MainActivityPresenter;
import com.taipei.ttbootcamp.controller.TripController;
import com.taipei.ttbootcamp.data.TripData;
import com.taipei.ttbootcamp.implementations.MapElementDisplayer;
import com.taipei.ttbootcamp.implementations.TripOptimizer;
import com.taipei.ttbootcamp.interfaces.ITripOptimizer;
import com.taipei.ttbootcamp.interfaces.MainActivityView;
import com.taipei.ttbootcamp.ttsengine.TTSEngine;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.routing.OnlineRoutingApi;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.search.OnlineSearchApi;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MainActivity extends AppCompatActivity implements MainActivityView {
    static private final String TAG = "MainActivity";

    // TomTom service
    private TomtomMap mTomtomMap;
    private RoutingApi mRoutingApi;
    private SearchApi mSearchApi;

    private ITripOptimizer mTripOptimizer;
    private MainActivityPresenter mMainActivityPresenter;
    private MapElementDisplayer mMapElementDisplayer;
    private TripController mTripController;

    // View
    private PopupWindow mPopupWindow;
    private View mRootView;

    // UI items
    private ImageButton mBtnSearch;
    private EditText mEditTextPois;
    private TTSEngine mTTSEngine;
    private Button mDepartureBtn;
    private Button mDestinationBtn;
    private Button mAddWaypointBtn;
    private Button mClearWaypointBtn;
    private Button mTestButton;

    private TextView mCurrentPosition;

    private BootcampBroadcastReceiver mBootcampBroadcastReceiver = new BootcampBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRootView = findViewById(android.R.id.content);

        initUIViews();
        initTomTomServices();
        initialGooglePlace();

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getAsyncMap(onMapReadyCallback);

        mMainActivityPresenter = new MainActivityPresenter(this);

        View.OnClickListener searchButtonListener = getSearchButtonListener();
        mBtnSearch.setOnClickListener(searchButtonListener);

        initPopup();
        // Create TTS engine
        mTTSEngine = new TTSEngine(this);
    }

    private void initialGooglePlace() {
        // Initialize the SDK
        Places.initialize(getApplicationContext(), BuildConfig.ApiKey);

        // Create a new Places client instance
        PlacesClient placesClient = Places.createClient(this);
        requestPlaceDetails(placesClient);
    }

    public interface ApiService {
        String BASE_URL = "https://maps.googleapis.com";
        @GET("maps/api/geocode/json")
        Call<GoogleGeocode> getGeocode(@Query("address") String address, @Query("key") String key);
    }

    Retrofit retrofit = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(ApiService.BASE_URL)
            .build();
    ApiService service = retrofit.create(ApiService.class);

    private void requestPlaceDetails(final PlacesClient placesClient) {
        service.getGeocode("新光三越站前", BuildConfig.ApiKey).enqueue(new Callback<GoogleGeocode>() {
            @Override
            public void onResponse(Call<GoogleGeocode> call, Response<GoogleGeocode> response) {
                GoogleGeocode googleGeocode = response.body();
                if (!googleGeocode.getResults().isEmpty()) {
                    String placeId = googleGeocode.getResults().get(0).getPlace_id();
                    if (placeId.isEmpty()) {
                        Log.e(TAG, "PlaceId is empty");
                        return;
                    } else {
                        requestPlaceDetails(placesClient, placeId);
                    }
                }
            }

            @Override
            public void onFailure(Call<GoogleGeocode> call, Throwable t) {

            }
        });
    }

    private void requestPlaceDetails(final PlacesClient placesClient, final String placeId) {
        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.OPENING_HOURS, Place.Field.RATING);

        // Construct a request object, passing the place ID and fields array.
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.i(TAG, "Place found: name: " + place.getName()
                                + ", opening hours: " + place.getOpeningHours()
                                + ", rating: " + place.getRating());
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e(TAG, "Place not found: " + exception.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBootcampBroadcastReceiver, new IntentFilter(BootcampBroadcastReceiver.ACTION));
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mBootcampBroadcastReceiver);
        super.onPause();
    }

    private void initPopup() {
        mTestButton = findViewById(R.id.test_button);
        mTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });
    }
    private void showPopup() {
        if (mPopupWindow == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.popup_window,null);
            mPopupWindow = new PopupWindow(this, null, R.style.Transparent_Dialog);
            mPopupWindow.setContentView(view);

            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setAnimationStyle(R.style.anim_menu_bottombar);


            ((Button)view.findViewById(R.id.feeling_button_1)).setText("Museum");
            ((Button)view.findViewById(R.id.feeling_button_2)).setText("Coffee");
            ((Button)view.findViewById(R.id.feeling_button_3)).setText("Drinks");
            ((Button)view.findViewById(R.id.feeling_button_4)).setText("Don't want to go home");

            ArrayList<Button> buttonList = new ArrayList<>();
            buttonList.add(view.findViewById(R.id.feeling_button_1));
            buttonList.add(view.findViewById(R.id.feeling_button_2));
            buttonList.add(view.findViewById(R.id.feeling_button_3));
            buttonList.add(view.findViewById(R.id.feeling_button_4));

            for (Button button : buttonList) {
                button.setOnClickListener(view1 -> mPopupWindow.dismiss());
            }
        }
        mPopupWindow.showAtLocation(mRootView, Gravity.CENTER_VERTICAL | Gravity.LEFT,0, 40);
    }

    private View.OnClickListener getSearchButtonListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSearchClick(v);
            }

            private void handleSearchClick(View v) {
                String textToSearch = mEditTextPois.getText().toString();
                if (!textToSearch.isEmpty()) {
                    doFuzzySearch(textToSearch);
                }
            }

            private void doFuzzySearch(String textToSearch) {
                Log.d(TAG, "doFuzzySearch= " + textToSearch);
                final Integer QUERY_LIMIT = 10;
                mSearchApi.search(new FuzzySearchQueryBuilder(textToSearch)
                            .withLimit(QUERY_LIMIT).build())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<FuzzySearchResponse>() {

                            @Override
                            public void onSuccess(FuzzySearchResponse fuzzySearchResponse) {
                                for (FuzzySearchResult fuzzySearchResult : fuzzySearchResponse.getResults()) {
                                    Log.d(TAG, "fuzzySearchResult: " + fuzzySearchResult.toString());
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "Search error: " + e.getMessage());
                            }
                        });
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mTomtomMap.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private final OnMapReadyCallback onMapReadyCallback =
            new OnMapReadyCallback() {
                @Override
                public void onMapReady(TomtomMap map) {
                    //Map is ready here
                    mTomtomMap = map;
                    mTomtomMap.setMyLocationEnabled(true);
                    mTomtomMap.getUiSettings().getZoomingControlsView().show();
//                    mTomtomMap.collectLogsToFile(SampleApp.LOGCAT_PATH);
                    //mTomtomMap.getMarkerSettings().setMarkerBalloonViewAdapter(createCustomViewAdapter());

                    mMapElementDisplayer = new MapElementDisplayer(getApplicationContext(), mTomtomMap);
                    mMainActivityPresenter.initMainActivityPresenter(mMapElementDisplayer, mSearchApi);
                    mTripOptimizer = new TripOptimizer(mSearchApi, mRoutingApi);
                    mTripController = new TripController(mRoutingApi, mSearchApi, mMapElementDisplayer, mTripOptimizer);

                    mTripOptimizer.setOptimizeResultListener(mTripController);

                    // Plan test trip
                    TripData tripData = new TripData(new LatLng(25.046570, 121.515313));
                    mTripController.PlanTrip(tripData, POIGenerator.POITYPE.MUSEUM, 100000);
                }
            };

/*
    private SingleLayoutBalloonViewAdapter createCustomViewAdapter() {
        return new SingleLayoutBalloonViewAdapter(R.layout.marker_custom_balloon) {

            @Override
            public void onBindView(View view, final Marker marker, BaseMarkerBalloon baseMarkerBalloon) {
                Button btnAddWayPoint = view.findViewById(R.id.btn_balloon_waypoint);
                TextView textViewPoiName = view.findViewById(R.id.textview_balloon_poiname);
                TextView textViewPoiAddress = view.findViewById(R.id.textview_balloon_poiaddress);
                textViewPoiName.setText(baseMarkerBalloon.getStringProperty(getApplicationContext().getString(R.string.poi_name_key)));
                textViewPoiAddress.setText(baseMarkerBalloon.getStringProperty(getApplicationContext().getString(R.string.address_key)));
                btnAddWayPoint.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setWayPoint(marker);
                    }

                    private void setWayPoint(Marker marker) {
                        wayPointPosition = marker.getPosition();
                        mTomtomMap.clearRoute();
                        drawRouteWithWayPoints(departurePosition, destinationPosition, new LatLng[] {wayPointPosition});
                        marker.deselect();
                    }
                });
            }
        };
    }
*/

    public void displayMarkerFeatureMenu(boolean display) {
        if (display) {
            findViewById(R.id.marker_feature_menu).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.marker_feature_menu).setVisibility(View.GONE);
        }
    }

    private void initUIViews() {
        mBtnSearch = findViewById(R.id.btn_main_poisearch);
        mEditTextPois = findViewById(R.id.edittext_main_poisearch);
        mCurrentPosition = findViewById(R.id.textview_balloon_poiname);
        mDepartureBtn = findViewById(R.id.btn_balloon_departure);
        mDestinationBtn = findViewById(R.id.btn_balloon_destination);
        mAddWaypointBtn = findViewById(R.id.btn_balloon_waypoint);
        mClearWaypointBtn = findViewById(R.id.btn_balloon_clearwaypoint);

        mDepartureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainActivityPresenter.onDepartureButtonClick();
            }
        });

        mDestinationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainActivityPresenter.onDestinationButtonClick();
            }
        });

        mAddWaypointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainActivityPresenter.onAddWaypointButtonClick();
            }
        });

        mClearWaypointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainActivityPresenter.onClearWaypointButtonClick();
            }
        });

    }

    private void initTomTomServices() {
        mSearchApi = OnlineSearchApi.create(this);
        mRoutingApi = OnlineRoutingApi.create(this);
    }

    @Override
    public void showMarkerFeatureMenu() {
        displayMarkerFeatureMenu(true);
    }

    @Override
    public void hideMarkerFeatureMenu() {
        displayMarkerFeatureMenu(false);
    }

    public class BootcampBroadcastReceiver extends BroadcastReceiver {
        static private final String TAG = "BootcampBroadcast";

        static private final String ACTION = "TTS_SPEAK";
        static private final String EXTRA_SPEAK = "EXTRA_SPEAK";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Receive intent: " + intent);
            final String action = intent.getAction();
            if (ACTION.equals(action)) {
                final String extraSpeak = intent.getStringExtra(EXTRA_SPEAK);
                Log.d(TAG, "extra= " + extraSpeak);

                if (mTTSEngine != null) {
                    mTTSEngine.speak(getSpeakString(extraSpeak), Locale.ENGLISH);
                } else {
                    Log.d(TAG, "mTTSEngine is null ");
                }
                if ("KIND".equals(extraSpeak.toUpperCase())) {
                    showPopup();
                }
            }
        }

        private String getSpeakString(final String extraSpeak) {
            switch (extraSpeak.toUpperCase()) {
                case "HELLO":
                    return "Morning Nick, " + getResources().getString(R.string.greeting_string);
                case "KIND":
                    return "Then, " + getResources().getString(R.string.ask_category_string);
                case "RECOMMEND":
                    return getResources().getString(R.string.provide_recommend_string);
                case "TRASH":
                    return getResources().getString(R.string.trash_trip_string);
                case "DOG":
                    return getResources().getString(R.string.dogs_out_string);
                default:
                    return "";
            }
        }
    }
}
