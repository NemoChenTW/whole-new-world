package com.taipei.ttbootcamp.interfaces;

import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;

public interface IFirstPlanResultListener extends IPlanResultListener {
    void onRoutePlanComplete(RouteResponse routeResult, ArrayList<FuzzySearchResult> originalSearchResult);
}
