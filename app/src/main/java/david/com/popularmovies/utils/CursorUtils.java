package david.com.popularmovies.utils;

import android.database.Cursor;

import java.util.ArrayList;

import david.com.popularmovies.model.Movie;

/**
 * Created by David on 09-Jul-17.
 */

public class CursorUtils {
    private static final int TITLE = 1;
    private static final int RATING = 2;
    private static final int YEAR = 3;
    private static final int SUMMARY = 4;
    private static final int TRAILER = 5;
    private static final int REVIEW = 6;
    private static final int MOVIE_ID = 7;
    private static final int POSTER_PATH = 8;

    public static ArrayList<Movie> convertCursorToArrayListOfMovie(Cursor cursor){
        ArrayList<Movie> movieList = new ArrayList<>();

        while (cursor.moveToNext()){
            movieList.add(new Movie(cursor.getString(TITLE),
                                    cursor.getString(RATING),
                                    cursor.getString(YEAR),
                                    cursor.getString(SUMMARY),
                                    cursor.getString(TRAILER),
                                    cursor.getString(REVIEW),
                                    cursor.getString(MOVIE_ID),
                                    cursor.getString(POSTER_PATH)));
        }
        return movieList;
    }

    public static Movie convertCursorToMovieObject(Cursor cursor) {
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

        return movie;
    }
}
