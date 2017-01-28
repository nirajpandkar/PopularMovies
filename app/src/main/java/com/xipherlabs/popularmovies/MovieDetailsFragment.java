package com.xipherlabs.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.xipherlabs.popularmovies.db.FavoritesProvider;
import com.xipherlabs.popularmovies.db.MovieContract;
import com.xipherlabs.popularmovies.model.Movie;
import com.xipherlabs.popularmovies.model.ResultsVideo;
import com.xipherlabs.popularmovies.model.Review;
import com.xipherlabs.popularmovies.model.ResultsReview;
import com.xipherlabs.popularmovies.model.Video;
import com.xipherlabs.popularmovies.rest.MovieService;

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MovieDetailsFragment extends Fragment {

    private static final String ARG_MOVIE = "movie";
    public static final String BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780";
    private Movie mMovie;
    private MenuItem star;
    private MenuItem share;
    private boolean favorite = false;
    boolean trailerSharePrepared = false;
    private Uri trailerUri;
    boolean offlineMode = false;
    private View viewParent;
    @BindView(R.id.thumbnail)
    ImageView poster;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.backdrop)
    ImageView backdrop;
    @BindView(R.id.relDate)
    TextView releaseDate;
    @BindView(R.id.overview)
    TextView overview;
    @BindView(R.id.rating)
    TextView rating;
    @BindView(R.id.trailerView)
    LinearLayout trailerView;
    @BindView(R.id.reviewView)
    LinearLayout reviewView;

    public MovieDetailsFragment() { }

    public static MovieDetailsFragment newInstance(Movie movie) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MOVIE, movie);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMovie = (Movie) getArguments().getSerializable(ARG_MOVIE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        trailerSharePrepared = false;
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);
        viewParent = view;
        ButterKnife.bind(this, view);

        Picasso.with(getContext()).load(MovieAdapter.IMAGE_BASE_URL + mMovie.getPosterPath()).into(poster);
        Picasso.with(getContext()).load(BACKDROP_BASE_URL + mMovie.getBackdropPath()).into(backdrop);
        backdrop.setColorFilter(Color.parseColor("#B6000000"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date date = sdf.parse(mMovie.getReleaseDate(), new ParsePosition(0));
        title.setText(mMovie.getOriginalTitle());
        releaseDate.setText(DateFormat.format("MMMM dd, yyyy", date));
        overview.setText(mMovie.getDescription());
        rating.setText(String.format(Locale.US, "%2.1f / 10", Double.parseDouble(mMovie.getVoteAvg())));

        NetworkInfo networkInfo = ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected()) {
            new FetchVideoTask(getContext()).execute(mMovie.getId());
            new FetchReviewTask(getContext()).execute(mMovie.getId());
        }else{
            offlineMode = true;
            Snackbar.make(container, "Offline Mode", Snackbar.LENGTH_LONG).show();
            reviewView.setVisibility(View.GONE);
            trailerView.setVisibility(View.GONE);
            view.findViewById(R.id.trailerTitle).setVisibility(View.GONE);
            view.findViewById(R.id.reviewTitle).setVisibility(View.GONE);
        }
        //added1
       /* MovieDbHelper dbHelper = new MovieDbHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();*/

        Cursor c = getContext().getContentResolver().query(FavoritesProvider.Movies.CONTENT_URI, new String[]{MovieContract.MovieEntry.COL_TMDB_ID}, MovieContract.MovieEntry.COL_TMDB_ID + "=?", new String[]{Long.toString(mMovie.getId())}, null);

        if(c != null && c.moveToFirst()) {
            favorite = true;
        }
        if(c != null) c.close();

        return view;
    }
    //added2
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_movie_detail, menu);
        star = menu.findItem(R.id.favorite);
        if(favorite) {
            if(star != null) {
                star.setIcon(R.drawable.ic_favorite_black);
            }
        }
        share = menu.findItem(R.id.share);
        if(share != null && offlineMode) {
            share.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.favorite) {
            item.setIcon(R.drawable.ic_favorite_black);
            if(!favorite) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MovieContract.MovieEntry.COL_TMDB_ID, mMovie.getId());
                contentValues.put(MovieContract.MovieEntry.COL_TITLE, mMovie.getTitle());
                contentValues.put(MovieContract.MovieEntry.COL_DESC, mMovie.getDescription());
                contentValues.put(MovieContract.MovieEntry.COL_POSTER_PATH, mMovie.getPosterPath());
                contentValues.put(MovieContract.MovieEntry.COL_REL_DATE, mMovie.getReleaseDate());
                contentValues.put(MovieContract.MovieEntry.COL_ORIGINAL_TITLE, mMovie.getOriginalTitle());
                contentValues.put(MovieContract.MovieEntry.COL_BACKDROP_PATH, mMovie.getBackdropPath());
                contentValues.put(MovieContract.MovieEntry.COL_VOTE_AVG, mMovie.getVoteAvg());
                Uri uri = getContext().getContentResolver()
                        .insert(FavoritesProvider.Movies.CONTENT_URI, contentValues);
                favorite = true;
            } else {
                favorite = false;
                item.setIcon(R.drawable.ic_favorite_border_black);
                int n = getContext().getContentResolver()
                        .delete(FavoritesProvider.Movies.CONTENT_URI
                                , MovieContract.MovieEntry.COL_TMDB_ID + "=?"
                                , new String[]{Long.toString(mMovie.getId())});
            }
            return true;
        }
        if (item.getItemId() == R.id.share) {
            if (trailerSharePrepared) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, trailerUri.toString());
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, getString(R.string.string_share_trailer)));
            } else {
                Snackbar.make(viewParent
                        , getContext().getString(R.string.string_trailer_not_loaded)
                        , Snackbar.LENGTH_SHORT).show();
            }
        }

        return false;
    }
    //TODO: Have a look at ReactiveX
    private class FetchVideoTask extends AsyncTask<Long, Void, List<Video>> {
        //Reference: http://stackoverflow.com/a/8842839/2663152
        public static final String YT_THUMB_BASE = "http://img.youtube.com/vi/%s/0.jpg";
        public static final String YT_VIDEO_BASE = "http://www.youtube.com/watch?v=";

        private Context mContext;
        boolean networkError = false;
        public FetchVideoTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected List<Video> doInBackground(Long... params) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(MovieFragment.FetchDataTask.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            MovieService movieService = retrofit.create(MovieService.class);
            Call<ResultsVideo> call = movieService.getVideosForMovie(params[0], mContext.getString(R.string.api));

            try {
                Response<ResultsVideo> response = call.execute();
                return response.body().getVideos();
            } catch (IOException e) {
                networkError = true;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final List<Video> videos) {
            super.onPostExecute(videos);
            if (videos != null && videos.size() != 0) {
                trailerUri = Uri.parse(YT_VIDEO_BASE + videos.get(0).getKey());
                trailerSharePrepared = true;
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(YT_VIDEO_BASE + videos.get((Integer) v.getTag()).getKey()));
                        startActivity(i);
                    }
                };
                for (int i = 0; i < videos.size(); i++) {
                    if (i >= 2) {
                        ImageView imageView = new ImageView(mContext);
                        imageView.setLayoutParams(trailerView.getChildAt(0).getLayoutParams());
                        trailerView.addView(imageView);
                    }

                    Picasso.with(mContext).load(Uri.parse(String.format(YT_THUMB_BASE, videos.get(i).getKey()))).into((ImageView) trailerView.getChildAt(i));
                    trailerView.getChildAt(i).setTag(i);
                    trailerView.getChildAt(i).setOnClickListener(onClickListener);
                }
                if (videos.size() < 2) {
                    trailerView.getChildAt(1).setVisibility(View.GONE);
                }

            }else{
                if(networkError) {
                 Snackbar.make(viewParent, "Network Error", Snackbar.LENGTH_INDEFINITE).show();
                }
            }
        }
    }


    public class FetchReviewTask extends AsyncTask<Long, Void, List<Review>> {

        private Context mContext;
        boolean networkError = false;

        public FetchReviewTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected List<Review> doInBackground(Long... params) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(MovieFragment.FetchDataTask.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            MovieService movieService = retrofit.create(MovieService.class);

            Call<ResultsReview> call = movieService.getReviewsForMovie(mMovie.getId(), mContext.getString(R.string.api));

            try {
                Response<ResultsReview> response = call.execute();
                return response.body().getResults();
            } catch (IOException e) {
                networkError = true;
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(final List<Review> reviews) {
            super.onPostExecute(reviews);



            if (reviews != null) {
                if(reviews.size() == 0) {
                    ((TextView) reviewView.getChildAt(0)).setText(R.string.string_no_reviews);
                    return;
                }

                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(reviews.get((Integer) v.getTag()).getUrl()));
                        startActivity(i);
                    }
                };

                for(int i = 0; i < reviews.size(); i++) {
                    if(i >= 1) {
                        TextView v = new TextView(mContext);
                        v.setLayoutParams(reviewView.getChildAt(0).getLayoutParams());
                        v.setTextColor(mContext.getResources().getColor(android.R.color.white));
                        v.setMaxLines(20);
                        v.setEllipsize(TextUtils.TruncateAt.END);
                        TypedValue outValue = new TypedValue();
                        mContext.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
                        v.setBackgroundResource(outValue.resourceId);
                        reviewView.addView(v);
                    }
                    String s1 = (reviews.get(i).getContent().length() > 400 ? reviews.get(i).getContent().substring(0, 400) : reviews.get(i).getContent());
                    ((TextView) reviewView.getChildAt(i)).setText(String.format(mContext.getString(R.string.string_review_format), reviews.get(i).getAuthor(), s1));
                    reviewView.getChildAt(i).setTag(i);
                    reviewView.getChildAt(i).setOnClickListener(onClickListener);
                }
            } else {
                if(networkError) {
                    ((TextView) reviewView.getChildAt(0)).setText(R.string.string_review_network_error);
                } else {
                    ((TextView) reviewView.getChildAt(0)).setText(R.string.string_no_reviews);
                }
            }
        }
    }


}