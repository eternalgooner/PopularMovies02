package david.com.popularmovies.utils;

import android.database.Cursor;

import java.util.ArrayList;

import david.com.popularmovies.model.Movie;

/**
 * Created by David on 09-Jul-17.
 */

public class CursorUtils {

    public static ArrayList<Movie> convertCursorToArrayListOfMovie(Cursor cursor){
        ArrayList<Movie> movieList = new ArrayList<>();
        while (cursor.moveToNext()){
            movieList.add(new Movie(cursor.getString(1),
                                    cursor.getString(2),
                                    cursor.getString(3),
                                    cursor.getString(4),
                                    cursor.getString(5),
                                    cursor.getString(6),
                                    cursor.getString(7),
                                    cursor.getString(8)));
        }
        //cursor.close();
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
        //cursor.close();

        return movie;
    }
}
