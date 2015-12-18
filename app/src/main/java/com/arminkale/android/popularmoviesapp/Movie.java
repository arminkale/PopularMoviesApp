package com.arminkale.android.popularmoviesapp;

import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;

/**
 * Created by arminkale on 12/6/15.
 */
public class Movie implements Serializable {
    private static final String LOG_TAG = Movie.class.getSimpleName();

    int Id;
    String OriginalTitle;
    String Synopsis;
    String ReleaseDate;
    private String BasePath = "http://image.tmdb.org/t/p/w500/";
    private String PosterPath;
    Double Popularity;
    Double UserRating;

    Movie(int id,
          String originalTitle,
          String synopsis,
          String releaseDate,
          String relativePosterPath,
          Double popularity,
          Double userRating)
    {
        Id = id;
        OriginalTitle = originalTitle;
        Synopsis = synopsis;
        ReleaseDate = releaseDate;
        PosterPath = relativePosterPath;
        Popularity = popularity;
        UserRating = userRating;
    }

    public String getPosterPath() {
        return BasePath + PosterPath;
    }
}
