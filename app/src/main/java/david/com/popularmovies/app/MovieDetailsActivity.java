package david.com.popularmovies.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import david.com.popularmovies.R;
import david.com.popularmovies.adapters.ExpandableListAdapter;
import david.com.popularmovies.db.FavMoviesContract;
import david.com.popularmovies.db.FavMoviesDbHelper;
import david.com.popularmovies.utils.JsonUtils;
import david.com.popularmovies.utils.NetworkUtils;

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

public class MovieDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{

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
    private boolean mIsFavourite;        //TODO bug - need to perform check in onCreate to see if movie coming in is FAV or not
    private String[] videoKeys;
    private String[] reviews;
    private SQLiteDatabase mDb;
    private Bundle bundle;

    private ExpandableListView listTrailerView;
    private ExpandableListAdapter listTrailerAdapter;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listHash;
    private List<String> trailerList;

    private static final int THE_MOVIE_DB_REVIEW_LOADER = 60;
    private static final int THE_MOVIE_DB_TRAILER_LOADER = 61;

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
        expandableTextView = (ExpandableTextView) findViewById(R.id.expandable_text_view);

        listTrailerView = (ExpandableListView) findViewById(R.id.expLV);

        bundle = this.getIntent().getExtras();
        movieSelected = (HashMap) bundle.getSerializable("selectedMovie");
        mIsFavourite = bundle.getBoolean("isFav");

        if(isNetworkAvailable() && !mIsFavourite){
            Log.d("TAG --- +++", "newtowrk is available & movie is not a FAV ");
            loadMovieReview("reviews");
            getTrailerData("videos");
        }else{
            //TODO txtNoNetworkMessage.setVisibility(View.VISIBLE);
            Log.d("TAG --- +++", "MOVIE IS FAV");
            loadLocalMovieData();
        }

        if(mIsFavourite){
            mFavStar.setImageResource(R.drawable.fav_star_on);
            //mIsFavourite = false;
        }else {
            mFavStar.setImageResource(R.drawable.fav_star_off);
            //mIsFavourite = true;
        }

        mFavStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickFav(v);
            }
        });
        moviePoster = (ImageView) findViewById(R.id.imgMoviePoster);

        FavMoviesDbHelper dbHelper = new FavMoviesDbHelper(this);
        mDb = dbHelper.getWritableDatabase();

        //Cursor cursor = mDb.query(FavMoviesContract.FavMovieEntry.TABLE_NAME, null, null, null, null, null, null);
//        Log.e("get DB column count", cursor.getColumnCount()+"");
//        Log.e("get DB count", cursor.getCount()+""); TODO remove debugging

        displayMovieDetails(movieSelected);
        Log.d(TAG, "exiting onCreate");
    }



    private void playTrailer(String trailerId) {
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + trailerId));

        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailerId));

