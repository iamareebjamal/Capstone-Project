package com.iamareebjamal.feddup.data.db.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.iamareebjamal.feddup.data.db.DatabaseProvider;
import com.iamareebjamal.feddup.data.db.schema.DraftColumns;
import com.iamareebjamal.feddup.data.models.PostDraft;

import rx.Observable;

public class DraftsHelper {

    private static ContentResolver contentResolver;

    static void initialize(ContentResolver contentResolver) {
        DraftsHelper.contentResolver = contentResolver;
    }

    private static void verify() {
        if (contentResolver == null)
            throw new IllegalAccessError("DraftsHelper : Must call initialize with ContentResolver first");
    }

    public static Observable<PostDraft> getDraftsFromCursor(Cursor cursor) {
        return Observable.create(subscriber -> {
            if (cursor != null) {
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    int id = cursor.getInt(cursor.getColumnIndex(DraftColumns._ID));
                    String title = cursor.getString(cursor.getColumnIndex(DraftColumns.title));
                    String author = cursor.getString(cursor.getColumnIndex(DraftColumns.author));
                    String content = cursor.getString(cursor.getColumnIndex(DraftColumns.content));
                    String filePath = cursor.getString(cursor.getColumnIndex(DraftColumns.filePath));

                    PostDraft postDraft = new PostDraft();
                    postDraft.setId(id);
                    postDraft.setTitle(title);
                    postDraft.setAuthor(author);
                    postDraft.setContent(content);
                    postDraft.setFilePath(filePath);

                    subscriber.onNext(postDraft);

                    cursor.moveToNext();
                }

            }

            subscriber.onCompleted();
        });
    }

    public static Observable<PostDraft> getDrafts() {
        verify();

        return Observable.defer(() -> {
            Cursor cursor = contentResolver.query(
                    DatabaseProvider.Drafts.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);

            return getDraftsFromCursor(cursor);
        });
    }

    public static Observable<Uri> insertDraft(PostDraft postDraft) {
        verify();

        return Observable.create(subscriber -> {
            ContentValues values = new ContentValues();
            values.put(DraftColumns.title, postDraft.getTitle());
            values.put(DraftColumns.author, postDraft.getAuthor());
            values.put(DraftColumns.content, postDraft.getContent());
            values.put(DraftColumns.filePath, postDraft.getFilePath());

            Uri uri = contentResolver.insert(DatabaseProvider.Drafts.CONTENT_URI, values);

            subscriber.onNext(uri);
            subscriber.onCompleted();
        });
    }

    public static Observable<Integer> updateDraft(Uri draftUri, PostDraft postDraft) {
        verify();

        return Observable.create(subscriber -> {
            ContentValues values = new ContentValues();
            values.put(DraftColumns.title, postDraft.getTitle());
            values.put(DraftColumns.author, postDraft.getAuthor());
            values.put(DraftColumns.content, postDraft.getContent());
            values.put(DraftColumns.filePath, postDraft.getFilePath());

            int rows = contentResolver.update(draftUri, values, null, null);

            subscriber.onNext(rows);
            subscriber.onCompleted();
        });
    }

    public static Observable<Integer> deleteUri(Uri uri) {
        verify();

        return Observable.create(subscriber -> {

            int rows = contentResolver.delete(uri, null, null);

            subscriber.onNext(rows);
            subscriber.onCompleted();
        });
    }
}
