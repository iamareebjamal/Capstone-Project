package com.example.iamareebjamal.feddup.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.iamareebjamal.feddup.data.models.PostDraft;
import com.example.iamareebjamal.feddup.data.db.schema.DraftColumns;

import rx.Observable;

public class DatabaseHelper {

    private Context context;

    public DatabaseHelper(Context context) {
        this.context = context;
    }


    public Observable<PostDraft> getDraftsFromCursor(Cursor cursor) {
        return Observable.create(subscriber -> {
            if (cursor != null) {
                cursor.moveToFirst();

                do {

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
                } while (cursor.moveToNext());
            }

            subscriber.onCompleted();
        });
    }

    public Observable<PostDraft> getDrafts() {
        return Observable.defer(() -> {
            Cursor cursor = context.getContentResolver().query(
                    DatabaseProvider.Drafts.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);

            return getDraftsFromCursor(cursor);
        });
    }

    public Observable<Uri> insertDraft(PostDraft postDraft) {

        return Observable.create(subscriber -> {
            ContentValues values = new ContentValues();
            values.put(DraftColumns.title, postDraft.getTitle());
            values.put(DraftColumns.author, postDraft.getAuthor());
            values.put(DraftColumns.content, postDraft.getContent());
            values.put(DraftColumns.filePath, postDraft.getFilePath());

            Uri uri = context.getContentResolver().insert(DatabaseProvider.Drafts.CONTENT_URI, values);

            subscriber.onNext(uri);
            subscriber.onCompleted();
        });
    }

    /* Extremely hacky method for development mode only */
    public Observable<Uri> insertDraft(String... args) {

        PostDraft postDraft = new PostDraft();

        try {
            postDraft.setTitle(args[0]);
            postDraft.setAuthor(args[1]);
            postDraft.setContent(args[2]);
            postDraft.setFilePath(args[3]);
        } catch (IndexOutOfBoundsException ioe) {
            Log.d("DB", "Completed on Exception");
        }

        return insertDraft(postDraft);
    }

    public Observable<Integer> updateDraft(Uri draftUri, PostDraft postDraft) {
        return Observable.create(subscriber -> {
            ContentValues values = new ContentValues();
            values.put(DraftColumns.title, postDraft.getTitle());
            values.put(DraftColumns.author, postDraft.getAuthor());
            values.put(DraftColumns.content, postDraft.getContent());
            values.put(DraftColumns.filePath, postDraft.getFilePath());

            int rows = context.getContentResolver().update(draftUri, values, null, null);

            subscriber.onNext(rows);
            subscriber.onCompleted();
        });
    }

    public Observable<Integer> deleteUri(Uri uri) {
        return Observable.create(subscriber -> {

            int rows = context.getContentResolver().delete(uri, null, null);

            subscriber.onNext(rows);
            subscriber.onCompleted();
        });
    }
}
