package com.example.iamareebjamal.feddup.data.db.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.example.iamareebjamal.feddup.data.db.DatabaseProvider;
import com.example.iamareebjamal.feddup.data.db.schema.PostColumns;
import com.google.firebase.database.FirebaseDatabase;

import rx.Observable;

public class DownvotesHelper {

    private static ContentResolver contentResolver;

    static void initialize(ContentResolver contentResolver) {
        DownvotesHelper.contentResolver = contentResolver;
    }

    private static void verify() {
        if(contentResolver == null)
            throw new IllegalAccessError("DownvotesHelper : Must call initialize with ContentResolver first");
    }

    public static Observable<String> getDowvotesFromCursor(Cursor cursor) {
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

    public static Observable<String> getDownvotes() {
        verify();

        return Observable.defer(() -> {
            Cursor cursor = contentResolver.query(
                    DatabaseProvider.Downvotes.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);

            Observable<String> results = getDowvotesFromCursor(cursor);

            if(cursor != null) cursor.close();

            return results;
        });
    }

    public static Observable<Boolean> isDownvoted(String key) {
        verify();

        return Observable.create(subscriber -> {
            Cursor cursor = contentResolver.query(
                    DatabaseProvider.Downvotes.CONTENT_URI,
                    null,
                    PostColumns.POST_KEY + "=?",
                    new String[]{key},
                    null);

            subscriber.onNext(cursor!= null && cursor.getCount() > 0);

            if(cursor != null) cursor.close();

            subscriber.onCompleted();
        });
    }

    public static Observable<Boolean> addDownvote(String key, int currentValue) {
        verify();

        return Observable.create(subscriber -> {
            ContentValues values = new ContentValues();
            values.put(PostColumns.POST_KEY, key);

            Uri uri = contentResolver.insert(DatabaseProvider.Downvotes.CONTENT_URI,
                    values);

            if(uri != null)
                FirebaseDatabase.getInstance()
                    .getReference("posts/"+key)
                    .child("downvotes")
                    .setValue(currentValue + 1);

            subscriber.onNext(uri);
            subscriber.onCompleted();
        }).map(uri -> uri != null);
    }

    public static Observable<Integer> removeDownvote(String key, int currentValue) {
        verify();

        return Observable.create(subscriber -> {

            int rows = contentResolver.delete(DatabaseProvider.Downvotes.CONTENT_URI,
                    PostColumns.POST_KEY + "=?", new String[]{key});

            if (rows > 0)
                FirebaseDatabase.getInstance()
                    .getReference("posts/"+key)
                    .child("downvotes")
                    .setValue(currentValue - 1);

            subscriber.onNext(rows);
            subscriber.onCompleted();
        });
    }
}