//        PackageManager packageManager = getPackageManager();
//        List activities = packageManager.queryIntentActivities(webIntent, PackageManager.MATCH_DEFAULT_ONLY);
//        boolean isIntentSafe = activities.size() > 0;

        PackageManager packageManager = getPackageManager();
        List activities = packageManager.queryIntentActivities(appIntent, PackageManager.MATCH_DEFAULT_ONLY);
        boolean isIntentSafe = activities.size() > 0;

        if(isIntentSafe){
            startActivity(appIntent);
        }
    }

    private void getTrailerData(String videos) {
        String movieId = (String)(movieSelected.get("id"));
        URL myUrl = NetworkUtils.buildUrl(videos, getApplicationContext(), movieId);

        Bundle queryBundle = new Bundle();
        queryBundle.putString("theMovieDbTrailerQuery", myUrl.toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> theMovieDbLoader = loaderManager.getLoader(THE_MOVIE_DB_TRAILER_LOADER);

        loaderManager.initLoader(THE_MOVIE_DB_TRAILER_LOADER, queryBundle, this).forceLoad();
    }

    private void loadMovieReview(String reviews) {
        String movieId = (String)(movieSelected.get("id"));
        URL myUrl = NetworkUtils.buildUrl(reviews, getApplicationContext(), movieId);

        Bundle queryBundle = new Bundle();
        queryBundle.putString("theMovieDbReviewQuery", myUrl.toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        //Loader<String> theMovieDbLoader = loaderManager.getLoader(THE_MOVIE_DB_REVIEW_LOADER);

        loaderManager.initLoader(THE_MOVIE_DB_REVIEW_LOADER, queryBundle, this).forceLoad();
    }

    private void loadLocalMovieData() {
        Log.d("TAG --- +++", "in loadLocalMovieData() ");
        String review = (String) movieSelected.get("review");
        Log.d("TAG --- +++", "review data is: " + review);
        expandableTextView.setText(review);

        //try to load local trailer data
        String trailerKey = (String) movieSelected.get("trailer");
        trailerList = new ArrayList<>();
        trailerList.add(trailerKey);
//        for(String trailer : videoKeys){    //TODO change to for loop
//            JSONObject reviewDetails = JsonUtils.getJSONObject(jsonMovieVideos, nextTrailer);
//            videoKeys[nextTrailer] = JsonUtils.getString(reviewDetails, "key");
//
//            trailerList.add("Trailer " + nextTrailer);
//
//            ++nextTrailer;
//        }

        Log.d(TAG, "debugging trailer list ======= items are:" + Arrays.toString(trailerList.toArray()));


        for(int i = 0; i < trailerList.size(); ++i){
            Log.d(TAG, "key is: " + trailerList.get(i));
        }
        //if(trailerList.size() > 1) addExtraTrailerViewsIfNeeded();
        addExtraTrailerViewsIfNeeded();
    }

    private boolean isNetworkAvailable(){
        Log.d(TAG, "entering isNetworkAvailable");
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        Log.d(TAG, "exiting isNetworkAvailable");
        return ((activeNetworkInfo != null) && (activeNetworkInfo.isConnected()));
    }
    //TODO mark as favourite, tap button (star) - local movies collection that I will maintain & does not require an API request

    private void displayMovieDetails(HashMap movie) {
        Log.d(TAG, "entering displayMovieDetails");
        StringBuilder movieYear = new StringBuilder((String) movie.get("releaseDate"));
        String year = movieYear.substring(0,4);
        String posterPrefix = getString(R.string.url_poster_prefix);
        movieTitle.setText((String)movie.get("title"));
        movieSummary.setText((String)movie.get("overview"));
        //TODO add review here - use movie.get("review");
        userRating.setText((String)movie.get("voteAverage") + "/10");
        releaseDate.setText(year);
        if(!mIsFavourite){
            Picasso.with(getApplicationContext()).load(posterPrefix + (String) movie.get("posterPath")).into(moviePoster);
        }else{
            moviePoster.setPadding(24, 224, 24, 24);
            moviePoster.setImageResource(R.mipmap.movie_projector);
        }

        Log.d(TAG, "poster path is: " + movie.get("posterPath"));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void clickFav(View view){
        if(mIsFavourite){
            mFavStar.setImageResource(R.drawable.fav_star_off);
            Toast.makeText(getApplicationContext(), "removed from Favourites", Toast.LENGTH_SHORT).show();
            mIsFavourite = false;
        }else{
            mFavStar.setImageResource(R.drawable.fav_star_on);
            addMovieToFav();
            Toast.makeText(getApplicationContext(), "added to Favourites", Toast.LENGTH_SHORT).show();
            mIsFavourite = true;
        }
    }

    private void addExtraTrailerViewsIfNeeded() {
        Log.d(TAG, "entering addExtraTrailerViewsIfNeeded");
        listDataHeader = new ArrayList<>();
        listDataHeader.add("Trailers");
        listHash = new HashMap<>();

        Log.d(TAG, "debugging trailer list in add extraTrailerViews ======= items in list is now:" + Arrays.toString(trailerList.toArray()));
        listHash.put(listDataHeader.get(0), trailerList);
        Log.d(TAG, "list data header index 0 is:" + listDataHeader.get(0).toString());
        Log.d(TAG, "1st item in list hash is:" + listHash.get(listDataHeader.get(0)).toString());
        Log.d(TAG, "xxxxxxxxxx listhash size : " + listHash.size());
        //Log.d(TAG, "xxxxxxxxxx debugging trailer list in hashmap ======= items in list is now:" + Arrays.toString(listHash.get(0).toArray()));
        listTrailerAdapter = new ExpandableListAdapter(this, listDataHeader, listHash);
        listTrailerView.setAdapter(listTrailerAdapter);
        listTrailerView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.d(TAG, "clicked trailer, position: " + childPosition + " video key is: " + videoKeys[childPosition]);
                playTrailer(videoKeys[childPosition]);
                return false;
            }
        });
        listTrailerView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                setListViewHeight(parent, groupPosition);
                return false;
            }
        });
    }

    private void setListViewHeight(ExpandableListView parent, int groupPosition) {
        ExpandableListAdapter listAdapter = (ExpandableListAdapter) listTrailerView.getExpandableListAdapter();
        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listTrailerView.getWidth(),
                View.MeasureSpec.EXACTLY);
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View groupItem = listAdapter.getGroupView(i, false, null, listTrailerView);
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

            totalHeight += groupItem.getMeasuredHeight();

            if (((listTrailerView.isGroupExpanded(i)) && (i != groupPosition))
                    || ((!listTrailerView.isGroupExpanded(i)) && (i == groupPosition))) {
                for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
                    View listItem = listAdapter.getChildView(i, j, false, null,
                            listTrailerView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

                    totalHeight += listItem.getMeasuredHeight();

                }
            }
        }

        ViewGroup.LayoutParams params = listTrailerView.getLayoutParams();
        int height = totalHeight
                + (listTrailerView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
        if (height < 10)
            height = 200;
        params.height = height;
        listTrailerView.setLayoutParams(params);
        listTrailerView.requestLayout();
    }

    private void addMovieToFav(){
        //TODO add all movie values into new movie object
        ContentValues contentValues = new ContentValues();
        contentValues.put(FavMoviesContract.FavMovieEntry.COLUMN_TITLE, (String) movieSelected.get("title"));
        contentValues.put(FavMoviesContract.FavMovieEntry.COLUMN_RATING, (String) movieSelected.get("voteAverage"));
        contentValues.put(FavMoviesContract.FavMovieEntry.COLUMN_YEAR, (String) movieSelected.get("releaseDate"));
        contentValues.put(FavMoviesContract.FavMovieEntry.COLUMN_SUMMARY, (String) movieSelected.get("overview"));
        contentValues.put(FavMoviesContract.FavMovieEntry.COLUMN_TRAILER, videoKeys[0]);                //TODO defect, videoKeys[] can be null if no trailers, need check...
        contentValues.put(FavMoviesContract.FavMovieEntry.COLUMN_REVIEW, reviews[0]);                   //TODO defect, reviews[] can be null if no reviews, need check...

        Uri uri = getContentResolver().insert(FavMoviesContract.FavMovieEntry.CONTENT_URI, contentValues);

        if(uri != null){
            Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
        }

        //return mDb.insert(FavMoviesContract.FavMovieEntry.TABLE_NAME, null, contentValues);

    }

    @Override
    public Loader<String> onCreateLoader(final int id, final Bundle args) {
        Log.d(TAG, "entering **** new  onCreateLoader *** & ID is:" + id);
        return new AsyncTaskLoader<String>(this) {
            @Override
            public String loadInBackground() {
                String theMovieDbReviewQueryString = args.getString("theMovieDbReviewQuery");
                String theMovieDbTrailerQueryString = args.getString("theMovieDbTrailerQuery");
                if(id == 60){
                     return processReviewQueryData(theMovieDbReviewQueryString);
                }else if(id == 61){
                    return processTrailerQueryData(theMovieDbTrailerQueryString);
                }
                return null;
            }

            private String processTrailerQueryData(String theMovieDbTrailerQueryString) {
                Log.d(TAG, "61 found in loadInBackground");
                if(theMovieDbTrailerQueryString == null || TextUtils.isEmpty(theMovieDbTrailerQueryString)){
                    return null;
                }

                try {
                    Log.d(TAG, "in TRY 61");
                    URL theMovieDbTrailerUrl = new URL(theMovieDbTrailerQueryString);
                    return NetworkUtils.getResponseFromHttpUrl(theMovieDbTrailerUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "exiting loadInBackground after exception");
                    return null;
                }
            }

            private String processReviewQueryData(String reviewData) {
                Log.d(TAG, "60 found in loadInBackground");
                if(reviewData == null || TextUtils.isEmpty(reviewData)){
                    return null;
                }

                try {
                    Log.d(TAG, "in TRY 60");
                    URL theMovieDbReviewUrl = new URL(reviewData);
                    return NetworkUtils.getResponseFromHttpUrl(theMovieDbReviewUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "exiting loadInBackground after exception");
                    return null;
                }
            }

            @Override
            protected void onStartLoading() {
                Log.d(TAG, "--- entering onStartLoading in Loader method --- ID: " + id);
                super.onStartLoading();
                if(args == null){
                    return;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        Log.d(TAG, "entering onLoadFinished");
            if (data != null && !data.equals("")) {
                Log.d(TAG, data);
                JSONObject jsonObject = JsonUtils.getJSONObject(data);

                Log.d(TAG, "JSON size is : " + jsonObject.length());
                switch (jsonObject.length()) {
                    case 2:                             //video api result size
                        getVideoData(jsonObject);
                        break;
                    case 5:                             //review api result size
                        getReviewData(jsonObject);
                        break;
                    default:
                        Log.e(TAG, "no match found in onLoadFinished() switch, looking for either getVideoData() or getReviewData()");
                        break;
                }
            } else {
                Log.e(TAG, "empty data back from themoviedb api call");
            }
    }

    private void getReviewData(JSONObject reviewObject) {
        Log.d(TAG, "entering getReviewData");
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

    private void getVideoData(JSONObject videoObject) {
        Log.d(TAG, "entering getVideoData");
        JSONArray jsonMovieVideos = JsonUtils.getJSONArray(videoObject, "results");

        videoKeys = new String[jsonMovieVideos.length()];
        trailerList = new ArrayList<>();

        int nextTrailer = 0;
        for(String trailer : videoKeys){    //TODO change to for loop
            JSONObject reviewDetails = JsonUtils.getJSONObject(jsonMovieVideos, nextTrailer);
            videoKeys[nextTrailer] = JsonUtils.getString(reviewDetails, "key");

            trailerList.add("Trailer " + nextTrailer);

            ++nextTrailer;
        }

        Log.d(TAG, "debugging trailer list ======= items are:" + Arrays.toString(trailerList.toArray()));


        for(int i = 0; i < videoKeys.length; ++i){
            Log.d(TAG, "key is: " + videoKeys[i]);
        }
        //if(videoKeys.length > 1) addExtraTrailerViewsIfNeeded();
        addExtraTrailerViewsIfNeeded();
    }


    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
