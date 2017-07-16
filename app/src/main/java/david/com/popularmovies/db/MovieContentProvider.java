package david.com.popularmovies.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import david.com.popularmovies.db.FavMoviesContract;
import david.com.popularmovies.db.FavMoviesDbHelper;

/**
 * Created by David on 20-Jun-17.
 */

public class MovieContentProvider extends ContentProvider {

    private FavMoviesDbHelper mFavMovieDbHelper;
    private static final String TAG = MovieContentProvider.class.getSimpleName();
    public static final int FAV_MOVIES = 100;
    public static final int MOVIE_WITH_ID = 101;
    private static final String FORWARD_SLASH_HASH = "/#";
    private static final String SELECT_ALL_FROM_FAV_MOVIES_WHERE_MOVIE_ID_EQUALS = "SELECT * FROM favMovies WHERE movieId=?";
    private static final String UNKNOWN_URI = "Unknown Uri: ";
    private static final String FAILED_TO_INSERT_ROW_INTO_ = "failed to insert row into ";
    public static final String TABLE_NAME = "favMovies";
    private static final String MOVIE_ID_EQUALS = "movieId = ?";
    private static final String MATCH_IS = ". Match is: ";

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    //define a static buildUriMatcher method that associates Uris with their int match
    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        //add matches with addUri(String authority, String path, int code)
        uriMatcher.addURI(FavMoviesContract.AUTHORITY, FavMoviesContract.PATH_FAV_MOVIES, FAV_MOVIES);
        uriMatcher.addURI(FavMoviesContract.AUTHORITY, FavMoviesContract.PATH_FAV_MOVIES + FORWARD_SLASH_HASH, MOVIE_WITH_ID );

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mFavMovieDbHelper = new FavMoviesDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mFavMovieDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match){
            case FAV_MOVIES:
                Log.d(TAG, " match found for FAV_MOVIES in DB query method");
                retCursor = db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIE_WITH_ID:
                Log.d(TAG, " match found for MOVIE_WITH_ID in DB query method");
                Log.d(TAG, "selection is: " + selection + " AND selArgs is: " + selectionArgs[0]);
                String id = uri.getLastPathSegment();
                String[] mSelectionArgs = new String[]{id};
                retCursor = db.rawQuery(SELECT_ALL_FROM_FAV_MOVIES_WHERE_MOVIE_ID_EQUALS, selectionArgs);
                retCursor.moveToFirst();
                break;

            default:
                throw new UnsupportedOperationException(UNKNOWN_URI + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mFavMovieDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        Uri returnUri;

        switch (match){
            case FAV_MOVIES:
                //inserting values into table
                long id = db.insert(TABLE_NAME, null, values);
                if(id > 0){
                    returnUri = ContentUris.withAppendedId(FavMoviesContract.FavMovieEntry.CONTENT_URI, id);
                }else{
                    throw new SQLException(FAILED_TO_INSERT_ROW_INTO_ + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException(UNKNOWN_URI + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.d(TAG, "entering delete method in ContentProvider");
        final SQLiteDatabase db = mFavMovieDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int transactionResult = 0;
        String whereIdEquals = MOVIE_ID_EQUALS;
        String[] movieId = selectionArgs;

        switch (match){
            case MOVIE_WITH_ID:
                Log.d(TAG, "found match in delete method in ContentProvider");
                //delete movie from table
                transactionResult = db.delete(TABLE_NAME, whereIdEquals, movieId);
                break;
            default:
                transactionResult = -1;
                throw new UnsupportedOperationException(UNKNOWN_URI + uri + MATCH_IS + match);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return transactionResult;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
