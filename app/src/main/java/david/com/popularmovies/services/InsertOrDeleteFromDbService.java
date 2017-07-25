package david.com.popularmovies.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import david.com.popularmovies.db.FavMoviesContract;
import david.com.popularmovies.model.Movie;

/**
 * Created by David on 08-Jul-17.
 *
 * class that starts a service to either insert or delete from a DB
 */

public class InsertOrDeleteFromDbService extends IntentService {
    //TODO AWESOME  It's great you're using the different components the framework provides, an IntentService leveraging a Content Provider
    private static final int INSERT_TO_DATABASE = 1;
    private static final int DELETE_FROM_DATABSE = -1;
    private static final String INSERT_OR_DELETE_FROM_DB = "InsertOrDeleteFromDbService";
    private static final String MOVIE_ID = "movieId";
    private static final String SELECTED_MOVIE = "selectedMovie";
    private static final String DB_ACTION = "dbAction";
    private static final String TAG = InsertOrDeleteFromDbService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public InsertOrDeleteFromDbService(String name) {
        super(name);
    }

    public InsertOrDeleteFromDbService() {
        super(INSERT_OR_DELETE_FROM_DB);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String movieId = intent.getStringExtra(MOVIE_ID);
        Movie selectedMovie = intent.getParcelableExtra(SELECTED_MOVIE);
        int actionToTake = intent.getIntExtra(DB_ACTION, 0);
        Log.d(TAG, "entering onHandleRequest, movie id is: " + movieId + " AND actionToTake is: " + actionToTake);

        if(actionToTake == INSERT_TO_DATABASE){
            insertToDatabase(selectedMovie);
        }else if(actionToTake == DELETE_FROM_DATABSE){
            deleteFromDatabase(movieId);
        }
    }

    private void deleteFromDatabase(String movieId) {
        String[] movieIds = new String[1];
        movieIds[0] = movieId;
        Log.d(TAG, "entering deleteFromDatabase() method with movie id: " + movieId);
        Uri uri = FavMoviesContract.FavMovieEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(movieId).build();
        getContentResolver().delete(uri, MOVIE_ID, movieIds);
    }

    private void insertToDatabase(Movie movie) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FavMoviesContract.FavMovieEntry.COLUMN_TITLE, movie.getmTitle());
        Log.d(TAG, "in addMovieToFav() method, adding movie: " + movie.getmTitle() + " and ID is: " + movie.getmMovieId());
        contentValues.put(FavMoviesContract.FavMovieEntry.COLUMN_RATING, movie.getmRating());
        contentValues.put(FavMoviesContract.FavMovieEntry.COLUMN_YEAR, movie.getmYear());
        contentValues.put(FavMoviesContract.FavMovieEntry.COLUMN_SUMMARY, movie.getmSummary());
        contentValues.put(FavMoviesContract.FavMovieEntry.COLUMN_TRAILER, movie.getmReview());
        contentValues.put(FavMoviesContract.FavMovieEntry.COLUMN_REVIEW, movie.getmTrailer());
        contentValues.put(FavMoviesContract.FavMovieEntry.COLUMN_MOVIE_ID, movie.getmMovieId());
        contentValues.put(FavMoviesContract.FavMovieEntry.COLUMN_POSTER_PATH, movie.getmPosterPath());

        getContentResolver().insert(FavMoviesContract.FavMovieEntry.CONTENT_URI, contentValues);
    }
}
