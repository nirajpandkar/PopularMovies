package com.xipherlabs.popularmovies.model;

import java.util.List;

public class ResultsReview {

    int id;
    int page;
    List<Review> results;

    public ResultsReview() {}

    public ResultsReview(int id, int page, List<Review> results) {
        this.id = id;
        this.page = page;
        this.results = results;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<Review> getResults() {
        return results;
    }

    public void setResults(List<Review> results) {
        this.results = results;
    }
}