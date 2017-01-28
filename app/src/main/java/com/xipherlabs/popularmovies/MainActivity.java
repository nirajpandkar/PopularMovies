package com.xipherlabs.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    private boolean mTwoPane = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(findViewById(R.id.detailfrag_container) != null) {
            mTwoPane = true;
        }
        if (savedInstanceState == null) {
            MovieFragment movieFragment = MovieFragment.newInstance(mTwoPane);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, movieFragment, "fragment_movie").commit();
        }
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }
}