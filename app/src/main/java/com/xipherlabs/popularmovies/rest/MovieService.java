package com.xipherlabs.popularmovies.rest;
import com.xipherlabs.popularmovies.model.ResultsMovie;
import com.xipherlabs.popularmovies.model.ResultsVideo;
import com.xipherlabs.popularmovies.model.ResultsReview;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieService {

    @GET("discover/movie")
    Call<ResultsMovie> getPopularMovies(@Query("api_key") String apiKey, @Query("sort_by") String sortBy, @Query("vote_count.gte") String minVotes);

    @GET("movie/{id}/videos")
    Call<ResultsVideo> getVideosForMovie(@Path("id") long id, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<ResultsReview> getReviewsForMovie(@Path("id") long id, @Query("api_key") String apiKey);
}
