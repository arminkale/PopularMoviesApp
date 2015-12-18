package com.arminkale.android.popularmoviesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private MovieAdapter mMovieAdapter = null;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //Create array adapter with the grid view
        mMovieAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());
        //Find grid view
        GridView gridView = (GridView) rootView.findViewById(R.id.grid_view_main);
        //Set the array adapter as the grid view adapter
        gridView.setAdapter(mMovieAdapter);
        //Add the OnItemClickListener
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = mMovieAdapter.getItem(position);
                // Executed in an Activity, so 'this' is the Context
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_INTENT, movie);
                startActivity(detailIntent);
            }
        });

        return rootView;
    }

    private void UpdateMovies() {
        FetchMovieTask movieTask = new FetchMovieTask();
        movieTask.execute();
    }

    @Override
    public void onStart(){
        super.onStart();
        UpdateMovies();
    }

    public class FetchMovieTask extends AsyncTask<String, Void, List<Movie>> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private List<Movie> getMovieDataFromJson(String movieJsonStr)
                throws JSONException {
            // These are the names of the JSON objects that need to be extracted.
            final String JSON_RESULTS = "results";
            final String JSON_ID = "id";
            final String JSON_ORIGINAL_TITLE = "original_title";
            final String JSON_SYNOPSIS = "overview";
            final String JSON_RELEASE_DATE = "release_date";
            final String JSON_POSTER = "poster_path";
            final String JSON_POPULARITY = "popularity";
            final String JSON_USER_RATING = "vote_average";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(JSON_RESULTS);

            // TMDB currently returns 20 movies based upon the the popularity or user rating that
            // is being asked for. The returned data is also sorted.
            List<Movie> movieResults = new ArrayList<Movie>(movieArray.length());
            for(int i = 0; i < movieArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                //Movie movie = new Movie();
                int id;
                String originalTitle;
                String synopsis;
                String releaseDate;
                String relativePosterPath;
                Double popularity;
                Double userRating;

                // Get the JSON object representing the day
                JSONObject json_movie = movieArray.getJSONObject(i);

                // Get the child objects representing the movie.
                id = json_movie.getInt(JSON_ID);
                originalTitle = json_movie.getString(JSON_ORIGINAL_TITLE);
                synopsis = json_movie.getString(JSON_SYNOPSIS);
                releaseDate = json_movie.getString(JSON_RELEASE_DATE);
                relativePosterPath = json_movie.getString(JSON_POSTER);
                popularity = json_movie.getDouble(JSON_POPULARITY);
                userRating = json_movie.getDouble(JSON_USER_RATING);

                Movie movie = new Movie(
                        id,
                        originalTitle,
                        synopsis,
                        releaseDate,
                        relativePosterPath,
                        popularity,
                        userRating);

                movieResults.add(movie);
            }

            return movieResults;
        }

        @Override
        protected List<Movie> doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String responseJsonString = null;

            try {
                final String THEMOVIEDB_URL_DISCOVER = "http://api.themoviedb.org/3/discover/movie";
                final String SORT_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";
                String sortParamValue = "popularity.desc";

                // Movie data is sorted by popularity by default.
                // If user prefers to sort by rating, pass the correct
                // sort parameter in the API call below.
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String unitType = sharedPrefs.getString(
                        getString(R.string.pref_sort_order_key),
                        getString(R.string.pref_sort_order_most_popular));

                if (unitType.equals(getString(R.string.pref_sort_order_highest_rated))){
                    sortParamValue = "vote_average.desc";
                } else if (!unitType.equals(getString(R.string.pref_sort_order_most_popular))){
                    Log.d(LOG_TAG, "Sort order unit type not found: " + unitType);
                }

                Uri builtUri = Uri.parse(THEMOVIEDB_URL_DISCOVER).buildUpon()
                        .appendQueryParameter(SORT_PARAM, sortParamValue)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THEMOVIEDB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    Log.d(LOG_TAG, "Input stream is empty!");
                    // Nothing to do.
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    Log.d(LOG_TAG, "Returned buffer is empty!");
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                responseJsonString = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in
                // attempting to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(responseJsonString);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return  null;
        }

        @Override
        protected void onPostExecute(List<Movie> result) {
            if (result != null) {
                mMovieAdapter.clear();
                for(Movie movie : result) {
                    mMovieAdapter.add(movie);
                }
                // New data is back from the server. Hooray!
            }
        }
    }
}
