package david.com.popularmovies.app;

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
import david.com.popularmovies.db.FavMoviesDbHelper;
import david.com.popularmovies.model.Movie;
import david.com.popularmovies.services.InsertOrDeleteFromDbService;
import david.com.popularmovies.utils.JsonUtils;
import david.com.popularmovies.utils.NetworkUtils;

/**
 * Class that shows the selected movie details
 * - movie details are retrieved as a bundle from the intent
 * - movie details are then retrieved as a Movie from the bundle
 * - AsyncTasksLoaders are called at start of the activity to get review & trailer data
 *
 * UI:
 * - LinearLayout used with multiple CardViews
 *
 * STRING LITERALS:
 * - string literals in log statements only
 *
 * ATTRIBUTION:
 * - some code was implemented with help from StackOverflow
 *
 */

public class MovieDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>{

    private static final String TAG = MovieDetailsActivity.class.getSimpleName();
    private TextView movieTitle;
    private ImageView moviePoster;
    private TextView userRating;
    private TextView releaseDate;
    private ImageButton mFavStar;
    protected TextView movieSummary;
    private ExpandableTextView expandableTextView;
    private boolean mIsFavourite;
    private String[] videoKeys;
    private String[] reviews;
    private SQLiteDatabase mDb;
    private Bundle bundle;
    private Movie selectedMovie;
    private ExpandableListView listTrailerView;
    private ExpandableListAdapter listTrailerAdapter;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listHash;
    private List<String> trailerList;
    private static final int THE_MOVIE_DB_REVIEW_LOADER = 60;
    private static final int THE_MOVIE_DB_TRAILER_LOADER = 61;
    private static final int INSERT_ACTION = 1;
    private static final int DELETE_ACTION = -1;
    private int currentMenu = 0;
    private static final int START = 0;
    private static final int END = 4;
    private static final int MENU_FAVOURITES = 3;
    private static final String REVIEW_SEPARATOR = "\"\n\n          -----------------------------------------------\n\n";
    private static final String SEMI_COLON = ":";
    private static final String NEW_LINE = "\n\"";

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
        expandableTextView = (ExpandableTextView) findViewById(R.id.expandable_text_view);
        listTrailerView = (ExpandableListView) findViewById(R.id.expLV);

        bundle = this.getIntent().getExtras();
        selectedMovie = getIntent().getExtras().getParcelable(getString(R.string.selectedMovie));
        if(savedInstanceState != null){
            mIsFavourite = savedInstanceState.getBoolean(getString(R.string.isFav));
        }else {
            mIsFavourite = bundle.getBoolean(getString(R.string.isFav));
        }

        currentMenu = getIntent().getIntExtra(getString(R.string.currentMenu), 5);
        Log.d(TAG, "came from menu state: " + currentMenu);

        if(isNetworkAvailable() && !mIsFavourite){
            Log.d("TAG --- +++", "newtowrk is available & movie is not a FAV ");
            loadMovieReview(getString(R.string.reviews));
            getTrailerData(getString(R.string.videos));
        }else{
            Log.d("TAG --- +++", "MOVIE IS FAV");
            loadLocalMovieData();
        }

        if(mIsFavourite){
            mFavStar.setImageResource(R.drawable.fav_star_on);
        }else {
            mFavStar.setImageResource(R.drawable.fav_star_off);
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
        displayMovieDetails(selectedMovie);
        Log.d(TAG, "exiting onCreate");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(getString(R.string.isFav), mIsFavourite);
    }

