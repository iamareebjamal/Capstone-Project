package com.example.iamareebjamal.feddup.data.db.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.example.iamareebjamal.feddup.data.db.DatabaseProvider;
import com.example.iamareebjamal.feddup.data.db.schema.PostColumns;

import rx.Observable;

public class FavoritesHelper {

    private Context context;

    public FavoritesHelper(Context context) {
        this.context = context;
    }

    public Observable<String> getFavoritesFromCursor(Cursor cursor) {
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

    public Observable<String> getFavorites() {
        return Observable.defer(() -> {
            Cursor cursor = context.getContentResolver().query(
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

    public Observable<Boolean> addFavorite(String key) {

        return Observable.create(subscriber -> {
            ContentValues values = new ContentValues();
            values.put(PostColumns.POST_KEY, key);

            Uri uri = context.getContentResolver().insert(DatabaseProvider.Favorites.CONTENT_URI,
                    values);

            subscriber.onNext(uri);
            subscriber.onCompleted();
        }).map(uri -> uri != null);
    }

    public Observable<Integer> removeFavorite(String key) {

        return Observable.create(subscriber -> {

            int rows = context.getContentResolver().delete(DatabaseProvider.Favorites.CONTENT_URI,
                    PostColumns.POST_KEY + "=?", new String[]{key});

            subscriber.onNext(rows);
            subscriber.onCompleted();
        });
    }
}
