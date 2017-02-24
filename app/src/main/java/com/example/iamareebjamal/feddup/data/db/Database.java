package com.example.iamareebjamal.feddup.data.db;

import com.example.iamareebjamal.feddup.data.db.schema.DraftColumns;
import com.example.iamareebjamal.feddup.data.db.schema.PostColumns;

import net.simonvt.schematic.annotation.Table;

@net.simonvt.schematic.annotation.Database(version = Database.VERSION, packageName = "com.example.iamareebjamal.feddup.data.db.provider")
public class Database {

    public static final int VERSION = 1;

    @Table(DraftColumns.class)
    public static final String Drafts = "Drafts";

    @Table(PostColumns.class)
    public static final String Favorites = "Favorites";

    @Table(PostColumns.class)
    public static final String Downvotes = "Downvotes";

    private Database(){ }
}
