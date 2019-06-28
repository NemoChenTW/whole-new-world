package com.taipei.ttbootcamp.interfaces;

import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;

import java.util.ArrayList;

public interface IOptimizeResultListener {
    void onOptimizeResult(ArrayList<FuzzySearchResult> searchResult);
}
