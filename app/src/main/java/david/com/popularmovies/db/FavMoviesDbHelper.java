package david.com.popularmovies.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by User on 6/6/2017.
 */

public class FavMoviesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favMovies.db";
    private static final int DATABASE_VERSION = 1;

    public FavMoviesDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAV_MOVIES_TABLE = "CREATE TABLE " +
                FavMoviesContract.FavMovieEntry.TABLE_NAME + " (" +
                FavMoviesContract.FavMovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavMoviesContract.FavMovieEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                FavMoviesContract.FavMovieEntry.COLUMN_RATING + " TEXT NOT NULL," +
                FavMoviesContract.FavMovieEntry.COLUMN_YEAR + " TEXT NOT NULL," +
                FavMoviesContract.FavMovieEntry.COLUMN_SUMMARY + " TEXT NOT NULL," +
                FavMoviesContract.FavMovieEntry.COLUMN_TRAILER + " TEXT NOT NULL," +
                FavMoviesContract.FavMovieEntry.COLUMN_REVIEW + " TEXT NOT NULL" +
                ");";

        db.execSQL(SQL_CREATE_FAV_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavMoviesContract.FavMovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
