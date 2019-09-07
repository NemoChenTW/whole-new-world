package com.taipei.ttbootcamp.data;

import android.util.Log;

import com.taipei.ttbootcamp.MyDriveAPI.PublicItinerary;
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

    //
    private ArrayList<Integer> fuzzySearchResultArrivalTimes;

    private boolean isWaypointsNeedUpdate = false;

    private ArrayList<PublicItinerary> myDriveItineraries;
    private Integer selectedItineraryIndex;

    private String tripTitle;
    private boolean useMyDriveData;

    public TripData() {
        tripTitle = "";
        selectedItineraryIndex = -1;
        useMyDriveData = true;
    }

    public TripData(final LatLng startPoint) {
        this.startPoint = startPoint;
    }

    public boolean isAvailableForPlan() {
        return (startPoint != null && endPoint != null);
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

    public void addWaypoints(int index, final LocationPoint locationPoint) {
        wayPoints.add(index, locationPoint);
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

    //
    public int getStayTime(){
        return wayPoints.get(0).getStaytimeToSeconds();
    }

    public ArrayList<Integer> getWaypointsOpeningTime(){
        ArrayList<Integer> openingTime = new ArrayList<>();
        wayPoints.forEach(locationPoint -> openingTime.add(locationPoint.getOpeningToSeconds()));
        return openingTime;
    }

    public ArrayList<Integer> getWaypointsClosedTime(){
        ArrayList<Integer> closedTime = new ArrayList<>();
        wayPoints.forEach(locationPoint -> closedTime.add(locationPoint.getClosedToSeconds()));
        return closedTime;
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

        useMyDriveData = false;

        this.fuzzySearchResults = (ArrayList<FuzzySearchResult>) fuzzySearchResults.clone();
        isWaypointsNeedUpdate = true;
    }

    public ArrayList<Integer> getFuzzySearchResultTravelTimes() {
        return fuzzySearchResultTravelTimes;
    }

    public void setFuzzySearchResultTravelTimes(ArrayList<Integer> fuzzySearchResultTravelTimes) {
        this.fuzzySearchResultTravelTimes = fuzzySearchResultTravelTimes;
    }

    //
    public ArrayList<Integer> getFuzzySearchResultArrivalTimes(){
        return fuzzySearchResultArrivalTimes;
    }

    public void setFuzzySearchResultArrivalTimes(ArrayList<Integer> fuzzySearchResultTravelTimes){
        this.fuzzySearchResultArrivalTimes = fuzzySearchResultTravelTimes;
    }


    public boolean isWaypointsNeedUpdate() {
        return isWaypointsNeedUpdate && ((myDriveItineraries != null && !myDriveItineraries.isEmpty()) || (fuzzySearchResults != null && !fuzzySearchResults.isEmpty()));
    }

    public void updateWaypointFromSearchResults() {
        isWaypointsNeedUpdate = false;
        wayPoints.clear();

        if (!useMyDriveData && this.fuzzySearchResults != null) {
            this.fuzzySearchResults.forEach(result -> wayPoints.add(new LocationPoint(result.getPosition(), result.getPoi().getName())));
            wayPoints.remove(wayPoints.size() - 1);
        }
        else if (useMyDriveData && this.myDriveItineraries != null && !this.myDriveItineraries.isEmpty() && selectedItineraryIndex >= 0 && selectedItineraryIndex < this.myDriveItineraries.size()) {
            PublicItinerary myDriveItinerary = myDriveItineraries.get(selectedItineraryIndex);
            setTripTitle(myDriveItinerary.getName());

            Log.e("MyDrive", "Itinerary ID: " + myDriveItinerary.getId() + ", Name: " + myDriveItinerary.getName());
            int i = 0;
            for (PublicItinerary.SegmentsBean segment : myDriveItinerary.getSegments()) {
                Log.e("MyDrive", "Segment Size: " + myDriveItinerary.getSegments().size());
                for (PublicItinerary.SegmentsBean.WaypointsBean waypoint : segment.getWaypoints()) {
                    Log.e("MyDrive", "waypoint Size: " + segment.getWaypoints().size());

                    //if (waypoint.getLocationInfo().getPoiName() != null
                    //        && !waypoint.getLocationInfo().getPoiName().isEmpty()) {
                    //    Log.e("MyDrive", "Add POI Name: " + waypoint.getLocationInfo().getPoiName());
                    if (waypoint.getLocationInfo() != null && waypoint.getLocationInfo().getPoint() != null) {
                        if (waypoint.getLocationInfo().getPoiName() != null && !waypoint.getLocationInfo().getPoiName().isEmpty()) {
                            Log.e("MyDrive", "Add WP: " + i + " Name: " + waypoint.getLocationInfo().getPoiName());
                            ++i;
                            addWaypoints(new LocationPoint(
                                    new LatLng(waypoint.getLocationInfo().getPoint().get(0)
                                            , waypoint.getLocationInfo().getPoint().get(1))
                                    , waypoint.getLocationInfo().getPoiName()));
                        }
                    }
                    //}
                }
            }
        }
        else {
            Log.e(TAG, "No Search Results at all. No waypoint is updated.");
        }
    }

    public String getTripTitle() {
        return tripTitle;
    }

    public void setTripTitle(String tripTitle) {
        this.tripTitle = tripTitle;
    }

    public ArrayList<PublicItinerary> getMyDriveItineraries() {
        return myDriveItineraries;
    }

    public void setMyDriveItineraries(ArrayList<PublicItinerary> myDriveItineraries) {
        if (this.myDriveItineraries != null && !this.myDriveItineraries.isEmpty()) {
            this.myDriveItineraries.clear();
        }
        this.myDriveItineraries = (ArrayList<PublicItinerary>) myDriveItineraries.clone();

        useMyDriveData = true;

        selectedItineraryIndex = -1;
        isWaypointsNeedUpdate = true;
    }

    public Integer getSelectedItineraryIndex() {
        return selectedItineraryIndex;
    }

    public void setSelectedItineraryIndex(Integer selectedItineraryIndex) {
        if (selectedItineraryIndex >= 0 && selectedItineraryIndex < myDriveItineraries.size()) {
            this.selectedItineraryIndex = selectedItineraryIndex;
            useMyDriveData = true;
        }
        else {
            Log.e(TAG, "Invalid mydrive select index is set.");
        }
    }

    public boolean isUseMyDriveData() {
        return useMyDriveData;
    }
}
