package com.example.iamareebjamal.feddup.data.db;

import com.example.iamareebjamal.feddup.data.db.schema.DraftColumns;

import net.simonvt.schematic.annotation.Table;

@net.simonvt.schematic.annotation.Database(version = Database.VERSION )
public class Database {

    public static final int VERSION = 4;

    @Table(DraftColumns.class)
    public static final String Drafts = "Drafts";

    private Database(){}
}
