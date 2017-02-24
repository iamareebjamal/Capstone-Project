package com.example.iamareebjamal.feddup.data.db.schema;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

public interface DraftColumns {

    @DataType(INTEGER)
    @PrimaryKey
    @AutoIncrement
    String _ID = "_id";

    @DataType(TEXT)
    String title = "title";

    @DataType(TEXT)
    String author = "author";

    @DataType(TEXT)
    String content = "content";

    @DataType(TEXT)
    String filePath = "filePath";
}
