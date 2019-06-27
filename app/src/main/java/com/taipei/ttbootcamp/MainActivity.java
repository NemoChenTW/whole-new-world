package com.taipei.ttbootcamp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.taipei.ttbootcamp.RoutePlanner.RoutePlanner;
import com.taipei.ttbootcamp.controller.TripController;
import com.taipei.ttbootcamp.implementations.MapElementDisplayer;
import com.taipei.ttbootcamp.poigenerator.POIGenerator;
import com.taipei.ttbootcamp.ttsengine.TTSEngine;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.map.TomtomMapCallback;
import com.tomtom.online.sdk.routing.OnlineRoutingApi;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.InstructionsType;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.routing.data.RouteType;
import com.tomtom.online.sdk.search.OnlineSearchApi;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    static private final String TAG = "MainActivity";

    // TomTom service
    private TomtomMap tomtomMap;
    private RoutingApi routingApi;
    private SearchApi searchApi;

    private MapElementDisplayer mMapElementDisplayer;
    private RoutePlanner routePlanner;
    private TripController tripController;

    // View
    private PopupWindow popupWindow;
    private View rootView;

    // UI items
    private ImageButton btnSearch;
    private EditText editTextPois;
    private TTSEngine mTTSEngine;
    private Button departureBtn;
    private Button destinationBtn;
    private Button addWaypointBtn;
    private Button clearWaypointBtn;

    private TextView currentPosition;

    private BootcampBroadcastReceiver bootcampBroadcastReceiver = new BootcampBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootView = findViewById(android.R.id.content);

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getAsyncMap(onMapReadyCallback);

        initUIViews();
        initTomTomServices();

        View.OnClickListener searchButtonListener = getSearchButtonListener();
        btnSearch.setOnClickListener(searchButtonListener);

        initPopup();
        // Create TTS engine
        mTTSEngine = new TTSEngine(this);
    }

    private TomtomMapCallback.OnMapLongClickListener onMapLongClickListener =
            latLng -> handleLongClick(latLng);

    private void handleLongClick(LatLng latLng) {
        displayMarkerFeatureMenu(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(bootcampBroadcastReceiver, new IntentFilter(BootcampBroadcastReceiver.ACTION));
    }

    @Override
    protected void onPause() {
        unregisterReceiver(bootcampBroadcastReceiver);
        super.onPause();
    }

    private Button testButton;
    private void initPopup() {
        testButton = findViewById(R.id.test_button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });
    }
    private void showPopup() {
        if (popupWindow == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.popup_window,null);
            popupWindow = new PopupWindow(this, null, R.style.Transparent_Dialog);
            popupWindow.setContentView(view);

            popupWindow.setOutsideTouchable(true);
            popupWindow.setAnimationStyle(R.style.anim_menu_bottombar);


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
                button.setOnClickListener(view1 -> popupWindow.dismiss());
            }
        }
        popupWindow.showAtLocation(rootView, Gravity.CENTER_VERTICAL | Gravity.LEFT,0, 40);
    }

    private View.OnClickListener getSearchButtonListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSearchClick(v);
            }

            private void handleSearchClick(View v) {
                String textToSearch = editTextPois.getText().toString();
                if (!textToSearch.isEmpty()) {
                    doFuzzySearch(textToSearch);
                }
            }

            private void doFuzzySearch(String textToSearch) {
                Log.d(TAG, "doFuzzySearch= " + textToSearch);
                final Integer QUERY_LIMIT = 10;
                searchApi.search(new FuzzySearchQueryBuilder(textToSearch)
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
        tomtomMap.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private final OnMapReadyCallback onMapReadyCallback =
            new OnMapReadyCallback() {
                @Override
                public void onMapReady(TomtomMap map) {
                    //Map is ready here
                    tomtomMap = map;
                    tomtomMap.setMyLocationEnabled(true);
//                    tomtomMap.collectLogsToFile(SampleApp.LOGCAT_PATH);
                    //tomtomMap.getMarkerSettings().setMarkerBalloonViewAdapter(createCustomViewAdapter());

                    tomtomMap.addOnMapLongClickListener(onMapLongClickListener);
                    mMapElementDisplayer = new MapElementDisplayer(getApplicationContext(), tomtomMap);
                    tripController = new TripController(routingApi, searchApi, mMapElementDisplayer);
                    tripController.PlanTrip(new LatLng(25.046570, 121.515313), POIGenerator.POITYPE.MUSEUM, 100000);
                    routePlanner = new RoutePlanner(routingApi, tripController);
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
                        tomtomMap.clearRoute();
                        drawRouteWithWayPoints(departurePosition, destinationPosition, new LatLng[] {wayPointPosition});
                        marker.deselect();
                    }
                });
            }
        };
    }
