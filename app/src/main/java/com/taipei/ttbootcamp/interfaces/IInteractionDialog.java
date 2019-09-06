package com.taipei.ttbootcamp.interfaces;

import android.content.Context;

import com.taipei.ttbootcamp.data.TripData;

public interface IInteractionDialog {
    void initialDialog(Context context);
    void setResultDialog(TripData tripData, boolean isNeedRestaurent);
    void setOptimizeInterface(IOptimizeResultCallBack optimizeResult);
}
