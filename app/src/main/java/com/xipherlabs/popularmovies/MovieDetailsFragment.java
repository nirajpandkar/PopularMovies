package com.xipherlabs.popularmovies;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MovieDetailsFragment extends Fragment {

    private static final String ARG_MOVIE = "movie";
    public static final String BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780";
    private Movie mMovie;


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

        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);

        ImageView poster = (ImageView) view.findViewById(R.id.poster);
        ImageView backdrop = (ImageView) view.findViewById(R.id.backdrop);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView releaseDate = (TextView) view.findViewById(R.id.relDate);
        TextView overview = (TextView) view.findViewById(R.id.overview);
        TextView rating = (TextView) view.findViewById(R.id.rating);

        Picasso.with(getContext()).load(MovieAdapter.IMAGE_BASE_URL + mMovie.getPosterPath()).into(poster);
        Picasso.with(getContext()).load(BACKDROP_BASE_URL + mMovie.getBackdropPath()).into(backdrop);  // TODO: investigate if it takes long to load

        backdrop.setColorFilter(Color.parseColor("#B6000000"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date date = sdf.parse(mMovie.getReleaseDate(), new ParsePosition(0));
        title.setText(mMovie.getOriginalTitle());
        releaseDate.setText(DateFormat.format("MMMM dd, yyyy", date));
        overview.setText(mMovie.getDescription());
        rating.setText(String.format(Locale.US, "%2.1f / 10", Double.parseDouble(mMovie.getVoteAvg())));

        return view;
    }

}