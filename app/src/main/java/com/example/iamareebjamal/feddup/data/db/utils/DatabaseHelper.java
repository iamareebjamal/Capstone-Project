package com.example.iamareebjamal.feddup.data.db.utils;

import android.content.ContentResolver;

public class DatabaseHelper {

    public static void initialize(ContentResolver contentResolver) {
        FavoritesHelper.initialize(contentResolver);
        DownvotesHelper.initialize(contentResolver);
        DraftsHelper.initialize(contentResolver);
    }
}
