package com.example.iamareebjamal.feddup.data.db.schema;

import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

public class PostColumns {

    @DataType(TEXT)
    @PrimaryKey
    @NotNull
    public static final String POST_KEY = "post_key";

}