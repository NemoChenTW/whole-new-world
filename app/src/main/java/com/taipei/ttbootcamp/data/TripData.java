package com.taipei.ttbootcamp.data;

import com.taipei.ttbootcamp.Utils.Utlis;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;

public class TripData {
    private static final String TAG = "TripData";
    private LatLng startPoint;
    private LatLng endPoint;
    private ArrayList<LocationPoint> wayPoints = new ArrayList<>();

    private ArrayList<FuzzySearchResult> fuzzySearchResults;
    private ArrayList<Integer> fuzzySearchResultTravelTimes;

    private boolean isWaypointsNeedUpdate = false;

    public TripData() {}

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
        if (latLng != null) {
            wayPoints.add(new LocationPoint(latLng));
        }
    }

    public void addWaypoints(final LocationPoint locationPoint) {
        wayPoints.add(locationPoint);
    }

    public void removeWaypoints() {
        wayPoints.clear();
    }

    public LatLng getEndPoint() {
        return endPoint;
    }

    public ArrayList<LocationPoint> getWayPoints() {
        return (wayPoints == null)? null : (ArrayList<LocationPoint>) wayPoints.clone();
    }

    public LatLng[] getWaypointsLatLng() {
        ArrayList <LatLng> latLngs = new ArrayList<>();
        wayPoints.forEach(locationPoint -> latLngs.add(locationPoint.getPosition()));
        return latLngs.toArray(new LatLng[0]);
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
        return (fuzzySearchResults == null)? null : (ArrayList<FuzzySearchResult>) fuzzySearchResults.clone();
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
        return isWaypointsNeedUpdate && (fuzzySearchResults != null && !fuzzySearchResults.isEmpty());
    }

    public void updateWaypointFromSearchResults() {
        isWaypointsNeedUpdate = false;
        wayPoints.clear();
        ArrayList<LatLng> waypoints = Utlis.toLatLngArrayList(this.fuzzySearchResults);

        waypoints.remove(waypoints.size() - 1);
        waypoints.forEach(p -> addWaypoints(p));
    }
}
