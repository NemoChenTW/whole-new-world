package com.taipei.ttbootcamp.PoiGenerator;

import android.util.Log;

import com.taipei.ttbootcamp.RoutePlanner.RoutePlanner;
import com.taipei.ttbootcamp.interfaces.IPOISearchResult;
import com.taipei.ttbootcamp.interfaces.IPOIWithTravelTimeResult;
import com.taipei.ttbootcamp.interfaces.POIWithTravelTime;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;
import java.util.Calendar;

public class DailyNeedDecorator {

    private static final int DEFAULT_VISIT_TIME_IN_SECOND = 3600; // one hour
    private static final int DEFAULT_RESTAURANT_RADIUS = 30000;
    private static final int DEFAULT_HOTEL_RADIUS = 30000;
    private static final int DEFAULT_EXPECTED_TRAVEL_TIME = 1800;  // 30 mins
    private static final int LUNCH_FLAG = -1;
    private static final int DINNER_FLAG = -2;

    private boolean isAddBreakfast = false;
    private boolean isAddLunch = false;
    private boolean isAddDinner = false;
    private int accumSecondNow = 0;
    private IPOIWithTravelTimeResult resultCallback;
    private SearchApi searchApi;
    private RoutingApi routingApi;
    private ArrayList<POIWithTravelTime> proposedTrip;

//    public DailyNeedDecorator(RoutingApi routingApi, SearchApi searchApi, IPOIWithTravelTimeResult resultCallback) {
//        this.routingApi = routingApi;
//        this.searchApi = searchApi;
//        this.resultCallback = resultCallback;
//        proposedTrip = new ArrayList<POIWithTravelTime>();
//    }
//
//
//    public void generateDailyNeed(ArrayList<POIWithTravelTime> tripDraft) {
//        proposedTrip.clear();
//        findRestaurantIndex(tripDraft);
//        findLunchRestaurant();
//        //addHotel(searchApi, proposedTrip);
//        //return proposedTrip;
//    }
//
//    private void findLunchRestaurant() {
//        if (isAddLunch) {
//            int index = findRestaurantFlag(proposedTrip, LUNCH_FLAG);
//
//            if (index >= 0) {
//                POIGenerator.queryWithCategory(searchApi, proposedTrip.get(index - 1).fuzzySearchResult.getPosition(),
//                        POIGenerator.POITYPE.FOOD, DEFAULT_RESTAURANT_RADIUS, new IPOISearchResult() {
//                            @Override
//                            public void onPOISearchResult(ArrayList<FuzzySearchResult> searchResult) {
//                                if (!searchResult.isEmpty()) {
//                                    int index = findRestaurantFlag(proposedTrip, LUNCH_FLAG);
//                                    proposedTrip.get(findRestaurantFlag(proposedTrip, LUNCH_FLAG)).fuzzySearchResult = searchResult.get(0);
//                                    //proposedTrip.remove(index);
//                                    //proposedTrip.add(index, new POIWithTravelTime(searchResult.get(0), LUNCH_FLAG));
//                                    findLunchTravelTime(searchResult.get(0));
//                                }
//                                else {
//                                    Log.d("DailySupply", "Not found lunch");
//                                    findDinnerRestaurant();
//                                }
//                            }
//                        });
//            }
//        }
//        else {
//            findDinnerRestaurant();
//        }
//    }
//
//    private int findRestaurantFlag(ArrayList<POIWithTravelTime> tripDraft, int flag) {
//        int index = -1;
//        for (int i = 0; i < tripDraft.size(); ++i) {
//            if (tripDraft.get(i).travelTime == flag) {
//                index = i;
//            }
//        }
//        return index;
//    }
//
//    private void findLunchTravelTime(FuzzySearchResult restaurant) {
//        int prev = findRestaurantFlag(proposedTrip, LUNCH_FLAG) - 1;
//        RoutePlanner routePlanner = new RoutePlanner(routingApi, null);
////        routePlanner.planRoute(proposedTrip.get(prev).fuzzySearchResult.getPosition(), restaurant.getPosition(), null);
//    }
//
//    private void findDinnerRestaurant() {
//        if (isAddDinner) {
//            int index = findRestaurantFlag(proposedTrip, DINNER_FLAG);
//
//            if (index >= 0) {
//                POIGenerator.queryWithCategory(searchApi, proposedTrip.get(index - 1).fuzzySearchResult.getPosition(),
//                        POIGenerator.POITYPE.FOOD, DEFAULT_RESTAURANT_RADIUS, new IPOISearchResult() {
//                            @Override
//                            public void onPOISearchResult(ArrayList<FuzzySearchResult> searchResult) {
//                                if (!searchResult.isEmpty()) {
//                                    int index = findRestaurantFlag(proposedTrip, DINNER_FLAG);
//                                    proposedTrip.get(findRestaurantFlag(proposedTrip, DINNER_FLAG)).fuzzySearchResult = searchResult.get(0);
//                                    //proposedTrip.remove(index);
//                                    //proposedTrip.add(index, new POIWithTravelTime(searchResult.get(0), DINNER_FLAG));
//                                    findDinnerTravelTime(searchResult.get(0));
//                                }
//                                else {
//                                    Log.d("DailySupply", "Not found dinner");
//                                    addHotel();
//                                }
//                            }
//                        });
//            }
//        }
//        else {
//            addHotel();
//        }
//    }
//
//    private void findDinnerTravelTime(FuzzySearchResult restaurant) {
//        int prev = findRestaurantFlag(proposedTrip, DINNER_FLAG) - 1;
//        RoutePlanner routePlanner = new RoutePlanner(routingApi, null);
////        routePlanner.planRoute(proposedTrip.get(prev).fuzzySearchResult.getPosition(), restaurant.getPosition(), null);
//    }
//
//    private void doneFinding() {
//        resultCallback.onPOIWithTravelTimeResult(proposedTrip);
//    }
//
//    private void findRestaurantIndex(ArrayList<POIWithTravelTime> tripDraft) {
//        Calendar rightNow = Calendar.getInstance();
//        //int hour = rightNow.get(Calendar.HOUR_OF_DAY);
//        //int minute = rightNow.get(Calendar.MINUTE);
//        //int second = rightNow.get(Calendar.SECOND);
//        int hour = 9;
//        int minute = 0;
//        int second = 0;
//        accumSecondNow = second + minute * 60 + hour * 3600;
//
//
//        for (POIWithTravelTime tripItem : tripDraft) {
//            boolean canEatBeforeThisTrip = isEatingTime(accumSecondNow);
//            boolean canEatAfterThisTrip = isEatingTime(accumSecondNow + tripItem.travelTime + DEFAULT_VISIT_TIME_IN_SECOND);
//
//            boolean shouldContinue = !betterEatNow(canEatBeforeThisTrip, canEatAfterThisTrip)
//                    || (isNoon(accumSecondNow) && isAddLunch)
//                    || (isEvening(accumSecondNow) && isAddDinner);
//
//            Log.d("DEC", "accuSec: " + accumSecondNow + " canEatBef: " + canEatBeforeThisTrip + " canEatAfter: " + canEatAfterThisTrip
//                    + " isAddLunch: " + isAddLunch + " isAddDinner: " + isAddDinner + " shouldCont: " + shouldContinue);
//
//            if (!shouldContinue) {
//                POIWithTravelTime restaurantPoint = new POIWithTravelTime(new FuzzySearchResult(), LUNCH_FLAG);
//                if (!isAddLunch) {
//                    isAddLunch = isNoon(accumSecondNow);
//                }
//                if (!isAddDinner) {
//                    isAddDinner = isEvening(accumSecondNow);
//                }
//                accumSecondNow += DEFAULT_EXPECTED_TRAVEL_TIME + DEFAULT_VISIT_TIME_IN_SECOND;
//
//                if (isAddDinner) {
//                    restaurantPoint.travelTime = DINNER_FLAG;
//                }
//
//                proposedTrip.add(restaurantPoint);
//            }
//
//            proposedTrip.add(tripItem);
//            accumSecondNow += tripItem.travelTime + DEFAULT_VISIT_TIME_IN_SECOND;
//        }
//    }
//
//    private void addHotel() {
//        POIWithTravelTime hotelPoint = new POIWithTravelTime(new FuzzySearchResult(), 12*60);
//
//        POIGenerator.queryWithCategory(searchApi, proposedTrip.get(proposedTrip.size() - 1).fuzzySearchResult.getPosition(),
//                POIGenerator.POITYPE.HOTEL, DEFAULT_HOTEL_RADIUS, new IPOISearchResult() {
//                    @Override
//                    public void onPOISearchResult(ArrayList<FuzzySearchResult> searchResult) {
//                        if (!searchResult.isEmpty()) {
//                            proposedTrip.add(new POIWithTravelTime(searchResult.get(0), 0));
//                            findHotelTravelTime(searchResult.get(0));
//                        }
//                    }
//                });
//    }
//
//    private void findHotelTravelTime(FuzzySearchResult hotel) {
//        RoutePlanner routePlanner = new RoutePlanner(routingApi, null);
////        routePlanner.planRoute(proposedTrip.get(proposedTrip.size() - 1).fuzzySearchResult.getPosition(), hotel.getPosition(), null);
//    }
//
//    private boolean betterEatNow(boolean canEatBeforeThisTrip, boolean canEatAfterThisTrip) {
//        // simplify later
//        if (!canEatBeforeThisTrip && !canEatAfterThisTrip) {
//            return false;
//        }
//        else if (canEatBeforeThisTrip && !canEatAfterThisTrip) {
//            return true;
//        }
//        else if (!canEatBeforeThisTrip && canEatAfterThisTrip) {
//            return false;
//        }
//        else {
//            return true;
//        }
//    }
//
//    private boolean isNoon(int secondOfDay) {
//        return (secondOfDay >= 11 * 3600 && secondOfDay <= 13 * 3600);
//    }
//
//    private boolean isEvening(int secondOfDay) {
//        return (secondOfDay >= 18 * 3600 && secondOfDay <= 20 * 3600);
//    }
//
//    private boolean isEatingTime(int secondOfDay) {
//        return isNoon(secondOfDay) || isEvening(secondOfDay);
//    }
}
