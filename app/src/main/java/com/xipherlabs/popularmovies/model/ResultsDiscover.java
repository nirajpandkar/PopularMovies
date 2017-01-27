package com.xipherlabs.popularmovies.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ResultsDiscover {
    @SerializedName("page")
    public String page;

    @SerializedName("results")
    private List<Movie> results;

    public List<Movie> getResults() {
        return results;
    }

    public void setResults(List<Movie> results) {
        this.results = results;
    }}
