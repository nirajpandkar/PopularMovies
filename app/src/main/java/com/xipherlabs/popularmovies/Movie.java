package com.xipherlabs.popularmovies;

import java.io.Serializable;

// TODO: Look at Parcelable vs Serializable & Update if need be.

public class Movie implements Serializable{
    private String title,originalTitle,description,posterPath,backdropPath,releaseDate,voteAvg;
    private long id;

    public Movie(){}

    public Movie(String title, String originalTitle, String description, String posterPath,String backdropPath, String releaseDate, String voteAvg, long id) {
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
