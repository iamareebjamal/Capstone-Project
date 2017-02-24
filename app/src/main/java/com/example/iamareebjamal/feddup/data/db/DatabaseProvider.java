package com.example.iamareebjamal.feddup.data.db;

import android.net.Uri;

import com.example.iamareebjamal.feddup.data.db.schema.DraftColumns;
import com.example.iamareebjamal.feddup.data.db.schema.PostColumns;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = DatabaseProvider.AUTHORITY, database = Database.class, packageName="com.example.iamareebjamal.feddup.data.db.provider")
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
                name = "DRAFT_ID",
                path = Database.Drafts + "/#",
                type = "vnd.android.cursor.item/Draft",
                whereColumn = DraftColumns._ID,
                pathSegment = 1
        )

        public static Uri withId(int id) {
            return buildUri(Database.Drafts, String.valueOf(id));
        }
    }

    @TableEndpoint(table = Database.Favorites)
    public static class Favorites {
        @ContentUri(
                path = Database.Favorites,
                type = "vnd.android.cursor.dir/Favorites"
        )

        public static final Uri CONTENT_URI = buildUri(Database.Favorites);
        @InexactContentUri(
                name = "FAVORITE_ID",
                path = Database.Favorites + "/#",
                type = "vnd.android.cursor.item/Favorite",
                whereColumn = PostColumns.POST_KEY,
                pathSegment = 1
        )

        public static Uri withId(String id) {
            return buildUri(Database.Favorites, id);
        }
    }

    @TableEndpoint(table = Database.Downvotes)
    public static class Downvotes {
        @ContentUri(
                path = Database.Downvotes,
                type = "vnd.android.cursor.dir/Downvotes"
        )

        public static final Uri CONTENT_URI = buildUri(Database.Downvotes);
        @InexactContentUri(
                name = "DOWNVOTE_ID",
                path = Database.Downvotes + "/#",
                type = "vnd.android.cursor.item/Downvote",
                whereColumn = PostColumns.POST_KEY,
                pathSegment = 1
        )

        public static Uri withId(String id) {
            return buildUri(Database.Downvotes, id);
        }
    }

}
