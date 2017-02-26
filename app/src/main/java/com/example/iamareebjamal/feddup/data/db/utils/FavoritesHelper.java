package com.example.iamareebjamal.feddup.data.db.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.example.iamareebjamal.feddup.data.db.DatabaseProvider;
import com.example.iamareebjamal.feddup.data.db.schema.PostColumns;

import rx.Observable;

public class FavoritesHelper {

    private static ContentResolver contentResolver;

    static void initialize(ContentResolver contentResolver) {
        FavoritesHelper.contentResolver = contentResolver;
    }

    private static void verify() {
        if(contentResolver == null)
            throw new IllegalAccessError("FavoriesHelper : Must call initialize with ContentResolver first");
    }

    public static Observable<String> getFavoritesFromCursor(Cursor cursor) {
        return Observable.create(subscriber -> {
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String key = cursor.getString(cursor.getColumnIndex(PostColumns.POST_KEY));
                    subscriber.onNext(key);
                    cursor.moveToNext();
                }
            }
            subscriber.onCompleted();
        });
    }

    public static Observable<String> getFavorites() {
        verify();

        return Observable.defer(() -> {
            Cursor cursor = contentResolver.query(
                    DatabaseProvider.Favorites.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);

            Observable<String> results = getFavoritesFromCursor(cursor);

            if(cursor != null) cursor.close();

            return results;
        });
    }

    public static Observable<Boolean> addFavorite(String key) {
        verify();

        return Observable.create(subscriber -> {
            ContentValues values = new ContentValues();
            values.put(PostColumns.POST_KEY, key);

            Uri uri = contentResolver.insert(DatabaseProvider.Favorites.CONTENT_URI,
                    values);

            subscriber.onNext(uri);
            subscriber.onCompleted();
        }).map(uri -> uri != null);
    }

    public static Observable<Integer> removeFavorite(String key) {
        verify();


        return Observable.create(subscriber -> {

            int rows = contentResolver.delete(DatabaseProvider.Favorites.CONTENT_URI,
                    PostColumns.POST_KEY + "=?", new String[]{key});

            subscriber.onNext(rows);
            subscriber.onCompleted();
        });
    }
}
