package com.iamareebjamal.feddup.data.db.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.iamareebjamal.feddup.data.db.DatabaseProvider;
import com.iamareebjamal.feddup.data.db.schema.PostCacheColumns;
import com.iamareebjamal.feddup.data.db.schema.PostColumns;
import com.iamareebjamal.feddup.data.models.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import rx.Observable;
import rx.schedulers.Schedulers;

public class FavoritesHelper {

    private static ContentResolver contentResolver;

    static void initialize(ContentResolver contentResolver) {
        FavoritesHelper.contentResolver = contentResolver;
    }

    private static void verify() {
        if(contentResolver == null)
            throw new IllegalAccessError("FavoritesHelper : Must call initialize with ContentResolver first");
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

    public static Observable<Post> getFavoritePostsFromCursor(Cursor cursor) {
        return Observable.create(subscriber -> {
            if (cursor != null) {
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    Post post = new Post();
                    post.key = cursor.getString(cursor.getColumnIndex(PostCacheColumns.key));
                    post.title = cursor.getString(cursor.getColumnIndex(PostCacheColumns.title));
                    post.url = cursor.getString(cursor.getColumnIndex(PostCacheColumns.url));

                    subscriber.onNext(post);
                    cursor.moveToNext();
                }
            }
            subscriber.onCompleted();
        });
    }

    private static void addOrUpdateCache(String key) {
        verify();

        Observable.fromCallable(() -> {
            ContentValues values = new ContentValues();
            values.put(PostCacheColumns.key, key);
            values.put(PostCacheColumns.key, key);

            FirebaseDatabase.getInstance().getReference("posts").child(key)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Post post = dataSnapshot.getValue(Post.class);
                            values.put(PostCacheColumns.title, post.title);
                            values.put(PostCacheColumns.url, post.url);

                            int rows = contentResolver.update(
                                    DatabaseProvider.PostCache.CONTENT_URI, values,
                                    PostCacheColumns.key + "=?",
                                    new String[]{ key }
                            );

                            if (rows == 0)
                                contentResolver.insert(
                                        DatabaseProvider.PostCache.CONTENT_URI,
                                        values
                                );
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Nothing to save here
                        }
                    });
            return null;
        }).subscribeOn(Schedulers.computation()).subscribe();

    }

    public static Observable<Boolean> addFavorite(String key) {
        verify();

        return Observable.create(subscriber -> {
            ContentValues values = new ContentValues();
            values.put(PostColumns.POST_KEY, key);

            Uri uri = contentResolver.insert(DatabaseProvider.Favorites.CONTENT_URI,
                    values);

            addOrUpdateCache(key);

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