*/

    private void planRoute(LatLng start, LatLng end, LatLng[] waypoints) {
        if (start != null && end != null) {
            tomtomMap.clearRoute();
            RouteQuery routeQuery = createRouteQuery(start, end, waypoints);
            routingApi.planRoute(routeQuery)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<RouteResponse>() {
                        @Override
                        public void onSuccess(RouteResponse routeResult) {
                            mMapElementDisplayer.displayRoutes(routeResult.getRoutes());
                            tomtomMap.displayRoutesOverview();
                        }

                        @Override
                        public void onError(Throwable e) {
                            //handleApiError(e);
                            //clearMap();
                        }
                    });
        }
    }

    private RouteQuery createRouteQuery(LatLng start, LatLng stop, LatLng[] wayPoints) {
        return (wayPoints != null && wayPoints.length != 0) ?
                new RouteQueryBuilder(start, stop).withWayPoints(wayPoints).withRouteType(RouteType.FASTEST)
                        .withInstructionsType(InstructionsType.TAGGED).build():
                new RouteQueryBuilder(start, stop).withRouteType(RouteType.FASTEST)
                        .withInstructionsType(InstructionsType.TAGGED).build();
    }

    public void displayMarkerFeatureMenu(boolean display) {
        if (display) {
            findViewById(R.id.marker_feature_menu).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.marker_feature_menu).setVisibility(View.GONE);
        }
    }

    private void initUIViews() {
        btnSearch = findViewById(R.id.btn_main_poisearch);
        editTextPois = findViewById(R.id.edittext_main_poisearch);
        currentPosition = findViewById(R.id.textview_balloon_poiname);
        departureBtn = findViewById(R.id.btn_balloon_departure);
        destinationBtn = findViewById(R.id.btn_balloon_destination);
        addWaypointBtn = findViewById(R.id.btn_balloon_waypoint);
        clearWaypointBtn = findViewById(R.id.btn_balloon_clearwaypoint);

        departureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("pandia", "departure btn");
                displayMarkerFeatureMenu(false);
                if (mMapElementDisplayer.getCurrentLatLng() != null) {
                    mMapElementDisplayer.setDeparturePosition(new LatLng(mMapElementDisplayer.getCurrentLatLng().toLocation()));
                }
                routePlanner.planRoute(mMapElementDisplayer.getDeparturePosition(),
                                        mMapElementDisplayer.getDestinationPosition(),
                                        mMapElementDisplayer.getAllWaypoints().toArray(new LatLng[0]));
                mMapElementDisplayer.updateMarkers();
            }
        });

        destinationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("pandia", "destination btn");
                displayMarkerFeatureMenu(false);
                if (mMapElementDisplayer.getCurrentLatLng() != null) {
                    mMapElementDisplayer.setDestinationPosition(new LatLng(mMapElementDisplayer.getCurrentLatLng().toLocation()));
                }
                routePlanner.planRoute(mMapElementDisplayer.getDeparturePosition(),
                                        mMapElementDisplayer.getDestinationPosition(),
                                        mMapElementDisplayer.getAllWaypoints().toArray(new LatLng[0]));
                mMapElementDisplayer.updateMarkers();
            }
        });

        clearWaypointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("pandia", "clear btn");
                displayMarkerFeatureMenu(false);
                routePlanner.planRoute(mMapElementDisplayer.getDeparturePosition(),
                                        mMapElementDisplayer.getDestinationPosition(), null);
                mMapElementDisplayer.clearWaypoints();
                mMapElementDisplayer.updateMarkers();
            }
        });

        addWaypointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("pandia", "add btn");
                displayMarkerFeatureMenu(false);
                if (mMapElementDisplayer.getCurrentLatLng() != null) {
                    mMapElementDisplayer.addWaypoint(new LatLng(mMapElementDisplayer.getCurrentLatLng().toLocation()));
                }
                routePlanner.planRoute(mMapElementDisplayer.getDeparturePosition(),
                                        mMapElementDisplayer.getDestinationPosition(),
                                        mMapElementDisplayer.getAllWaypoints().toArray(new LatLng[0]));
                mMapElementDisplayer.updateMarkers();
            }
        });
    }

    private void initTomTomServices() {
        searchApi = OnlineSearchApi.create(this);
        routingApi = OnlineRoutingApi.create(this);

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
