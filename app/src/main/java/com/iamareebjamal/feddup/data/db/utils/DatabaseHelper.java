package com.iamareebjamal.feddup.data.db.utils;

import android.content.ContentResolver;

import java.util.HashSet;
import java.util.Set;

public class DatabaseHelper {

    private static Set<String> downvoted = new HashSet<>();
    private static Set<String> favorites = new HashSet<>();

    public static void addDownVoted(String key) {
        downvoted.add(key);
    }

    public static void clearDownVoted() {
        downvoted.clear();
    }

    public static void addFavorite(String key) {
        favorites.add(key);
    }

    public static void clearFavorites() {
        favorites.clear();
    }

    public static int getFavoriteCount() {
        return favorites.size();
    }

    public static boolean isFavorite(String key) {
        return favorites.contains(key);
    }

    public static boolean isDownvoted(String key) {
        return downvoted.contains(key);
    }

    public static void initialize(ContentResolver contentResolver) {
        FavoritesHelper.initialize(contentResolver);
        DownvotesHelper.initialize(contentResolver);
        DraftsHelper.initialize(contentResolver);
    }
}
