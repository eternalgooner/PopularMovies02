package david.com.popularmovies;

import android.provider.BaseColumns;

/**
 * Created by User on 6/6/2017.
 */

public class FavMoviesContract {


    public static final class FavMovieEntry implements BaseColumns{
        public final static String TABLE_NAME = "favMovies";
        public final static String COLUMN_TITLE = "title";
        public final static String COLUMN_RATING = "rating";
        public final static String COLUMN_YEAR = "year";
        public final static String COLUMN_SUMMARY = "summary";
        public final static String COLUMN_TRAILER = "trailer";
        public final static String COLUMN_REVIEW = "review";
    }
}
