package com.example.iamareebjamal.feddup.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.example.iamareebjamal.feddup.data.db.schema.DraftColumns;

import rx.Observable;

public class DatabaseHelper {

    public static Observable<Uri> insertDraft(Context context, String title, String author, String content, String filePath) {

        return Observable.create(subscriber -> {
            ContentValues values = new ContentValues();
            values.put(DraftColumns.title, title);
            values.put(DraftColumns.author, author);
            values.put(DraftColumns.content, content);
            values.put(DraftColumns.filePath, filePath);
            Uri uri = context.getContentResolver().insert(DatabaseProvider.Drafts.CONTENT_URI, values);

            subscriber.onNext(uri);
            subscriber.onCompleted();
        });
    }
}
