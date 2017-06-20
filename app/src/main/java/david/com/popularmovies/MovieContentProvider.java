package david.com.popularmovies;

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

/**
 * Created by David on 20-Jun-17.
 */

public class MovieContentProvider extends ContentProvider {

    private FavMoviesDbHelper mFavMovieDbHelper;

    public static final int FAV_MOVIES = 100;
    public static final int MOVIE_WITH_ID = 101;

    public static final String TABLE_NAME = "favMovies";

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    //define a static buildUriMatcher method that associates Uris with their int match
    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        //add matches with addUri(String authority, String path, int code)
        uriMatcher.addURI(FavMoviesContract.AUTHORITY, FavMoviesContract.PATH_FAV_MOVIES, FAV_MOVIES);
        uriMatcher.addURI(FavMoviesContract.AUTHORITY, FavMoviesContract.PATH_FAV_MOVIES + "/#",MOVIE_WITH_ID );

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
        return null;
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
                    throw new SQLException("failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
