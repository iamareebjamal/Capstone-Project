package com.example.iamareebjamal.feddup.data.db;

import android.net.Uri;

import com.example.iamareebjamal.feddup.data.db.schema.DraftColumns;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = DatabaseProvider.AUTHORITY, database = Database.class)
public class DatabaseProvider {

    public static final String AUTHORITY =  "iamareebjamal.feddup.DatabaseProvider";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = Database.Drafts)
    public static class Drafts {
        @ContentUri(
                path = Database.Drafts,
                type = "vnd.android.cursor.dir/Drafts"
        )

        public static final Uri CONTENT_URI = buildUri(Database.Drafts);
        @InexactContentUri(
                name = "COUPON_ID",
                path = Database.Drafts + "/#",
                type = "vnd.android.cursor.item/Draft",
                whereColumn = DraftColumns._ID,
                pathSegment = 1
        )

        public static Uri withId(int id) {
            return buildUri(Database.Drafts, String.valueOf(id));
        }
    }

}
