package david.com.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * class that starts the application
 * - has inner class TheMovieDbTask
 *
 * - display message if no network connection
 *
 * - if there is a network connection it will:
 *      - build a URL
 *      - pass the URL to AsyncTask to retrieve JSON data from themoviedb
 *      - JSON data is then stored for each movie in a HashMap, which is then added to an ArrayList of movies
 *      - display movie posters in a grid layout
 *
 * UI:
 * - create RecyclerView, GridLayoutManager & Adapter for displaying scrolling list
 * - create menu option to sort by highest rated or most popular
 * - upon poster click, new activity should show clicked movie details
 *
 * STRING LITERALS:
 * - string literals have not been put into the strings.xml file as they are not user-facing
 *
 * ATTRIBUTION:
 * - some code was implemented with help from Udacity Android course
 *
 * INFO:
 * you need to supply your own API key to retrieve data from themoviedb (API key is used in NetworkUtils class)
 */

public class MainActivity extends AppCompatActivity implements MovieAdapter.ListItemClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView txtNoNetworkMessage;
    private static final int NUM_LIST_ITEMS = 20;
    private MovieAdapter mMovieAdapter;
    private RecyclerView mRecyclerView;
    private String[] posterPaths;
    private ArrayList<HashMap> movieList;
    private GridLayoutManager gridLayoutManager;
    private boolean showingMostPopular = true;
    private Bundle movieBundle = new Bundle();
    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "entering onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_moviePosters);

        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            gridLayoutManager = new GridLayoutManager(this, 3);
        }else{
            gridLayoutManager = new GridLayoutManager(this, 4);
        }
        mRecyclerView.setLayoutManager(gridLayoutManager);
        txtNoNetworkMessage = (TextView) findViewById(R.id.message_no_network_connection);

        if(isNetworkAvailable()){
            loadMovieList("mostPopular");
        }else{
            txtNoNetworkMessage.setVisibility(View.VISIBLE);
        }

        FavMoviesDbHelper dbHelper = new FavMoviesDbHelper(this);
        mDb = dbHelper.getReadableDatabase();

        Log.d(TAG, "exiting onCreate");
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
        //mRecyclerView = (RecyclerView) findViewById(R.id.rv_moviePosters);
        //gridLayoutManager = new GridLayoutManager(this, 3);
        //mRecyclerView.setLayoutManager(gridLayoutManager);
        mMovieAdapter = new MovieAdapter(posterPaths, NUM_LIST_ITEMS, this);
        mRecyclerView.setAdapter(mMovieAdapter);
        Log.d(TAG, "exiting showMovies");
    }

    private void loadMovieList(String sortType) {
        Log.d(TAG, "entering loadMovieList");
        URL myUrl = NetworkUtils.buildUrl(sortType, getApplicationContext(), "0");
        new TheMovieDbTask().execute(myUrl);
        Log.d(TAG, "exiting loadMovieList");
    }

    @Override
    public void onListItemClick(int clickedItem) {
        Log.d(TAG, "entering onListItemClick");
        Intent intent = new Intent(MainActivity.this, MovieDetailsActivity.class);
        movieBundle.putSerializable("selectedMovie", movieList.get(clickedItem));
        intent.putExtras(movieBundle);
        MainActivity.this.startActivity(intent);
        Log.d(TAG, "exiting onListItemClick");
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
                if(!showingMostPopular) showMostPopular();
                break;
            case R.id.menu_highest_rated:
                if(showingMostPopular) showHighestRated();
                break;
            case R.id.menu_favourites:
                showFavourites();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFavourites() {
        //TODO implement
        Cursor cursor = getAllMovies();
        mMovieAdapter = new MovieAdapter(this, cursor);
        mRecyclerView.setAdapter(mMovieAdapter);
        Toast.makeText(this, "show favs", Toast.LENGTH_SHORT).show();
    }

    private void showHighestRated() {
        showingMostPopular = false;
        loadMovieList("highestRated");
    }

    private void showMostPopular() {
        showingMostPopular = true;
        loadMovieList("mostPopular");
    }

    public class TheMovieDbTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            movieList = new ArrayList<>();
        }

        @Override
        protected String doInBackground(URL... params) {
            Log.d(TAG, "entering doInBackground");
            URL requestPopularMoviesUrl = params[0];
            String theMovieDbResult = null;
            try {
                theMovieDbResult = NetworkUtils.getResponseFromHttpUrl(requestPopularMoviesUrl);
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
            posterPaths = new String[20];
            if (theMovieDbSearchResults != null && !theMovieDbSearchResults.equals("")) {
                Log.d(TAG, theMovieDbSearchResults);
                JSONObject jsonObject = JsonUtils.getJSONObject(theMovieDbSearchResults);
                JSONArray jsonMoviesArray = JsonUtils.getJSONArray(jsonObject, "results");
                String[] moviesResult = new String[jsonMoviesArray.length()];
                int next = 0;
                for(String movie : moviesResult){
                    JSONObject nextMovie = JsonUtils.getJSONObject(jsonMoviesArray, next);
                    posterPaths[next] = JsonUtils.getString(nextMovie, "poster_path");
                    Log.d(TAG, posterPaths[next]);
                    posterPaths[next] = "https://image.tmdb.org/t/p/w500/" + posterPaths[next];     //other poster sizes are w92, w154, w185, w342, w500, w780 or original
                    getAllMovieData(nextMovie);
                    ++next;
                }
                showMovies();
                Log.d(TAG, "exiting onPostExecute");
            } else {
                Log.d(TAG, "empty data back from themoviedb api call");
            }
        }

        private void getAllMovieData(JSONObject clickedMovie) {
            Log.d(TAG, "entering getAllMovieData");
            HashMap movieMap = new HashMap();
            movieMap.put("title", JsonUtils.getString(clickedMovie, "original_title"));
            movieMap.put("overview", JsonUtils.getString(clickedMovie, "overview"));
            movieMap.put("releaseDate", JsonUtils.getString(clickedMovie, "release_date"));
            movieMap.put("posterPath", JsonUtils.getString(clickedMovie, "poster_path"));
            movieMap.put("voteAverage", JsonUtils.getString(clickedMovie, "vote_average"));
            movieMap.put("id", JsonUtils.getString(clickedMovie, "id"));
            movieList.add(movieMap);
            Log.d(TAG, "exiting getAllMovieData");
        }
    }
    private Cursor getAllMovies(){
        return mDb.query(FavMoviesContract.FavMovieEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
