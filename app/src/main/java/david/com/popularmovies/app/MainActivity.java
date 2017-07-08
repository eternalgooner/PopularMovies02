package david.com.popularmovies.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import david.com.popularmovies.R;
import david.com.popularmovies.adapters.MovieAdapter;
import david.com.popularmovies.db.FavMoviesContract;
import david.com.popularmovies.db.FavMoviesDbHelper;
import david.com.popularmovies.model.Movie;
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
 *      - JSON data is then stored for each movie in a HashMap, which is then added to an ArrayList of movies
 *      - display movie posters in a grid layout
 *
 * UI:
 * - create RecyclerView, GridLayoutManager & Adapter for displaying scrolling list
 * - create menu option to sort by highest rated or most popular
 * - upon poster click, new activity should show clicked movie details
 *
 * STRING LITERALS:
 * - only string literals are in log statements
 *
 * ATTRIBUTION:
 * - some code was implemented with help from Udacity Android course
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
    private ArrayList<HashMap> movieList;
    private GridLayoutManager gridLayoutManager;
    //private boolean showingMostPopular = true;
    //private boolean showingFavList = false;
    private Bundle movieBundle = new Bundle();
    private SQLiteDatabase mDb;
    private static final int THE_MOVIE_DB_MOST_POPULAR_LOADER = 58;
    private static final int THE_MOVIE_DB_HIGHEST_RATED_LOADER = 59;
    private enum MenuState {MENU_MOST_POPULAR, MENU_HIGHEST_RATED, MENU_FAV}
    private MenuState mMenuState = MenuState.MENU_MOST_POPULAR;

    private ArrayList<Movie> newMovieList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "entering onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_moviePosters);
        //movieList = new ArrayList<>();
        newMovieList = new ArrayList<>();

        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            gridLayoutManager = new GridLayoutManager(this, 3);
        }else{
            gridLayoutManager = new GridLayoutManager(this, 4);
        }
        mRecyclerView.setLayoutManager(gridLayoutManager);
        txtNoNetworkMessage = (TextView) findViewById(R.id.message_no_network_connection);

        if(isNetworkAvailable()){
            loadMovieList(getString(R.string.mostPopular));
        }else{
            txtNoNetworkMessage.setVisibility(View.VISIBLE);
        }

        FavMoviesDbHelper dbHelper = new FavMoviesDbHelper(this);
        mDb = dbHelper.getReadableDatabase();

        Log.d(TAG, "exiting onCreate");
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        //re-queries for all tasks
//        getSupportLoaderManager().restartLoader(THE_MOVIE_DB_MOST_POPULAR_LOADER, null, this); //TODO need to fix this as only working for popular movies, currently this breaks most popular
//    }

    private boolean isNetworkAvailable(){
        Log.d(TAG, "entering isNetworkAvailable");
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        Log.d(TAG, "exiting isNetworkAvailable");
        return ((activeNetworkInfo != null) && (activeNetworkInfo.isConnected()));
    }

    private void showMovies(){
        Log.d(TAG, "entering showMovies");
        mMovieAdapter = new MovieAdapter(posterPaths, NUM_LIST_ITEMS, this);
        Log.d(TAG, "***** setting adapter *****");
        mRecyclerView.setAdapter(mMovieAdapter);
        Log.d(TAG, "exiting showMovies");
    }

    private void loadMovieList(String sortType) {
        Log.d(TAG, "entering loadMovieList for " + sortType);
        URL myUrl = NetworkUtils.buildUrl(sortType, getApplicationContext(), "0");      //TODO "0" needs to be changed to get the ID of the movie, used for getting reviews & trailers for movie

        Bundle queryBundle = new Bundle();
        queryBundle.putString("theMovieDb" + sortType + "Query", myUrl.toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        //Loader<String> theMovieDbLoader = loaderManager.getLoader(THE_MOVIE_DB_LOADER);

        if(sortType.equals("mostPopular")){
            //movieList = new ArrayList<>();
            newMovieList = new ArrayList<>();
            loaderManager.initLoader(THE_MOVIE_DB_MOST_POPULAR_LOADER, queryBundle, this).forceLoad();
        }else if(sortType.equals("highestRated")){
            //movieList = new ArrayList<>();
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
            //HashMap<String, String> favMovie = new HashMap();
            Movie selectedFavMovie = null;
            Cursor cursor = getClickedMovieData(clickedItem + 1); //TODO what's this +1??

            Log.e("cursor count is: ", cursor.getCount()+"");
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
            //favMovie = convertCursorDataToHashMapMovie(cursor);
            selectedFavMovie = convertCursorDataToHashMapMovie(cursor);
            //movieBundle.putSerializable("selectedMovie", favMovie);
            intent.putExtra("selectedMovie", selectedFavMovie);
            movieBundle.putBoolean("isFav", true);
        }else{
            //movieBundle.putSerializable("selectedMovie", movieList.get(clickedItem));
            //movieBundle.putParcelable("selectedMovie", newMovieList.get(clickedItem));
            intent.putExtra("selectedMovie", newMovieList.get(clickedItem));
            Log.e(TAG, "add movie here into intent. Movie poster path is " + newMovieList.get(clickedItem).getmPosterPath());
            movieBundle.putBoolean("isFav", false);
        }

        intent.putExtras(movieBundle);
        MainActivity.this.startActivity(intent);
        Log.d(TAG, "exiting onListItemClick");
    }

    private Movie convertCursorDataToHashMapMovie(Cursor cursor) {
//        HashMap<String, String> movie = new HashMap<>();
//
//        cursor.moveToFirst();
//        movie.put("title", cursor.getString(1));
//        movie.put("voteAverage", cursor.getString(2));
//        movie.put("releaseDate", cursor.getString(3));
//        movie.put("overview", cursor.getString(4));
//        movie.put("trailer", cursor.getString(5));
//        movie.put("review", cursor.getString(6));
//        movie.put("movieId", cursor.getString(7));
//        movie.put("posterPath", cursor.getString(7));
//        cursor.close();
//        return movie;

        Movie movie = new Movie();

        cursor.moveToFirst();
        movie.setmTitle(cursor.getString(1));
        movie.setmRating(cursor.getString(2));
        movie.setmYear(cursor.getString(3));
        movie.setmSummary(cursor.getString(4));
        movie.setmTrailer(cursor.getString(5));
        movie.setmReview(cursor.getString(6));
        movie.setmMovieId(cursor.getString(7));
        movie.setmPosterPath(cursor.getString(7));
        cursor.close();
        return movie;
    }

    private Cursor getClickedMovieData(int clickedItem) {
        try{
            Uri uri = FavMoviesContract.FavMovieEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(clickedItem+"").build();
            return getContentResolver().query(uri,
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
                break;
            case R.id.menu_highest_rated:
                if(mMenuState != MenuState.MENU_HIGHEST_RATED) showHighestRated();
                break;
            case R.id.menu_favourites:
                showFavourites();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFavourites() {
        mMenuState = MenuState.MENU_FAV;
        txtNoNetworkMessage.setVisibility(View.INVISIBLE);
        Cursor cursor = getAllFavMovies();
        mMovieAdapter = new MovieAdapter(this, cursor, this);
        mRecyclerView.setAdapter(mMovieAdapter);
        Toast.makeText(this, "show favs", Toast.LENGTH_SHORT).show();
    }

    private void showHighestRated() {
        Log.d(TAG, "entering showHighestRated");
        mMenuState = MenuState.MENU_HIGHEST_RATED;
        if(isNetworkAvailable()){
            loadMovieList(getString(R.string.highestRated));
        }else{
            txtNoNetworkMessage.setVisibility(View.VISIBLE);
            mRecyclerView.setAdapter(null);
        }
    }

    private void showMostPopular() {
        Log.d(TAG, "entering showMostPopular");
        mMenuState = MenuState.MENU_MOST_POPULAR;
        if(isNetworkAvailable()){
            loadMovieList(getString(R.string.mostPopular));
        }else{
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
                String theMovieDbMostPopularQueryString = args.getString("theMovieDbmostPopularQuery");
                String theMovieDbHighestRatedQueryString = args.getString("theMovieDbhighestRatedQuery");

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
                Log.d(TAG, " £££££££££   entering getAllPosterPathsAndMovieDataFromJson ££££££££");
                posterPaths = new String[20];
                if (stringResponseFromRequest != null && !stringResponseFromRequest.equals("")) {
                    Log.d(TAG, stringResponseFromRequest);
                    JSONObject jsonObject = JsonUtils.getJSONObject(stringResponseFromRequest);
                    JSONArray jsonMoviesArray = JsonUtils.getJSONArray(jsonObject, "results");
                    String[] moviesResult = new String[jsonMoviesArray.length()];
                    int next = 0;
                    for(String movie : moviesResult){
                        JSONObject nextMovie = JsonUtils.getJSONObject(jsonMoviesArray, next);
                        posterPaths[next] = JsonUtils.getString(nextMovie, "poster_path");
                        Log.d(TAG, posterPaths[next]);
                        posterPaths[next] = "https://image.tmdb.org/t/p/w500/" + posterPaths[next];     //other poster sizes are w92, w154, w185, w342, w500, w780 or original
                        getAllMovieData(nextMovie,  posterPaths[next]);
                        ++next;
                    }
                    Log.d(TAG, "exiting onLoadFinished");
                } else {
                    Log.e(TAG, "empty data back from themoviedb api call");
                }
            }

            //TODO finished off here. need to set no review string for movies in first 2 lists - working for fav list
            private void getAllMovieData(JSONObject clickedMovie, String posterPath) {
                Log.d(TAG, "ATL entering getAllMovieData");
                HashMap movieMap = new HashMap();
                Movie movie = new Movie(JsonUtils.getString(clickedMovie, "original_title"),
                                        JsonUtils.getString(clickedMovie, "vote_average"),
                                        JsonUtils.getString(clickedMovie, "release_date"),
                                        JsonUtils.getString(clickedMovie, "overview"),
                                        "there are no reviews for this movie yet",
                                        "no trailer key yet",
                                        JsonUtils.getString(clickedMovie, "id"),
                                        posterPath);
//                movieMap.put("title", JsonUtils.getString(clickedMovie, "original_title"));
//                movieMap.put("overview", JsonUtils.getString(clickedMovie, "overview"));
//                movieMap.put("releaseDate", JsonUtils.getString(clickedMovie, "release_date"));
//                movieMap.put("posterPath", JsonUtils.getString(clickedMovie, "poster_path"));
//                movieMap.put("voteAverage", JsonUtils.getString(clickedMovie, "vote_average"));
//                movieMap.put("movieId", JsonUtils.getString(clickedMovie, "id"));
                //movieList.add(movieMap);
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

    private Cursor showSelectedFavMovie(){
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
