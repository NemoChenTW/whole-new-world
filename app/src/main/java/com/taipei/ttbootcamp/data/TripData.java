package com.taipei.ttbootcamp.data;

import com.taipei.ttbootcamp.Utils.Utlis;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;

public class TripData {
    private static final String TAG = "TripData";
    private LatLng startPoint;
    private LatLng endPoint;
    private ArrayList<LatLng> wayPoints = new ArrayList<>();

    private ArrayList<FuzzySearchResult> fuzzySearchResults;
    private ArrayList<Integer> fuzzySearchResultTravelTimes;

    private boolean isWaypointsNeedUpdate = false;

    public TripData(final LatLng startPoint) {
        this.startPoint = startPoint;
    }

    public boolean isAvailableForPlan() {
        return (startPoint != null &&endPoint != null);
    }

    public boolean hasWaypoints() {
        return (!wayPoints.isEmpty());
    }

    public void addWaypoints(final LatLng latLng) {
        wayPoints.add(latLng);
    }

    public LatLng getEndPoint() {
        return endPoint;
    }

    public LatLng[] getWaypoints() {
        return wayPoints.toArray(new LatLng[0]);
    }

    public void setEndPoint(LatLng endPoint) {
        this.endPoint = endPoint;
    }

    public LatLng getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(LatLng startPoint) {
        this.startPoint = startPoint;
    }

    public ArrayList<FuzzySearchResult> getFuzzySearchResults() {
        return (ArrayList<FuzzySearchResult>) fuzzySearchResults.clone();
    }

    public void setFuzzySearchResults(ArrayList<FuzzySearchResult> fuzzySearchResults) {
        if (this.fuzzySearchResults != null && !this.fuzzySearchResults.isEmpty()) {
            this.fuzzySearchResults.clear();
        }
        this.fuzzySearchResults = (ArrayList<FuzzySearchResult>) fuzzySearchResults.clone();
        isWaypointsNeedUpdate = true;
    }

    public ArrayList<Integer> getFuzzySearchResultTravelTimes() {
        return fuzzySearchResultTravelTimes;
    }

    public void setFuzzySearchResultTravelTimes(ArrayList<Integer> fuzzySearchResultTravelTimes) {
        this.fuzzySearchResultTravelTimes = fuzzySearchResultTravelTimes;
    }

    public boolean isWaypointsNeedUpdate() {
        return isWaypointsNeedUpdate;
    }

    public void updateWaypointFromSearchResults() {
        isWaypointsNeedUpdate = false;
        wayPoints.clear();
        ArrayList<LatLng> waypoints = Utlis.toLatLngArrayList(this.fuzzySearchResults);

        waypoints.remove(waypoints.size() - 1);
        waypoints.forEach(p -> addWaypoints(p));
    }
}
