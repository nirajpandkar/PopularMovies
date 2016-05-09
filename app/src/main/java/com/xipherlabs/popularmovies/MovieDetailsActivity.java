package com.xipherlabs.popularmovies;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MovieDetailsActivity extends AppCompatActivity {
    public static final String ARG_MOVIE = "movie";
    public static final String FRAGMENT = "movie_detail_fragment";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MovieDetailsFragment movieDetailsFragment = MovieDetailsFragment.newInstance((Movie) getIntent().getExtras().getSerializable(ARG_MOVIE));
        getSupportFragmentManager().beginTransaction().replace(R.id.movie_details,movieDetailsFragment, FRAGMENT).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            ActivityCompat.finishAfterTransition(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}