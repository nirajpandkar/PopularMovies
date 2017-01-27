package com.xipherlabs.popularmovies;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.xipherlabs.popularmovies.db.FavoritesProvider;
import com.xipherlabs.popularmovies.db.MovieContract;
import com.xipherlabs.popularmovies.db.MovieDbHelper;
import com.xipherlabs.popularmovies.model.Movie;
import com.xipherlabs.popularmovies.model.ResultsDiscover;
import com.xipherlabs.popularmovies.rest.MovieService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

import static com.xipherlabs.popularmovies.R.id.thumbnail;

public class MovieFragment extends Fragment {

    MovieAdapter mAdapter;
    @BindView(R.id.poster_grid)
    GridView movieGrid;

    public MovieFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie, container, false);
        ButterKnife.bind(this,view);
        FetchDataTask dataTask = new FetchDataTask(getContext());
        //dataTask.execute(FetchDataTask.SORT_BY_POPULARITY);


        NetworkInfo networkInfo = ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            dataTask.execute(FetchDataTask.SORT_BY_POPULARITY);
        } else {
            Toast.makeText(getContext(), R.string.string_review_network_error, Toast.LENGTH_SHORT).show();
        }

        //movieGrid = (GridView) view.findViewById(R.id.poster_grid);
        mAdapter = new MovieAdapter(getContext(), new ArrayList<Movie>());
        movieGrid.setAdapter(mAdapter);
        return view;
    }

    @OnItemClick(R.id.poster_grid)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void gridItemClick(AdapterView<?> parent, View view, int position, long id) {
       /* Intent intent = new Intent(getContext(), MovieDetailsActivity.class);intent.putExtra(MovieDetailsActivity.ARG_MOVIE, (Movie) parent.getItemAtPosition(position));
        View v = (View) getActivity().findViewById(R.id.thumbnail);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this.getActivity(), v, getString(R.string.activity_image_trans));
            startActivity(intent, options.toBundle());
        }
        else {
            startActivity(intent);
        }*/
            Intent i = new Intent(getContext(), MovieDetailsActivity.class);
            i.putExtra(MovieDetailsActivity.ARG_MOVIE, (Movie) parent.getItemAtPosition(position));
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                View statusBar = getActivity().getWindow().getDecorView().findViewById(android.R.id.statusBarBackground);
                View navigationBar = getActivity().getWindow().getDecorView().findViewById(android.R.id.navigationBarBackground);
                View toolbar = getActivity().findViewById(R.id.toolbar);
                List<Pair<View, String>> pairs = new ArrayList<Pair<View, String>>();
                pairs.add(Pair.create(toolbar, "toolbar"));
                if(statusBar != null) pairs.add(Pair.create(statusBar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
                if(navigationBar != null) pairs.add(Pair.create(navigationBar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
                pairs.add(Pair.create(view, "poster"));
                pairs.add(Pair.create(view,getString(R.string.activity_image_trans)));
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), pairs.toArray(new Pair[pairs.size()]));
                ActivityCompat.startActivity(getActivity(), i, optionsCompat.toBundle());
            }else {
                startActivity(i);
            }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_movie_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.sort_by_popularity:
                new FetchDataTask(getContext()).execute(FetchDataTask.SORT_BY_POPULARITY);
                return true;
            case R.id.sort_by_rating:
                new FetchDataTask(getContext()).execute(FetchDataTask.SORT_BY_RATING);
                return true;
            case R.id.favoritesOnly:
                new DbReader(getContext()).execute();
                return  true;
            default:
                return false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("scrollPos", movieGrid.getFirstVisiblePosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            movieGrid.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    movieGrid.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    movieGrid.setSelection(savedInstanceState.getInt("scrollPos"));
                }
            });
        }
    }

    public class FetchDataTask extends AsyncTask<String, Void, List<Movie>> {

        public static final String BASE_URL = "https://api.themoviedb.org/3/discover/movie?";
        public static final String BASE_URL2 = "https://api.themoviedb.org/3/";

        public static final String PARAM_API_KEY = "api_key";
        public static final String PARAM_SORT_BY = "sort_by";
        public static final String SORT_BY_POPULARITY = "popularity.desc";
        public static final String SORT_BY_RATING = "vote_average.desc";
        public static final String KEY_TITLE = "title";
        public static final String KEY_ORIGINAL_TITLE = "original_title";
        public static final String KEY_ID = "id";
        public static final String KEY_POSTER_PATH = "poster_path";
        public static final String KEY_DESCRIPTION = "overview";
        public static final String KEY_RELEASE_DATE = "release_date";
        public static final String KEY_BACKDROP_PATH = "backdrop_path";
        public static final String KEY_VOTE_AVG = "vote_average";
        private Context mContext;
        boolean networkError = false;
        FetchDataTask(Context context) {
            mContext = context;
        }

        @Override
        protected List<Movie> doInBackground(String... sortBy) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String responseJson;

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL2)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            try {

                    MovieService movieService = retrofit.create(MovieService.class);
                    Call<ResultsDiscover> call = movieService.getPopularMovies(mContext.getString(R.string.api), sortBy[0], (sortBy[0].equals(SORT_BY_RATING) ? "1000" : "0"));
                    Response<ResultsDiscover> response = call.execute();
                    return response.body().getResults();
                } catch (IOException e) {
                    e.printStackTrace();
                    if(e instanceof UnknownHostException) {
                        networkError = true;
                    }
                }

                /*
                try{
                    Uri.Builder uriBuilder = Uri.parse(BASE_URL).buildUpon()
                            .appendQueryParameter(PARAM_SORT_BY, sortBy[0])
                            .appendQueryParameter(PARAM_API_KEY, mContext.getString(R.string.api));
                    if(sortBy[0].equals(SORT_BY_RATING))
                        uriBuilder.appendQueryParameter("vote_count.gte", "500"); //consider Votes for better/relevant results

                    Uri uri = uriBuilder.build();
                    URL url = new URL(uri.toString());

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if(inputStream == null) {
                        responseJson = null;
                        return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if(buffer.length() == 0) {
                    responseJson = null;
                }

                responseJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
                responseJson = null;
            } finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }

                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                return parseJson(responseJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/

            return null;
        }

        private Movie[] parseJson(String jsonString) throws JSONException {
            JSONObject moviesObj = new JSONObject(jsonString);
            JSONArray results = moviesObj.getJSONArray("results");

            Movie[] data = new Movie[results.length()];

            for(int i = 0; i < results.length(); i++) {
                JSONObject movieData = results.getJSONObject(i);
                data[i] = new Movie(movieData.getString(KEY_TITLE),
                        movieData.getString(KEY_DESCRIPTION),
                        movieData.getString(KEY_POSTER_PATH),
                        movieData.getString(KEY_RELEASE_DATE),
                        movieData.getLong(KEY_ID),
                        movieData.getString(KEY_ORIGINAL_TITLE),
                        movieData.getString(KEY_BACKDROP_PATH),
                        movieData.getString(KEY_VOTE_AVG));
            }

            return data;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            if (movies != null) {
                mAdapter.clear();
                mAdapter.addAll(movies);
                mAdapter.notifyDataSetChanged();
            } else {
                if(networkError) {
                    Toast.makeText(mContext, "Network error", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public class DbReader extends AsyncTask<Void, Void, List<Movie>> {

        private Context mContext;

        public DbReader(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected List<Movie> doInBackground(Void... params) {

            MovieDbHelper dbHelper = new MovieDbHelper(mContext);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            List<Movie> movieList = new ArrayList<>();

            //Cursor c = db.query(MovieContract.MovieEntry.TABLE_NAME, null, null, null, null, null, null);
            Cursor c = mContext.getContentResolver().query(FavoritesProvider.Movies.CONTENT_URI, null, null, null, null);
            if (c != null) {
                int colTmdbId = c.getColumnIndex(MovieContract.MovieEntry.COL_TMDB_ID);
                int colTitle = c.getColumnIndex(MovieContract.MovieEntry.COL_TITLE);
                int colDesc = c.getColumnIndex(MovieContract.MovieEntry.COL_DESC);
                int colPoster = c.getColumnIndex(MovieContract.MovieEntry.COL_POSTER_PATH);
                int colRelDate = c.getColumnIndex(MovieContract.MovieEntry.COL_REL_DATE);
                int colOgTitle = c.getColumnIndex(MovieContract.MovieEntry.COL_ORIGINAL_TITLE);
                int colBackdrop = c.getColumnIndex(MovieContract.MovieEntry.COL_BACKDROP_PATH);
                int colVoteAvg = c.getColumnIndex(MovieContract.MovieEntry.COL_VOTE_AVG);

                if(c.moveToFirst()) {
                    movieList.add(new Movie(c.getString(colTitle),
                            c.getString(colDesc),
                            c.getString(colPoster),
                            c.getString(colRelDate),
                            c.getLong(colTmdbId),
                            c.getString(colOgTitle),
                            c.getString(colBackdrop),
                            c.getString(colVoteAvg)));
                }

                while(c.moveToNext()) {
                    movieList.add(new Movie(c.getString(colTitle),
                            c.getString(colDesc),
                            c.getString(colPoster),
                            c.getString(colRelDate),
                            c.getLong(colTmdbId),
                            c.getString(colOgTitle),
                            c.getString(colBackdrop),
                            c.getString(colVoteAvg)));
                }

                c.close();
            }
            db.close();

            return movieList;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            if(movies != null  && !movies.isEmpty()) {
                mAdapter.clear();
                mAdapter.addAll(movies);
                mAdapter.notifyDataSetChanged();
            }else {
                final Snackbar s = Snackbar.make(MovieFragment.this.movieGrid, R.string.string_no_favorites, Snackbar.LENGTH_INDEFINITE);
                s.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        s.dismiss();
                    }
                });
                s.show();
            }

        }
    }

}
