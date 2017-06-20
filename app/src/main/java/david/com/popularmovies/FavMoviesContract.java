package david.com.popularmovies;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by User on 6/6/2017.
 */

public class FavMoviesContract {

    //the authority, which is how my code knows which Content Provider to access
    public static final String AUTHORITY = "david.com.popularmovies";

    //the base content Uri = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    //possible paths for accessing data in the contract
    public static final String PATH_FAV_MOVIES = "favmovies";


    //FavMovie is an inner class that defines the contents of the fav movie table
    public static final class FavMovieEntry implements BaseColumns{

        //FavMovie content Uri = base content Uri + path
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAV_MOVIES).build();

        public final static String TABLE_NAME = "favMovies";
        public final static String COLUMN_TITLE = "title";
        public final static String COLUMN_RATING = "rating";
        public final static String COLUMN_YEAR = "year";
        public final static String COLUMN_SUMMARY = "summary";
        public final static String COLUMN_TRAILER = "trailer";
        public final static String COLUMN_REVIEW = "review";
    }
}
