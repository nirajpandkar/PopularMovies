package com.xipherlabs.popularmovies.db;

import android.provider.BaseColumns;

public class MovieContract {
    public static abstract class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "favmovies";

        public static final String COL_TMDB_ID = "tmdb_id";
        public static final String COL_TITLE = "title";
        public static final String COL_DESC = "description";
        public static final String COL_POSTER_PATH = "poster_path";
        public static final String COL_REL_DATE = "release_date";
        public static final String COL_ORIGINAL_TITLE = "original_title";
        public static final String COL_BACKDROP_PATH = "backdrop_path";
        public static final String COL_VOTE_AVG = "vote_average";
    }
}
