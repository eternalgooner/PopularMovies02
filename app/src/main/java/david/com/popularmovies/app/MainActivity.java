package david.com.popularmovies.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import david.com.popularmovies.R;
import david.com.popularmovies.adapters.MovieAdapter;
import david.com.popularmovies.db.FavMoviesContract;
import david.com.popularmovies.db.FavMoviesDbHelper;
import david.com.popularmovies.model.Movie;
import david.com.popularmovies.model.MovieCollection;
import david.com.popularmovies.utils.CursorUtils;
import david.com.popularmovies.utils.JsonUtils;
import david.com.popularmovies.utils.NetworkUtils;

/**
 *
 * class that starts the application
 * - has inner anonymous inner class AsyncTaskLoader
 *
 * - display message if no network connection
 *
 * - if there is a network connection it will:
 *      - build a URL
 *      - pass the URL to AsyncTaskLoader to retrieve JSON data from themoviedb
 *      - JSON data is then stored for each movie in a Movie object, which is then added to an ArrayList of movies
 *      - display movie posters in a grid layout
 *
 * UI:
 * - create RecyclerView, GridLayoutManager & Adapter for displaying scrolling list
 * - create menu option to sort by (i)Highest Rated, (ii)Most Popular or (iii)Favourites
 * - upon poster click, new activity should show clicked movie details
 *
 * STRING LITERALS:
 * - only string literals are in log statements
 *
 * ATTRIBUTION:
 * - some code was implemented with help from Android Dev API, Udacity Android course & StackOverflow
 *
 * INFO:
 * you need to supply your own API key to retrieve data from themoviedb (API key is used in NetworkUtils class)
 */

