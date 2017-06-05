package david.com.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class that shows the selected movie details
 * - movie details are retrieved as a bundle from the intent
 * - movie details are then retrieved as a HashMap from the bundle
 * - AsyncTasks are called at start of the activity to get review & trailer data
 *
 * UI:
 * - LinearLayout used with multiple CardViews
 *
 * STRING LITERALS:
 * - string literals have not been put into the strings.xml file as they are not user-facing
 *
 * ATTRIBUTION:
 * - some code was implemented with help from StackOverflow
 *
 */

public class MovieDetailsActivity extends AppCompatActivity {

    private static final String TAG = MovieDetailsActivity.class.getSimpleName();
    private ScrollView scrollView;
    private ConstraintLayout cl;
    private TextView movieTitle;
    private ImageView moviePoster;
    private TextView userRating;
    private TextView releaseDate;
    private ImageButton mFavStar;
    protected TextView movieSummary;
    private LinearLayout linearLayout;
    private ExpandableTextView expandableTextView;
    private HashMap movieSelected;
    private boolean mIsFavourite;
    private String[] videoKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "entering onCreate");
        setContentView(R.layout.activity_movie_details);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.title_movie_details);

        movieTitle = (TextView) findViewById(R.id.txtMovieTitle);
        userRating = (TextView) findViewById(R.id.txtMovieUserRating);
        releaseDate = (TextView) findViewById(R.id.txtMovieReleaseDate);
        movieSummary = (TextView) findViewById(R.id.txtMovieSummary);
        mFavStar = (ImageButton) findViewById(R.id.imgFavStar);
        linearLayout = (LinearLayout) findViewById(R.id.ll_play_trailer);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTrailer();
            }
        });
        expandableTextView = (ExpandableTextView) findViewById(R.id.expandable_text_view);

        Bundle bundle = this.getIntent().getExtras();
        movieSelected = (HashMap) bundle.getSerializable("selectedMovie");

        if(isNetworkAvailable()){
            loadMovieReview("reviews");
            getTrailerData("videos");
        }else{
            //txtNoNetworkMessage.setVisibility(View.VISIBLE);
        }

        if(mIsFavourite){
            mFavStar.setImageResource(R.drawable.fav_star_on);
            //Toast.makeText(getApplicationContext(), "removed from Favourites", Toast.LENGTH_SHORT).show();
            mIsFavourite = false;
        }else {
            mFavStar.setImageResource(R.drawable.fav_star_off);
            //Toast.makeText(getApplicationContext(), "added to Favourites", Toast.LENGTH_SHORT).show();
            mIsFavourite = true;
        }

        mFavStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickFav(v);
            }
        });
        moviePoster = (ImageView) findViewById(R.id.imgMoviePoster);

        displayMovieDetails(movieSelected);
        Log.d(TAG, "exiting onCreate");
    }

    private void playTrailer() {
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + videoKeys[0]));

        PackageManager packageManager = getPackageManager();
        List activities = packageManager.queryIntentActivities(webIntent, PackageManager.MATCH_DEFAULT_ONLY);
        boolean isIntentSafe = activities.size() > 0;

        if(isIntentSafe){
            startActivity(webIntent);
        }
    }

    private void getTrailerData(String videos) {
        String movieId = (String)(movieSelected.get("id"));
        URL myUrl = NetworkUtils.buildUrl(videos, getApplicationContext(), movieId);
        new MovieDetailsActivity.TheMovieDbTask().execute(myUrl);
    }

    private void loadMovieReview(String reviews) {
        String movieId = (String)(movieSelected.get("id"));
        URL myUrl = NetworkUtils.buildUrl(reviews, getApplicationContext(), movieId);
        new MovieDetailsActivity.TheMovieDbTask().execute(myUrl);
    }

    private boolean isNetworkAvailable(){
        Log.d(TAG, "entering isNetworkAvailable");
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        Log.d(TAG, "exiting isNetworkAvailable");
        return ((activeNetworkInfo != null) && (activeNetworkInfo.isConnected()));
    }

    //TODO view & play trailers (either on youtube app or web browser)

    //TODO mark as favourite, tap button (star) - local movies collection that I will maintain & does not require an API request

    private void displayMovieDetails(HashMap movie) {
        Log.d(TAG, "entering displayMovieDetails");
        StringBuilder movieYear = new StringBuilder((String) movie.get("releaseDate"));
        String year = movieYear.substring(0,4);
        String posterPrefix = getString(R.string.url_poster_prefix);
        movieTitle.setText((String)movie.get("title"));
        movieSummary.setText((String)movie.get("overview"));
        userRating.setText((String)movie.get("voteAverage") + "/10");
        releaseDate.setText(year);
        Picasso.with(getApplicationContext()).load(posterPrefix + (String) movie.get("posterPath")).into(moviePoster);
        Log.d(TAG, "poster path is: " + movie.get("posterPath"));
    }

    //TODO url to pass to youtube to watch trailer: https://www.youtube.com/watch?v= (youtubeKey)

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void clickFav(View view){
        if(mIsFavourite){
            mFavStar.setImageResource(R.drawable.fav_star_on);
            Toast.makeText(getApplicationContext(), "added to Favourites", Toast.LENGTH_SHORT).show();
            mIsFavourite = false;
        }else{
            mFavStar.setImageResource(R.drawable.fav_star_off);
            Toast.makeText(getApplicationContext(), "removed from Favourites", Toast.LENGTH_SHORT).show();
            mIsFavourite = true;
        }
    }

    public class TheMovieDbTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(URL... params) {
            Log.d(TAG, "entering doInBackground");
            URL requestMovieReviewUrl = params[0];
            String theMovieDbResult = null;
            try {
                theMovieDbResult = NetworkUtils.getResponseFromHttpUrl(requestMovieReviewUrl);
                Log.d(TAG, "exiting doInBackground");
                return theMovieDbResult;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "exiting doInBackground after exception");
                return null;
            }
        }

        @Override
        protected void onPostExecute(String theMovieDbSearchResults) {
            Log.d(TAG, "entering onPostExecute");
//            String[] reviews;
//            String[] authors;
            if (theMovieDbSearchResults != null && !theMovieDbSearchResults.equals("")) {
                Log.d(TAG, theMovieDbSearchResults);
                JSONObject jsonObject = JsonUtils.getJSONObject(theMovieDbSearchResults);

                Log.d(TAG, "JSON size is : " + jsonObject.length());
                switch (jsonObject.length()) {
                    case 2:                             //video api result size
                        getVideoData(jsonObject);
                        break;
                    case 5:                             //review api result size
                        getReviewData(jsonObject);
                        break;
                    default:
                        Log.e(TAG, "no match found in onPostExecute() switch, looking for either getVideoData() or getReviewData()");
                        break;
                }
            } else {
                Log.d(TAG, "empty data back from themoviedb api call");
            }
        }

        private void getReviewData(JSONObject reviewObject){
            String[] reviews;
            String[] authors;
            JSONArray jsonMovieReviews = JsonUtils.getJSONArray(reviewObject, "results");

            reviews = new String[jsonMovieReviews.length()];
            authors = new String[jsonMovieReviews.length()];

            int nextReview = 0;
            for(String review : reviews){
                JSONObject reviewDetails = JsonUtils.getJSONObject(jsonMovieReviews, nextReview);
                reviews[nextReview] = JsonUtils.getString(reviewDetails, "content");
                authors[nextReview] = JsonUtils.getString(reviewDetails, "author");
                ++nextReview;
            }

            for(int i = 0; i < reviews.length; ++i){
                expandableTextView.setText(expandableTextView.getText() + authors[i] + ":" + "\n\"" + reviews[i] + "\"\n\n          -----------------------------------------------\n\n");
            }
        }

        private void getVideoData(JSONObject videoObject){
            JSONArray jsonMovieVideos = JsonUtils.getJSONArray(videoObject, "results");

            videoKeys = new String[jsonMovieVideos.length()];

            int nextTrailer = 0;
            for(String trailer : videoKeys){
                JSONObject reviewDetails = JsonUtils.getJSONObject(jsonMovieVideos, nextTrailer);
                videoKeys[nextTrailer] = JsonUtils.getString(reviewDetails, "key");
                ++nextTrailer;
            }

            for(int i = 0; i < videoKeys.length; ++i){
                //expandableTextView.setText(expandableTextView.getText() + authors[i] + ":" + "\n\"" + reviews[i] + "\"\n\n          -----------------------------------------------\n\n");
                Log.d(TAG, "key is: " + videoKeys[i]);
            }
        }
    }
}
