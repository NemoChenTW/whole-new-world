package com.taipei.ttbootcamp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.Route;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.map.TomtomMapCallback;
import com.tomtom.online.sdk.routing.OnlineRoutingApi;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.Instruction;
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
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";

    // TomTom service
    private TomtomMap tomtomMap;

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
                    tomtomMap.addOnMapClickListener(onMapClickListener);
                    tomtomMap.addOnMapLongClickListener(onMapLongClickListener);
                    tomtomMap.addOnMapViewPortChangedListener(onMapViewPortChangedListener);
                    //tomtomMap.getMarkerSettings().setMarkerBalloonViewAdapter(createCustomViewAdapter());
                }
            };

    private TomtomMapCallback.OnMapClickListener onMapClickListener =
            latLng -> displayMessage(
                    R.string.menu_events_on_map_click,
                    latLng.getLatitude(),
                    latLng.getLongitude()
            );
    private TomtomMapCallback.OnMapLongClickListener onMapLongClickListener =
            latLng -> handleLongClick(latLng);

    private TomtomMapCallback.OnMapViewPortChanged onMapViewPortChangedListener =
            (focalLatitude, focalLongitude, zoomLevel, perspectiveRatio, yawDegrees) -> displayMessage(
                    R.string.menu_events_on_map_panning,
                    focalLatitude,
                    focalLongitude
            );
    private void displayMessage(@StringRes int titleId, double lat, double lon) {
        String title = this.getString(titleId);
        String message = String.format(java.util.Locale.getDefault(),
                "%s : %f : %f", title, lat, lon);
        Log.d(TAG, "displayMessage: " + message);
    }

    private void handleLongClick(@NonNull LatLng latLng) {
        currentLatLng = latLng;
        //currentPosition.setText(latLng.toString());
        /*
        searchApi.reverseGeocoding(new ReverseGeocoderSearchQueryBuilder(latLng.getLatitude(), latLng.getLongitude()).build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<ReverseGeocoderSearchResponse>() {
                    @Override
                    public void onSuccess(ReverseGeocoderSearchResponse response) {
                        processResponse(response);
                    }

                    @Override
                    public void onError(Throwable e) {
                        //handleApiError(e);
                    }

                    private void processResponse(ReverseGeocoderSearchResponse response) {
                        if (response.hasResults()) {
                            processFirstResult(response.getAddresses().get(0).getPosition());
                        }
                        else {
                            Toast.makeText(MainActivity.this, getString(R.string.geocode_no_results), Toast.LENGTH_SHORT).show();
                        }
                    }

                    private void processFirstResult(LatLng geocodedPosition) {
                        if (!isDeparturePositionSet()) {
                            setAndDisplayDeparturePosition(geocodedPosition);
                        } else {
                            destinationPosition = geocodedPosition;
                            tomtomMap.removeMarkers();
                            //drawRoute(departurePosition, destinationPosition);
                        }
                    }

                    private void setAndDisplayDeparturePosition(LatLng geocodedPosition) {
                        departurePosition = geocodedPosition;
                        createMarkerIfNotPresent(departurePosition, departureIcon);
                    }
                });
                */
    }

    private boolean isDestinationPositionSet() {
        return destinationPosition != null;
    }

    private boolean isDeparturePositionSet() {
        return departurePosition != null;
    }

    private void createMarkerIfNotPresent(LatLng position, Icon icon) {
        Optional optionalMarker = tomtomMap.findMarkerByPosition(position);
        if (!optionalMarker.isPresent()) {
            tomtomMap.addMarker(new MarkerBuilder(position)
                    .icon(icon));
        }
    }
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
                            displayRoutes(routeResult.getRoutes());
                            tomtomMap.displayRoutesOverview();
                        }

                        private void displayRoutes(List<FullRoute> routes) {
                            for (FullRoute fullRoute : routes) {
                                Log.d("pandia", fullRoute.getSummary().toString());
                                for (Instruction instruction : fullRoute.getGuidance().getInstructions())
                                {
                                    //System.out.println(instruction.getMessage() + " " + instruction.getManeuver() + " " + instruction.getCombinedMessage());
                                    Log.d("pandia", instruction.getMessage() + " " + instruction.getManeuver() + " " + instruction.getCombinedMessage());
                                }
                                route = tomtomMap.addRoute(new RouteBuilder(
                                        fullRoute.getCoordinates()).startIcon(departureIcon).endIcon(destinationIcon).isActive(true));
                            }
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

    private void updateMarkers() {
        tomtomMap.removeMarkers();

        if (departurePosition != null) {
            createMarkerIfNotPresent(departurePosition, departureIcon);
        }

        if (destinationPosition != null) {
            createMarkerIfNotPresent(destinationPosition, destinationIcon);
        }

        for (LatLng wp : allWaypoints) {
            createMarkerIfNotPresent(wp, waypointIcon);
        }
    }

    private void initUIViews() {
        departureIcon = Icon.Factory.fromResources(MainActivity.this, R.drawable.ic_map_route_departure);
        destinationIcon = Icon.Factory.fromResources(MainActivity.this, R.drawable.ic_map_route_destination);
        waypointIcon = Icon.Factory.fromResources(MainActivity.this, R.drawable.ic_map_traffic_danger_midgrey_small);

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
                if (currentLatLng != null) {
                    departurePosition = new LatLng(currentLatLng.toLocation());
                }
                planRoute(departurePosition, destinationPosition, allWaypoints.toArray(new LatLng[0]));
                updateMarkers();
            }
        });

        destinationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("pandia", "destination btn");
                if (currentLatLng != null) {
                    destinationPosition = new LatLng(currentLatLng.toLocation());
                }
                planRoute(departurePosition, destinationPosition, allWaypoints.toArray(new LatLng[0]));
                updateMarkers();
            }
        });

        clearWaypointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("pandia", "clear btn");
                planRoute(departurePosition, destinationPosition, null);
                allWaypoints.clear();
                updateMarkers();
            }
        });

        addWaypointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("pandia", "add btn");
                if (currentLatLng != null) {
                    allWaypoints.add(new LatLng(currentLatLng.toLocation()));
                }
                planRoute(departurePosition, destinationPosition, allWaypoints.toArray(new LatLng[0]));
                updateMarkers();
            }
        });
    }

    private void initTomTomServices() {
        searchApi = OnlineSearchApi.create(this);
        routingApi = OnlineRoutingApi.create(this);

    }

    private RoutingApi routingApi;
    private SearchApi searchApi;
    private LatLng departurePosition;
    private LatLng destinationPosition;
    private Icon departureIcon;
    private Icon destinationIcon;
    private Icon waypointIcon;
    private TextView currentPosition;
    private LatLng currentLatLng;
    private Button departureBtn;
    private Button destinationBtn;
    private Button addWaypointBtn;
    private Button clearWaypointBtn;
    private Route route;
    private ArrayList<LatLng> allWaypoints = new ArrayList<LatLng>();
}
