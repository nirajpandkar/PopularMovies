package com.xipherlabs.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MovieFragment extends Fragment {

    MovieAdapter mAdapter;
    GridView movieGrid;
    public MovieFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie, container, false);

        FetchDataTask dataTask = new FetchDataTask(getContext());
        dataTask.execute(FetchDataTask.SORT_BY_POPULARITY);
        movieGrid = (GridView) view.findViewById(R.id.poster_grid);
        mAdapter = new MovieAdapter(getContext(), new ArrayList<Movie>());
        movieGrid.setAdapter(mAdapter);

        movieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), MovieDetailsActivity.class);intent.putExtra(MovieDetailsActivity.ARG_MOVIE, (Movie) parent.getItemAtPosition(position));
                startActivity(intent);
            }
        });
        return view;
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

    public class FetchDataTask extends AsyncTask<String, Void, Movie[]> {

        public static final String BASE_URL = "https://api.themoviedb.org/3/discover/movie?";

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

        FetchDataTask(Context context) {
            mContext = context;
        }

        @Override
        protected Movie[] doInBackground(String... sortBy) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String responseJson;
            try {
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
            }

            return null;
        }

        private Movie[] parseJson(String jsonString) throws JSONException {
            JSONObject moviesObj = new JSONObject(jsonString);
            JSONArray results = moviesObj.getJSONArray("results");

            Movie[] data = new Movie[results.length()];

            for(int i = 0; i < results.length(); i++) {
                JSONObject movieData = results.getJSONObject(i);
                data[i] = new Movie(movieData.getString(KEY_TITLE),
                        movieData.getString(KEY_ORIGINAL_TITLE),
                        movieData.getString(KEY_DESCRIPTION),
                        movieData.getString(KEY_POSTER_PATH),
                        movieData.getString(KEY_BACKDROP_PATH),
                        movieData.getString(KEY_RELEASE_DATE),
                        movieData.getString(KEY_VOTE_AVG),
                        movieData.getLong(KEY_ID));
            }

            return data;
        }

        @Override
        protected void onPostExecute(Movie[] movies) {
           super.onPostExecute(movies);
            mAdapter.clear();
            for(Movie movie: movies) {
                mAdapter.add(movie);
            }
        }
    }

}
