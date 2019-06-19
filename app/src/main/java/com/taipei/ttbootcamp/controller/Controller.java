package com.taipei.ttbootcamp.controller;

import com.taipei.ttbootcamp.RoutePlanner.RoutePlanner;
import com.taipei.ttbootcamp.dailydecorator.DailyNeedDecorator;
import com.taipei.ttbootcamp.interfaces.IMapElementDisplay;
import com.taipei.ttbootcamp.interfaces.IPOISearchResult;
import com.taipei.ttbootcamp.interfaces.IPOIWithTravelTimeResult;
import com.taipei.ttbootcamp.interfaces.POIWithTravelTime;
import com.taipei.ttbootcamp.poigenerator.POIGenerator;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.Route;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;

public class Controller implements IPOISearchResult {

    private RoutingApi routingApi;
    private SearchApi searchApi;
    private IMapElementDisplay mapElementDisplay;
    private LatLng currentPosition;
    private RoutePlanner routePlanner;
    private DailyNeedDecorator dailyNeedDecorator;

    public Controller(RoutingApi routingApi, SearchApi searchApi, IMapElementDisplay mapElementDisplay)
    {
        this.routingApi = routingApi;
        this.searchApi = searchApi;
        this.mapElementDisplay = mapElementDisplay;
        routePlanner = new RoutePlanner(routingApi, mapElementDisplay, new IPOIWithTravelTimeResult() {
            @Override
            public void onPOIWithTravelTimeResult(ArrayList<POIWithTravelTime> result) {
                dailyNeedDecorator.generateDailyNeed(result);
            }
        });
        dailyNeedDecorator = new DailyNeedDecorator(routingApi, searchApi, new IPOIWithTravelTimeResult() {
            @Override
            public void onPOIWithTravelTimeResult(ArrayList<POIWithTravelTime> result) {

            }
        });
    }

    @Override
    public void onPOISearchResult(ArrayList<FuzzySearchResult> searchResult) {
        LatLng destination = new LatLng(searchResult.get(searchResult.size() - 1).getPosition().toLocation());
        routePlanner.planRoute(currentPosition, destination, searchResult, 0);
    }

    public void PlanTrip(LatLng currentPosition, POIGenerator.POITYPE poitype, int radius)
    {
        this.currentPosition = currentPosition;
        POIGenerator.getPOIsWithType(searchApi, this.currentPosition, poitype, radius, this);
    }

}
