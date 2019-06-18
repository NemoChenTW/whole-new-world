package com.taipei.ttbootcamp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.taipei.ttbootcamp.RoutePlanner.RoutePlanner;
import com.taipei.ttbootcamp.implement.MapElementDisplayer;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.Route;
import com.tomtom.online.sdk.map.TomtomMap;
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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";

    // TomTom service
    private TomtomMap tomtomMap;

    private MapElementDisplayer mMapElementDisplayer;

    // UI items
    private ImageButton btnSearch;
    private EditText editTextPois;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getAsyncMap(onMapReadyCallback);

        initUIViews();
        initTomTomServices();

        View.OnClickListener searchButtonListener = getSearchButtonListener();
        btnSearch.setOnClickListener(searchButtonListener);

        routePlanner = new RoutePlanner();
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

                    mMapElementDisplayer = new MapElementDisplayer(getApplicationContext(), tomtomMap);
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
                if (mMapElementDisplayer.getCurrentLatLng() != null) {
                    mMapElementDisplayer.setDeparturePosition(new LatLng(mMapElementDisplayer.getCurrentLatLng().toLocation()));
                }
                routePlanner.planRoute(mMapElementDisplayer.getDeparturePosition(),
                                        mMapElementDisplayer.getDestinationPosition(),
                                        mMapElementDisplayer.getAllWaypoints().toArray(new LatLng[0]),
                                        tomtomMap, routingApi);
                mMapElementDisplayer.updateMarkers();
            }
        });

        destinationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("pandia", "destination btn");
                if (mMapElementDisplayer.getCurrentLatLng() != null) {
                    mMapElementDisplayer.setDestinationPosition(new LatLng(mMapElementDisplayer.getCurrentLatLng().toLocation()));
                }
                routePlanner.planRoute(mMapElementDisplayer.getDeparturePosition(),
                                        mMapElementDisplayer.getDestinationPosition(),
                                        mMapElementDisplayer.getAllWaypoints().toArray(new LatLng[0]),
                                        tomtomMap, routingApi);
                mMapElementDisplayer.updateMarkers();
            }
        });

        clearWaypointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("pandia", "clear btn");
                routePlanner.planRoute(mMapElementDisplayer.getDeparturePosition(),
                                        mMapElementDisplayer.getDestinationPosition(),
                            null,
                                        tomtomMap, routingApi);
                mMapElementDisplayer.clearWaypoints();
                mMapElementDisplayer.updateMarkers();
            }
        });

        addWaypointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("pandia", "add btn");
                if (mMapElementDisplayer.getCurrentLatLng() != null) {
                    mMapElementDisplayer.addWaypoint(new LatLng(mMapElementDisplayer.getCurrentLatLng().toLocation()));
                }
                routePlanner.planRoute(mMapElementDisplayer.getDeparturePosition(),
                                        mMapElementDisplayer.getDestinationPosition(),
                                        mMapElementDisplayer.getAllWaypoints().toArray(new LatLng[0]),
                                        tomtomMap, routingApi);
                mMapElementDisplayer.updateMarkers();
            }
        });
    }

    private void initTomTomServices() {
        searchApi = OnlineSearchApi.create(this);
        routingApi = OnlineRoutingApi.create(this);

    }

    private RoutingApi routingApi;
    private SearchApi searchApi;
    private TextView currentPosition;
    private Button departureBtn;
    private Button destinationBtn;
    private Button addWaypointBtn;
    private Button clearWaypointBtn;
    private Route route;
    private RoutePlanner routePlanner;
}
