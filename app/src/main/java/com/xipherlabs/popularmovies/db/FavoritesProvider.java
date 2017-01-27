package com.xipherlabs.popularmovies.db;

import android.net.Uri;
import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = FavoritesProvider.AUTHORITY, database = FavoriteDatabase.class)
public class FavoritesProvider {

    public static final String AUTHORITY = "com.xipherlabs.popularmovies";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String MOVIES = "movies";
    }

    @TableEndpoint(table = FavoriteDatabase.TABLE_NAME) public static class Movies {

        @ContentUri(
                path = Path.MOVIES,
                type = "vnd.android.cursor.dir/movie",
                defaultSort = MovieContract.MovieEntry._ID + " DESC")
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Path.MOVIES).build();

        @InexactContentUri(
                path = Path.MOVIES + "/#",
                name = "MOVIE_ID",
                type = "vnd.android.cursor.item/movie",
                whereColumn = MovieContract.MovieEntry._ID,
                pathSegment = 1)
        public static Uri withId(long id) {
            return BASE_CONTENT_URI.buildUpon().appendPath(Path.MOVIES).appendPath(String.valueOf(id)).build();
        }
    }

}