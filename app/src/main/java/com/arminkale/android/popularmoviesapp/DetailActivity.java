package com.arminkale.android.popularmoviesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get movie object passed in as extra.
        Movie movie = (Movie)getIntent().getSerializableExtra(Intent.EXTRA_INTENT);

        // Set title
        TextView textViewTitle = (TextView)findViewById(R.id.text_view_title);
        textViewTitle.setText(movie.OriginalTitle);

        // Set poster
        ImageView imageViewPoster = (ImageView)findViewById(R.id.image_view_poster);
        Picasso.with(this).load(movie.getPosterPath()).into(imageViewPoster);

        // Set release date
        TextView textViewReleaseDate = (TextView)findViewById(R.id.text_view_release_date);
        textViewReleaseDate.setText(movie.ReleaseDate);

        // Set user rating
        TextView textViewUserRating = (TextView)findViewById(R.id.text_view_user_rating);
        textViewUserRating.setText(movie.UserRating + "/10");

        // Set synopsis
        TextView textViewSynopsis = (TextView)findViewById(R.id.text_view_synopsis);
        textViewSynopsis.setText(movie.Synopsis);
    }

}
