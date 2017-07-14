package david.com.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by David on 14-Jul-17.
 */

public class MovieCollection implements Parcelable{

    //private List<Movie> mostPopularMovieList;
    //private List<Movie> highestRatedMovieList;
    //private List<Movie> favouriteMovieList;
    private HashMap<String, List> movieCollection;

    public MovieCollection(){
//        mostPopularMovieList = new ArrayList<>();
//        highestRatedMovieList = new ArrayList<>();
//        favouriteMovieList = new ArrayList<>();
        movieCollection = new HashMap<String, List>();
    }

    protected MovieCollection(Parcel in) {
        in.readMap(movieCollection , List.class.getClassLoader());
    }

    public static final Creator<MovieCollection> CREATOR = new Creator<MovieCollection>() {
        @Override
        public MovieCollection createFromParcel(Parcel in) {
            return new MovieCollection(in);
        }

        @Override
        public MovieCollection[] newArray(int size) {
            return new MovieCollection[size];
        }
    };

    public void add(List<Movie> movieList, String movieListDescription){
        movieCollection.put(movieListDescription, movieList);
    }

    public ArrayList get(String movieList){
        return (ArrayList) movieCollection.get(movieList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeMap(movieCollection);
    }
}
