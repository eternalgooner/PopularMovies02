package david.com.popularmovies.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by User on 6/6/2017.
 */
//TODO SUGGESTION This 'User' fella is a prolific programmer, I see his code everywhere, or is he taking credit for your work :^)

public class FavMoviesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favMovies2.db";
    private static final int DATABASE_VERSION = 1;
    private static final String DROP_TABLE_IF_EXISTS_ = "DROP TABLE IF EXISTS ";
    private static final String CREATE_TABLE_ = "CREATE TABLE ";
    private static final String _OPEN_BRACKET = " (";
    private static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT_COMMA = " INTEGER PRIMARY KEY AUTOINCREMENT,";
    private static final String _TEXT_NOT_NULL_COMMA = " TEXT NOT NULL,";
    private static final String _TEXT_NOT_NULL = " TEXT NOT NULL";
    private static final String CLOSE_BRACKET_SEMI_COLON =  ");";

    public FavMoviesDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAV_MOVIES_TABLE = CREATE_TABLE_ +
                FavMoviesContract.FavMovieEntry.TABLE_NAME + _OPEN_BRACKET +
                FavMoviesContract.FavMovieEntry._ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT_COMMA +
                FavMoviesContract.FavMovieEntry.COLUMN_TITLE + _TEXT_NOT_NULL_COMMA +
                FavMoviesContract.FavMovieEntry.COLUMN_RATING + _TEXT_NOT_NULL_COMMA +
                FavMoviesContract.FavMovieEntry.COLUMN_YEAR + _TEXT_NOT_NULL_COMMA +
                FavMoviesContract.FavMovieEntry.COLUMN_SUMMARY + _TEXT_NOT_NULL_COMMA +
                FavMoviesContract.FavMovieEntry.COLUMN_TRAILER + _TEXT_NOT_NULL_COMMA +
                FavMoviesContract.FavMovieEntry.COLUMN_REVIEW + _TEXT_NOT_NULL_COMMA +
                FavMoviesContract.FavMovieEntry.COLUMN_MOVIE_ID + _TEXT_NOT_NULL_COMMA +
                FavMoviesContract.FavMovieEntry.COLUMN_POSTER_PATH + _TEXT_NOT_NULL +
                CLOSE_BRACKET_SEMI_COLON;

        db.execSQL(SQL_CREATE_FAV_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_IF_EXISTS_ + FavMoviesContract.FavMovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
