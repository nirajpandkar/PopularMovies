package com.xipherlabs.popularmovies.db;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

@Database(version = FavoriteDatabase.DATABASE_VERSION)
public final class FavoriteDatabase {
    public static final int DATABASE_VERSION = 1;

    @Table(MovieContract.MovieEntry.class) public static final String TABLE_NAME = "favmovies";

}