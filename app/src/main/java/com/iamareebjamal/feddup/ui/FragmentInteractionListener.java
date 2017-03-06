package com.iamareebjamal.feddup.ui;

public interface FragmentInteractionListener {
    void onFabDisplay(boolean show);

    void onPostSelect(String key);

    void onPostStart(String key);
}