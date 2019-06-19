package com.taipei.ttbootcamp.dailydecorator;

import android.util.Log;

import com.taipei.ttbootcamp.interfaces.IPOISearchResult;
import com.taipei.ttbootcamp.interfaces.IPOIWithTravelTimeResult;
import com.taipei.ttbootcamp.interfaces.POIWithTravelTime;
import com.taipei.ttbootcamp.poigenerator.POIGenerator;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DailyNeedDecorator {

    private static final int DEFAULT_VISIT_TIME_IN_SECOND = 3600; // one hour
    private static final int DEFAULT_RESTAURANT_RADIUS = 10;
    private static final int DEFAULT_HOTEL_RADIUS = 30;


    public static ArrayList<POIWithTravelTime> generateDailyNeed(ArrayList<POIWithTravelTime> tripDraft,
                                                                 SearchApi searchApi, IPOIWithTravelTimeResult resultCallback) {
        ArrayList<POIWithTravelTime> proposedTrip = new ArrayList<POIWithTravelTime>();
        addRestaurants(tripDraft, searchApi, proposedTrip, resultCallback);
        addHotel(searchApi, proposedTrip);
        return proposedTrip;
    }

    private static void addRestaurants(ArrayList<POIWithTravelTime> tripDraft, SearchApi searchApi,
                                       ArrayList<POIWithTravelTime> proposedTrip, IPOIWithTravelTimeResult resultCallback) {
        Calendar rightNow = Calendar.getInstance();
        //int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        //int minute = rightNow.get(Calendar.MINUTE);
        //int second = rightNow.get(Calendar.SECOND);
        int hour = 9;
        int minute = 0;
        int second = 0;
        int accumSecondNow = second + minute * 60 + hour * 3600;
        boolean isAddBreakfast = false;
        boolean isAddLunch = false;
        boolean isAddDinner = false;

        for (POIWithTravelTime tripItem : tripDraft) {
            boolean canEatBeforeThisTrip = isEatingTime(accumSecondNow);
            boolean canEatAfterThisTrip = isEatingTime(accumSecondNow + tripItem.travelTime + DEFAULT_VISIT_TIME_IN_SECOND);

            boolean shouldContinue = !betterEatNow(canEatBeforeThisTrip, canEatAfterThisTrip)
                    || (isNoon(accumSecondNow) && isAddLunch)
                    || (isEvening(accumSecondNow) && isAddDinner);

            Log.d("DEC", "accuSec: " + accumSecondNow + " canEatBef: " + canEatBeforeThisTrip + " canEatAfter: " + canEatAfterThisTrip
                    + " isAddLunch: " + isAddLunch + " isAddDinner: " + isAddDinner + " shouldCont: " + shouldContinue);

            if (!shouldContinue) {
                POIWithTravelTime restaurantPoint = new POIWithTravelTime(new FuzzySearchResult(), 13*60);

                //TODO callback restaurant and add to list.
                POIGenerator.getPOIsWithType(searchApi, tripItem.result.getPosition(), POIGenerator.POITYPE.FOOD, DEFAULT_RESTAURANT_RADIUS,
                        new IPOISearchResult() {
                            @Override
                            public void onPOISearchResult(ArrayList<FuzzySearchResult> searchResult) {
                                if (!searchResult.isEmpty()) {

                                }
                            }
                        }
                );

                isAddLunch = isNoon(accumSecondNow);
                isAddDinner = isEvening(accumSecondNow);
                accumSecondNow += restaurantPoint.travelTime + DEFAULT_VISIT_TIME_IN_SECOND;
                proposedTrip.add(restaurantPoint);
            }

            proposedTrip.add(tripItem);
            accumSecondNow += tripItem.travelTime + DEFAULT_VISIT_TIME_IN_SECOND;
        }
    }

    private static void addHotel(SearchApi searchApi, ArrayList<POIWithTravelTime> proposedTrip) {
        POIWithTravelTime hotelPoint = new POIWithTravelTime(new FuzzySearchResult(), 12*60);

        //TODO callback restaurant and add to list.
        //POIGenerator.getPOIsWithType(searchApi, tripItem.reqult.getPosition(), POIGenerator.POITYPE.HOTEL, DEFAULT_HOTEL_RADIUS);

        proposedTrip.add(hotelPoint);
    }

    private static boolean betterEatNow(boolean canEatBeforeThisTrip, boolean canEatAfterThisTrip) {
        // simplify later
        if (!canEatBeforeThisTrip && !canEatAfterThisTrip) {
            return false;
        }
        else if (canEatBeforeThisTrip && !canEatAfterThisTrip) {
            return true;
        }
        else if (!canEatBeforeThisTrip && canEatAfterThisTrip) {
            return false;
        }
        else {
            return true;
        }
    }

    private static boolean isNoon(int secondOfDay) {
        return (secondOfDay >= 11 * 3600 && secondOfDay <= 13 * 3600);
    }

    private static boolean isEvening(int secondOfDay) {
        return (secondOfDay >= 18 * 3600 && secondOfDay <= 20 * 3600);
    }

    private static boolean isEatingTime(int secondOfDay) {
        return isNoon(secondOfDay) || isEvening(secondOfDay);
    }
}
