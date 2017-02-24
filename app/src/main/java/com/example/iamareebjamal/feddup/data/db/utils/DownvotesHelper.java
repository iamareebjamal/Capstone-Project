package com.example.iamareebjamal.feddup.data.db.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.example.iamareebjamal.feddup.data.db.DatabaseProvider;
import com.example.iamareebjamal.feddup.data.db.schema.PostColumns;

import rx.Observable;

public class DownvotesHelper {
    private Context context;

    public DownvotesHelper(Context context) {
        this.context = context;
    }

    public Observable<String> getDowvotesFromCursor(Cursor cursor) {
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

    public Observable<String> getDownvotes() {
        return Observable.defer(() -> {
            Cursor cursor = context.getContentResolver().query(
                    DatabaseProvider.Downvotes.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);

            return getDowvotesFromCursor(cursor);
        });
    }

    public Observable<Boolean> addDownvote(String key) {

        return Observable.create(subscriber -> {
            ContentValues values = new ContentValues();
            values.put(PostColumns.POST_KEY, key);

            Uri uri = context.getContentResolver().insert(DatabaseProvider.Downvotes.CONTENT_URI,
                    values);

            subscriber.onNext(uri);
            subscriber.onCompleted();
        }).map(uri -> uri != null);
    }

    public Observable<Integer> removeDownvote(String key) {

        return Observable.create(subscriber -> {

            int rows = context.getContentResolver().delete(DatabaseProvider.Downvotes.CONTENT_URI,
                    PostColumns.POST_KEY + "=?", new String[]{key});

            subscriber.onNext(rows);
            subscriber.onCompleted();
        });
    }
}
