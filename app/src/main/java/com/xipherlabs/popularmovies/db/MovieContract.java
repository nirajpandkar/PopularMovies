package com.xipherlabs.popularmovies.db;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.Unique;

public class MovieContract {
    public static abstract class MovieEntry {
        public static final String TABLE_NAME = "favmovies";

        @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement public static final String _ID = "_id";
        @DataType(DataType.Type.INTEGER) @NotNull @Unique public static final String COL_TMDB_ID = "tmdb_id";
        @DataType(DataType.Type.TEXT) @NotNull public static final String COL_TITLE = "title";
        @DataType(DataType.Type.TEXT) @NotNull public static final String COL_DESC = "description";
        @DataType(DataType.Type.TEXT) @NotNull public static final String COL_POSTER_PATH = "poster_path";
        @DataType(DataType.Type.TEXT) @NotNull public static final String COL_REL_DATE = "release_date";
        @DataType(DataType.Type.TEXT) @NotNull public static final String COL_ORIGINAL_TITLE = "original_title";
        @DataType(DataType.Type.TEXT) @NotNull public static final String COL_BACKDROP_PATH = "backdrop_path";
        @DataType(DataType.Type.TEXT) @NotNull public static final String COL_VOTE_AVG = "vote_average";
    }
}
