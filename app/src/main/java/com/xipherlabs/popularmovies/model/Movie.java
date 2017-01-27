package com.xipherlabs.popularmovies.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Movie implements Serializable{

    @SerializedName("id")
    private long id;
    @SerializedName("title")
    private String title;
    @SerializedName("overview")
    private String description;
    @SerializedName("vote_average")
    private String voteAvg;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("release_date")
    private String releaseDate;
    @SerializedName("original_title")
    private String originalTitle;
    @SerializedName("backdrop_path")
    private String backdropPath;

    public Movie(){}

    public Movie(String title, String description, String posterPath, String releaseDate, long id, String originalTitle, String backdropPath, String voteAvg) {
        this.title = title;
        this.originalTitle = originalTitle;
        this.description = description;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.releaseDate = releaseDate;
        this.id = id;
        this.voteAvg = voteAvg;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public String getVoteAvg() {
        return voteAvg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