    private void playTrailer(String trailerId) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.youtube_url) + trailerId));

        PackageManager packageManager = getPackageManager();
        List activities = packageManager.queryIntentActivities(appIntent, PackageManager.MATCH_DEFAULT_ONLY);
        boolean isIntentSafe = activities.size() > 0;

        if(isIntentSafe){
            startActivity(appIntent);
        }
    }

    private void getTrailerData(String videos) {
        String movieId = selectedMovie.getmMovieId();
        URL myUrl = NetworkUtils.buildUrl(videos,movieId);

        Bundle queryBundle = new Bundle();
        queryBundle.putString(getString(R.string.theMovieDbTrailerQuery), myUrl.toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> theMovieDbLoader = loaderManager.getLoader(THE_MOVIE_DB_TRAILER_LOADER);

        loaderManager.initLoader(THE_MOVIE_DB_TRAILER_LOADER, queryBundle, this).forceLoad();
    }

    private void loadMovieReview(String reviews) {
        String movieId = selectedMovie.getmMovieId();
        URL myUrl = NetworkUtils.buildUrl(reviews, movieId);

        Bundle queryBundle = new Bundle();
        queryBundle.putString(getString(R.string.theMovieDbReviewQuery), myUrl.toString());

        LoaderManager loaderManager = getSupportLoaderManager();

        loaderManager.initLoader(THE_MOVIE_DB_REVIEW_LOADER, queryBundle, this).forceLoad();
    }

    private void loadLocalMovieData() {
        Log.d("TAG --- +++", "in loadLocalMovieData() ");
        String review = selectedMovie.getmReview();
        Log.d("TAG --- +++", "review data is: " + review);
        expandableTextView.setText(review);

        String trailerKey = selectedMovie.getmTrailer();
        trailerList = new ArrayList<>();
        trailerList.add(trailerKey);

        Log.d(TAG, "debugging trailer list ======= items are:" + Arrays.toString(trailerList.toArray()));


        for(int i = 0; i < trailerList.size(); ++i){
            Log.d(TAG, "key is: " + trailerList.get(i));
        }
        if(trailerList.size() > 1) addExtraTrailerViewsIfNeeded();
    }

    private boolean isNetworkAvailable(){
        Log.d(TAG, "entering isNetworkAvailable");
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        Log.d(TAG, "exiting isNetworkAvailable");
        return ((activeNetworkInfo != null) && (activeNetworkInfo.isConnected()));
    }

    private void displayMovieDetails(Movie movie) {
        Log.d(TAG, "entering displayMovieDetails");
        StringBuilder movieYear = new StringBuilder(movie.getmYear());
        String year = movieYear.substring(START, END);
        //String posterPrefix = getString(R.string.url_poster_prefix);
        movieTitle.setText(movie.getmTitle());
        movieSummary.setText(movie.getmSummary());
        userRating.setText(movie.getmRating() + getString(R.string.out_of_ten));
        releaseDate.setText(year);
        if(!mIsFavourite){
            Log.d(TAG, "in displayMovieDetails(), setting real image as poster - not a FAV");
            Picasso.with(getApplicationContext()).load(movie.getmPosterPath()).into(moviePoster);
        }else{
            if(currentMenu != MENU_FAVOURITES){
                Log.d(TAG, "in displayMovieDetails(), setting real image as poster - FAV from real menu");
                Picasso.with(getApplicationContext()).load(movie.getmPosterPath()).into(moviePoster);
            }else {
                Log.d(TAG, "in displayMovieDetails(), setting default image as poster");
                moviePoster.setPadding(24, 224, 24, 24);
                moviePoster.setImageResource(R.mipmap.movie_projector);
            }
        }
        Log.d(TAG, "poster path is: " + movie.getmPosterPath());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void clickFav(View view){
        if(mIsFavourite){
            Intent dbIntent = new Intent(this, InsertOrDeleteFromDbService.class);
            dbIntent.putExtra(getString(R.string.movieId), selectedMovie.getmMovieId());
            dbIntent.putExtra(getString(R.string.dbAction), DELETE_ACTION);
            startService(dbIntent);
            mFavStar.setImageResource(R.drawable.fav_star_off);
            Toast.makeText(getApplicationContext(), R.string.removed_from_Favourites, Toast.LENGTH_SHORT).show();
            mIsFavourite = false;
        }else{
            mFavStar.setImageResource(R.drawable.fav_star_on);
            addMovieToFavUsingService();
            Toast.makeText(getApplicationContext(), R.string.added_to_Favourites, Toast.LENGTH_SHORT).show();
            mIsFavourite = true;
        }
    }

    private void addExtraTrailerViewsIfNeeded() {
        Log.d(TAG, "entering addExtraTrailerViewsIfNeeded");
        listDataHeader = new ArrayList<>();
        listDataHeader.add(getString(R.string.Trailers));
        listHash = new HashMap<>();

        Log.d(TAG, "debugging trailer list in add extraTrailerViews ======= items in list is now:" + Arrays.toString(trailerList.toArray()));
        listHash.put(listDataHeader.get(0), trailerList);
        Log.d(TAG, "list data header index 0 is:" + listDataHeader.get(0).toString());
        Log.d(TAG, "1st item in list hash is:" + listHash.get(listDataHeader.get(0)).toString());
        Log.d(TAG, "listhash size : " + listHash.size());
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

    private void addMovieToFavUsingService(){
        Intent dbIntent = new Intent(this, InsertOrDeleteFromDbService.class);
        dbIntent.putExtra(getString(R.string.selectedMovie), selectedMovie);
        dbIntent.putExtra(getString(R.string.dbAction), INSERT_ACTION);
        startService(dbIntent);
    }

    @Override
    public Loader<String> onCreateLoader(final int id, final Bundle args) {
        Log.d(TAG, "entering **** new  onCreateLoader *** & ID is:" + id);
        return new AsyncTaskLoader<String>(this) {
            @Override
            public String loadInBackground() {
                String theMovieDbReviewQueryString = args.getString(getString(R.string.theMovieDbReviewQuery));
                String theMovieDbTrailerQueryString = args.getString(getString(R.string.theMovieDbTrailerQuery));
                if(id == THE_MOVIE_DB_REVIEW_LOADER){
                     return processReviewQueryData(theMovieDbReviewQueryString);
                }else if(id == THE_MOVIE_DB_TRAILER_LOADER){
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

        for(int i = 0; i < reviews.length; ++i){
            JSONObject reviewDetails = JsonUtils.getJSONObject(jsonMovieReviews, i);
            reviews[i] = JsonUtils.getString(reviewDetails, getString(R.string.content));
            authors[i] = JsonUtils.getString(reviewDetails, getString(R.string.author));
        }

        for(int i = 0; i < reviews.length; ++i){
            expandableTextView.setText(expandableTextView.getText() + authors[i] + SEMI_COLON + NEW_LINE + reviews[i] + REVIEW_SEPARATOR);
        }
    }

    private void getVideoData(JSONObject videoObject) {
        Log.d(TAG, "entering getVideoData");
        JSONArray jsonMovieVideos = JsonUtils.getJSONArray(videoObject, getString(R.string.results));

        videoKeys = new String[jsonMovieVideos.length()];
        trailerList = new ArrayList<>();

        for(int i = 0; i < videoKeys.length; ++i){
            JSONObject reviewDetails = JsonUtils.getJSONObject(jsonMovieVideos, i);
            videoKeys[i] = JsonUtils.getString(reviewDetails, getString(R.string.key));
            trailerList.add(getString(R.string.Trailer_) + i);
        }
        Log.d(TAG, "debugging trailer list ======= items are:" + Arrays.toString(trailerList.toArray()));

        for(int i = 0; i < videoKeys.length; ++i){
            Log.d(TAG, "key is: " + videoKeys[i]);
        }
        if(videoKeys.length > 1) addExtraTrailerViewsIfNeeded();
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
