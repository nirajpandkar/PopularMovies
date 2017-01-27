package com.xipherlabs.popularmovies.rest;
import com.xipherlabs.popularmovies.model.ResultsDiscover;
import com.xipherlabs.popularmovies.model.ResultsVideo;
import com.xipherlabs.popularmovies.model.ReviewResults;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieService {

    @GET("discover/movie")
    Call<ResultsDiscover> getPopularMovies(@Query("api_key") String apiKey, @Query("sort_by") String sortBy, @Query("vote_count.gte") String minVotes);

    @GET("movie/{id}/videos")
    Call<ResultsVideo> getVideosForMovie(@Path("id") long id, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<ReviewResults> getReviewsForMovie(@Path("id") long id, @Query("api_key") String apiKey);
}