public class MainActivity extends AppCompatActivity implements MovieAdapter.ListItemClickListener, LoaderManager.LoaderCallbacks<String>{

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView txtNoNetworkMessage;
    private static final int NUM_LIST_ITEMS = 20;
    private MovieAdapter mMovieAdapter;
    private RecyclerView mRecyclerView;
    private String[] posterPaths;
    private GridLayoutManager gridLayoutManager;
    private Bundle movieBundle = new Bundle();
    private SQLiteDatabase mDb;
    private static final int THE_MOVIE_DB_MOST_POPULAR_LOADER = 58;
    private static final int THE_MOVIE_DB_HIGHEST_RATED_LOADER = 59;
    public enum MenuState {MENU_MOST_POPULAR, MENU_HIGHEST_RATED, MENU_FAV}
    private MenuState mMenuState = MenuState.MENU_MOST_POPULAR;
    private ArrayList<Movie> newMovieList;
    private int currentMenu = 1;
    private MovieCollection mMovieCollection;
    private Cursor mCursor;
    private static final int PORTRAIT_MODE = 3;
    private static final int LANDSCAPE_MODE = 4;
    private static final int MOST_POPULAR_MENU = 1;
    private static final int HIGHEST_RATED_MENU = 2;
    private static final int FAVOURITES_MENU = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "entering onCreate");
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_moviePosters);

        //if null - load for the first time
        if(savedInstanceState == null){
            setContentView(R.layout.activity_main);
            getSupportActionBar().setSubtitle(getString(R.string.Most_Popular));
            mRecyclerView = (RecyclerView) findViewById(R.id.rv_moviePosters);
            newMovieList = new ArrayList<>();
            mMovieCollection = new MovieCollection(getApplicationContext());

            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                gridLayoutManager = new GridLayoutManager(this, PORTRAIT_MODE);
            } else {
                gridLayoutManager = new GridLayoutManager(this, LANDSCAPE_MODE);
            }
            mRecyclerView.setLayoutManager(gridLayoutManager);
            txtNoNetworkMessage = (TextView) findViewById(R.id.message_no_network_connection);

            if (isNetworkAvailable()) {
                loadMovieList(getString(R.string.mostPopular));
            } else {
                txtNoNetworkMessage.setVisibility(View.VISIBLE);
            }

            FavMoviesDbHelper dbHelper = new FavMoviesDbHelper(this);
            mDb = dbHelper.getReadableDatabase();
        }else{
            //else load from savedInstanceState
            setContentView(R.layout.activity_main);
            mRecyclerView = (RecyclerView) findViewById(R.id.rv_moviePosters);
            mMovieCollection = (MovieCollection) savedInstanceState.getParcelable(getString(R.string.movieCollection));
            posterPaths = savedInstanceState.getStringArray(getString(R.string.posterPaths));
            currentMenu = savedInstanceState.getInt(getString(R.string.menu));
            txtNoNetworkMessage = (TextView) findViewById(R.id.message_no_network_connection);
            if(currentMenu == MOST_POPULAR_MENU){
                Log.e(TAG, "in onCreate() and currentMenu is: " + currentMenu);
                mMenuState = MenuState.MENU_MOST_POPULAR;
                newMovieList = mMovieCollection.getMostPopular();
                getSupportActionBar().setSubtitle(getString(R.string.Most_Popular));
            }else if(currentMenu == HIGHEST_RATED_MENU){
                Log.e(TAG, "in onCreate() and currentMenu is: " + currentMenu);
                mMenuState = MenuState.MENU_HIGHEST_RATED;
                newMovieList = mMovieCollection.getHighestRated();
                getSupportActionBar().setSubtitle(getString(R.string.highest_rated));
            }else if(currentMenu == FAVOURITES_MENU){
                Log.e(TAG, "in onCreate() and currentMenu is: " + currentMenu);
                mMenuState = MenuState.MENU_FAV;
                newMovieList = mMovieCollection.getFavourites();
                getSupportActionBar().setSubtitle(getString(R.string.Favourites));
            }else{
                Log.e(TAG, "error when restoring menu state in onRestoreInstanceState - no match found, currentMenu is: " + currentMenu);
            }

            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                gridLayoutManager = new GridLayoutManager(this, PORTRAIT_MODE);
            } else {
                gridLayoutManager = new GridLayoutManager(this, LANDSCAPE_MODE);
            }
            mRecyclerView.setLayoutManager(gridLayoutManager);
            showMovies();
        }

        Log.d(TAG, "exiting onCreate");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "entering onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        outState.putParcelable(getString(R.string.movieCollection), mMovieCollection);
        outState.putStringArray(getString(R.string.posterPaths), posterPaths);
        Log.d(TAG, "in onSaveInstanceState(), currentMenu is: " + currentMenu);
        outState.putInt(getString(R.string.menu), currentMenu);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "entering onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mMenuState == MenuState.MENU_FAV){
            showFavourites();
        }
    }

    private boolean isNetworkAvailable(){
        Log.d(TAG, "entering isNetworkAvailable");
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        Log.d(TAG, "exiting isNetworkAvailable");
        return ((activeNetworkInfo != null) && (activeNetworkInfo.isConnected()));
    }

    private void showMovies(){
        Log.d(TAG, "entering showMovies");
        posterPaths = getCurrentPosterPaths(newMovieList);
        if(mMenuState != MenuState.MENU_FAV){
            mMovieAdapter = new MovieAdapter(posterPaths, NUM_LIST_ITEMS, this);
        }else{
            if(mCursor == null){
                Log.d(TAG, "in showMovies for FAV, cursor IS null");
                mMovieAdapter = new MovieAdapter(mMovieCollection.getFavourites(), posterPaths, getApplicationContext(), this);
            }else{
                Log.d(TAG, "in showMovies for FAV, cursor is NOT null");
                mMovieAdapter = new MovieAdapter(getApplicationContext(), mCursor, this, posterPaths);
            }
        }
        Log.d(TAG, "***** setting adapter *****");
        mRecyclerView.setAdapter(mMovieAdapter);
        Log.d(TAG, "exiting showMovies");
    }

    private String[] getCurrentPosterPaths(ArrayList<Movie> newMovieList) {
        String[] currentPaths = new String[newMovieList.size()];
        for(int i = 0; i < currentPaths.length; ++i){
            currentPaths[i] = newMovieList.get(i).getmPosterPath();
        }
        return currentPaths;
    }

    private void loadMovieList(String sortType) {
        Log.d(TAG, "entering loadMovieList for " + sortType);
        URL myUrl = NetworkUtils.buildUrl(sortType, getApplicationContext(), getString(R.string.default_id));

        Bundle queryBundle = new Bundle();
        queryBundle.putString(getString(R.string.theMovieDb) + sortType + getString(R.string.Query), myUrl.toString());

        LoaderManager loaderManager = getSupportLoaderManager();

        if(sortType.equals(getString(R.string.mostPopular))){
            newMovieList = new ArrayList<>();
            loaderManager.initLoader(THE_MOVIE_DB_MOST_POPULAR_LOADER, queryBundle, this).forceLoad();
        }else if(sortType.equals(getString(R.string.highestRated))){
            newMovieList = new ArrayList<>();
            loaderManager.initLoader(THE_MOVIE_DB_HIGHEST_RATED_LOADER, queryBundle, this).forceLoad();
        }
        Log.d(TAG, "exiting loadMovieList");
    }

    @Override
    public void onListItemClick(int clickedItem) {
        Log.d(TAG, "entering onListItemClick");
        Intent intent = new Intent(MainActivity.this, MovieDetailsActivity.class);

        if(mMenuState == MenuState.MENU_FAV){
            Movie selectedFavMovie;
            Cursor cursor = getClickedMovieData(clickedItem);

            Log.e("cursor count is: ", cursor.getCount()+getString(R.string.emptyString));
            while(cursor.moveToNext()){
                Log.e("debug cursor", cursor.getString(0));
                Log.e("debug cursor", cursor.getString(1));
                Log.e("debug cursor", cursor.getString(2));
                Log.e("debug cursor", cursor.getString(3));
                Log.e("debug cursor", cursor.getString(4));
                Log.e("debug cursor", cursor.getString(5));
                Log.e("debug cursor", cursor.getString(6));
                Log.e("debug cursor", cursor.getString(7));
                Log.e("debug cursor", cursor.getString(8));
            }
            selectedFavMovie = CursorUtils.convertCursorToMovieObject(cursor);
            intent.putExtra(getString(R.string.currentMenu), currentMenu);
            intent.putExtra(getString(R.string.selectedMovie), selectedFavMovie);
            movieBundle.putBoolean(getString(R.string.isFav), true);
        }else{
            intent.putExtra(getString(R.string.selectedMovie), newMovieList.get(clickedItem));
            intent.putExtra(getString(R.string.currentMenu), currentMenu);
            Log.e(TAG, "add movie here into intent. Movie poster path is " + newMovieList.get(clickedItem).getmPosterPath());
            boolean isFav = checkIfMovieIsAlreadyInFavourites(clickedItem);
            movieBundle.putBoolean(getString(R.string.isFav), isFav);
        }

        intent.putExtras(movieBundle);
        MainActivity.this.startActivity(intent);
        Log.d(TAG, "exiting onListItemClick");
    }

    private boolean checkIfMovieIsAlreadyInFavourites(int clickedItem) {
        Cursor cursor = getClickedMovieData(clickedItem);
        if(cursor.getCount() < 1){
            return false;
        }else{
            return true;
        }
    }

    private Cursor getClickedMovieData(int clickedItem) {
        String[] selectedMovie = { newMovieList.get(clickedItem).getmMovieId() + getString(R.string.emptyString)};
        try{
            Uri uri = FavMoviesContract.FavMovieEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(selectedMovie[0]).build();
            Log.d(TAG, "SelectedMovie is: " + selectedMovie[0] + "....Uri sending to DB for fav movie query is: " + uri.toString());
            return getContentResolver().query(uri,
                                            null,
                                            getString(R.string.movieIdEqualsQuestionMark),
                                            selectedMovie,
                                            null);
        }catch (Exception e){
            Log.e(TAG, "failed to async load data");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "entering onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.movie_menu, menu);
        Log.d(TAG, "exiting onCreateOptionsMenu");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "entering onOptionsItemSelected");
        int itemSelected = item.getItemId();
        Log.d(TAG, "item id is: " + itemSelected);

        switch (itemSelected){
            case R.id.menu_most_popular:
                if(mMenuState != MenuState.MENU_MOST_POPULAR) showMostPopular();
                item.setChecked(true);
                getSupportActionBar().setSubtitle(getString(R.string.most_popular));
                currentMenu = MOST_POPULAR_MENU;
                break;
            case R.id.menu_highest_rated:
                if(mMenuState != MenuState.MENU_HIGHEST_RATED) showHighestRated();
                item.setChecked(true);
                getSupportActionBar().setSubtitle(getString(R.string.highest_rated));
                currentMenu = HIGHEST_RATED_MENU;
                break;
            case R.id.menu_favourites:
                showFavourites();
                item.setChecked(true);
                getSupportActionBar().setSubtitle(getString(R.string.Favourites));
                currentMenu = FAVOURITES_MENU;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(currentMenu == MOST_POPULAR_MENU){
            mMenuState = MenuState.MENU_MOST_POPULAR;
        }else if(currentMenu == HIGHEST_RATED_MENU){
            mMenuState = MenuState.MENU_HIGHEST_RATED;
            menu.findItem(R.id.menu_highest_rated).setChecked(true);
            getSupportActionBar().setSubtitle(getString(R.string.highest_rated));
        }else if(currentMenu == FAVOURITES_MENU){
            mMenuState = MenuState.MENU_FAV;
            menu.findItem(R.id.menu_favourites).setChecked(true);
            getSupportActionBar().setSubtitle(getString(R.string.Favourites));
        }else{
            Log.e(TAG, "error when restoring menu state in onRestoreInstanceState - no match found");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void showFavourites() {
        mMenuState = MenuState.MENU_FAV;
        txtNoNetworkMessage.setVisibility(View.INVISIBLE);
        mCursor = getAllFavMovies();
        refreshMovieList(mCursor);
        mMovieAdapter = new MovieAdapter(this, mCursor, this, getCurrentPosterPaths(newMovieList));
        mRecyclerView.setAdapter(mMovieAdapter);
    }

    private void refreshMovieList(Cursor cursor) {
        Log.e(TAG, "refreshingMovieList()");
        newMovieList = CursorUtils.convertCursorToArrayListOfMovie(cursor);
        mMovieCollection.setFavourites(newMovieList);
    }

    private void showHighestRated() {
        Log.d(TAG, "entering showHighestRated");
        mMenuState = MenuState.MENU_HIGHEST_RATED;
        if((isNetworkAvailable()) && (mMovieCollection.getHighestRated() == null)){
            txtNoNetworkMessage.setVisibility(View.INVISIBLE);
            loadMovieList(getString(R.string.highestRated));
        }else if(mMovieCollection.getHighestRated() != null && isNetworkAvailable()){
            txtNoNetworkMessage.setVisibility(View.INVISIBLE);
            loadCachedCollection(getString(R.string.highestRated));
        }else if(mMovieCollection.getHighestRated() != null){
            txtNoNetworkMessage.setVisibility(View.INVISIBLE);
            loadCachedCollection(getString(R.string.highestRated));
        }else{
            txtNoNetworkMessage.setVisibility(View.VISIBLE);
            mRecyclerView.setAdapter(null);
        }
    }

    private void loadCachedCollection(String sortType) {
        Log.d(TAG, "entering loadCachedCollection() with sortType: " + sortType);
        if(sortType.equals(getString(R.string.mostPopular))){
            Log.d(TAG, "load most popular cache");
            newMovieList = mMovieCollection.getMostPopular();
            posterPaths = getCurrentPosterPaths(newMovieList);
            showMovies();
        }else if(sortType.equals(getString(R.string.highestRated))){
            Log.d(TAG, "load highest rated cache");
            newMovieList = mMovieCollection.getHighestRated();
            posterPaths = getCurrentPosterPaths(newMovieList);
            showMovies();
        }else if(sortType.equals(getString(R.string.favourites))){
            Log.d(TAG, "load favourites cache");
            newMovieList = mMovieCollection.getFavourites();
            posterPaths = getCurrentPosterPaths(newMovieList);
            mMovieAdapter = new MovieAdapter(newMovieList, posterPaths, this, this);
            showMovies();
        }
    }

    private void showMostPopular() {
        Log.d(TAG, "entering showMostPopular");
        mMenuState = MenuState.MENU_MOST_POPULAR;
        if((isNetworkAvailable()) && (mMovieCollection.getMostPopular() == null)){
            txtNoNetworkMessage.setVisibility(View.INVISIBLE);
            Log.d(TAG, "in showMostPopular() - 1st if true");
            loadMovieList(getString(R.string.mostPopular));
        }else if(mMovieCollection.getMostPopular() != null && isNetworkAvailable()){
            txtNoNetworkMessage.setVisibility(View.INVISIBLE);
            Log.d(TAG, "in showMostPopular() - 2nd if true");
            loadCachedCollection(getString(R.string.mostPopular));
        }else if(mMovieCollection.getMostPopular() != null){
            txtNoNetworkMessage.setVisibility(View.INVISIBLE);
            Log.d(TAG, "in showMostPopular() - 3rd if true");
            loadCachedCollection(getString(R.string.mostPopular));
        }else{
            Log.d(TAG, "in showMostPopular() - 4th if true");
            txtNoNetworkMessage.setVisibility(View.VISIBLE);
            mRecyclerView.setAdapter(null);
        }
    }


    @Override
    public Loader<String> onCreateLoader(final int id, final Bundle args) {
        Log.d(TAG, "--- entering onCreateLoader in Loader method ---");
        return new AsyncTaskLoader<String>(this) {
            @Override
            public String loadInBackground() {
                Log.d(TAG, "--- entering loadInBackground in Loader method ---");
                String theMovieDbMostPopularQueryString = args.getString(getString(R.string.tmdbMostPopQuery));
                String theMovieDbHighestRatedQueryString = args.getString(getString(R.string.tmdbHighestRatedQuery));

                if(id == THE_MOVIE_DB_MOST_POPULAR_LOADER){
                    return processMostPopularInBackground(theMovieDbMostPopularQueryString);
                }else{
                    return processHighestRatedInBackground(theMovieDbHighestRatedQueryString);
                }
            }

            private String processHighestRatedInBackground(String queryString) {
                Log.d(TAG, "ATL --- entering processHighestRatedInBackground in Loader method ---");
                if(queryString == null || TextUtils.isEmpty(queryString)){
                    Log.d(TAG, "++ theMovieDbQueryString is null or empty & will return null ++");
                    return null;
                }
                try {
                    URL theMovieDbUrl = new URL(queryString);
                    String stringResponseFromRequest = NetworkUtils.getResponseFromHttpUrl(theMovieDbUrl);
                    getAllPosterPathsAndMovieDataFromJson(stringResponseFromRequest);
                    return stringResponseFromRequest;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "exiting onCreateLoader after exception");
                    return null;
                }
            }

            private String processMostPopularInBackground(String queryString) {
                Log.d(TAG, "ATL --- entering processMostPopularInBackground in Loader method ---");
                if(queryString == null || TextUtils.isEmpty(queryString)){
                    Log.d(TAG, "ATL++ theMovieDbQueryString is null or empty & will return null ++");
                    return null;
                }
                try {
                    URL theMovieDbUrl = new URL(queryString);
                    String stringResponseFromRequest = NetworkUtils.getResponseFromHttpUrl(theMovieDbUrl);
                    getAllPosterPathsAndMovieDataFromJson(stringResponseFromRequest);
                    return stringResponseFromRequest;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "ATL exiting onCreateLoader after exception");
                    return null;
                }
            }

            private void getAllPosterPathsAndMovieDataFromJson(String stringResponseFromRequest) {
                Log.d(TAG, "entering getAllPosterPathsAndMovieDataFromJson");
                posterPaths = new String[20];
                if (stringResponseFromRequest != null && !stringResponseFromRequest.equals(getString(R.string.emptyString))) {
                    Log.d(TAG, stringResponseFromRequest);
                    JSONObject jsonObject = JsonUtils.getJSONObject(stringResponseFromRequest);
                    JSONArray jsonMoviesArray = JsonUtils.getJSONArray(jsonObject, getString(R.string.results));
                    int next = 0;
                    for(int i = 0; i < jsonMoviesArray.length(); ++i){
                        JSONObject nextMovie = JsonUtils.getJSONObject(jsonMoviesArray, next);
                        posterPaths[next] = JsonUtils.getString(nextMovie, getString(R.string.poster_path));
                        Log.d(TAG, posterPaths[next]);
                        posterPaths[next] = getString(R.string.base_poster_path) + posterPaths[next];     //other poster sizes are w92, w154, w185, w342, w500, w780 or original
                        getAllMovieData(nextMovie,  posterPaths[next]);
                        ++next;
                    }
                    if(mMenuState == MenuState.MENU_MOST_POPULAR){
                        mMovieCollection.setMostPopular(newMovieList);
                    }else if(mMenuState == MenuState.MENU_HIGHEST_RATED){
                        mMovieCollection.setHighestRated(newMovieList);
                    }
                    Log.d(TAG, "exiting onLoadFinished");
                } else {
                    Log.e(TAG, "empty data back from themoviedb api call");
                }
            }

            //TODO finished off here. need to set no review string for movies in first 2 lists - working for fav list
            private void getAllMovieData(JSONObject clickedMovie, String posterPath) {
                Log.d(TAG, "ATL entering getAllMovieData");
                Movie movie = new Movie(JsonUtils.getString(clickedMovie, getString(R.string.original_title)),
                                        JsonUtils.getString(clickedMovie, getString(R.string.vote_average)),
                                        JsonUtils.getString(clickedMovie, getString(R.string.release_date)),
                                        JsonUtils.getString(clickedMovie, getString(R.string.overview)),
                                        getString(R.string.noTrailers),          //TODO check this, maybe set as null? otherwise always showing one trailer, but won't load if really no trailer
                                        getString(R.string.noReviewsYet),
                                        JsonUtils.getString(clickedMovie, getString(R.string.id)),
                                        posterPath);
                newMovieList.add(movie);
                Log.d(TAG, "ATL exiting getAllMovieData");
            }

            @Override
            protected void onStartLoading() {
                Log.d(TAG, "ATL --- entering onStartLoading in Loader method ---");
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
            showMovies();
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    private Cursor getAllFavMovies(){
        try{
            return getContentResolver().query(FavMoviesContract.FavMovieEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
        }catch (Exception e){
            Log.e(TAG, "failed to async load data");
            e.printStackTrace();
            return null;
        }
    }
}
