package david.com.popularmovies.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import david.com.popularmovies.db.FavMoviesContract;

/**
 * Created by David on 08-Jul-17.
 */

public class InsertOrDeleteFromDbService extends IntentService {
    private static final int INSERT_TO_DATABASE = 1;
    private static final int DELETE_FROM_DATABSE = -1;
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
        super("InsertOrDeleteFromDbService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String movieId = intent.getStringExtra("movieId");
        int actionToTake = intent.getIntExtra("dbAction", 0);
        Log.d(TAG, "entering onHandleRequest, movie id is: " + movieId + " AND actionToTake is: " + actionToTake);

        if(actionToTake == INSERT_TO_DATABASE){
            insertToDatabase(movieId);
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
        //int transactionResult = getContentResolver().delete(uri, "movieId", movieIds);
        getContentResolver().delete(uri, "movieId", movieIds);
    }

    private void insertToDatabase(String movieId) {

    }
}
