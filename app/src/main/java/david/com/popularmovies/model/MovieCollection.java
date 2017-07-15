package david.com.popularmovies.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import david.com.popularmovies.R;

/**
 * Created by David on 14-Jul-17.
 *
 * Class that holds & gives each list of movie objects - (i) Most Popular, (ii) Highest Rated & (iii) Favourites
 *
 * - used so that the 1st time the loader is called, the results are stored in this class, available
 * for retrieval to eliminate any unnecessary calls over the network
 */

public class MovieCollection implements Parcelable{
    private HashMap<String, List> movieCollection;
    private Context context;

    public MovieCollection(Context context){
        this.context = context;
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

    public ArrayList getMostPopular(){
        return (ArrayList) movieCollection.get(context.getString(R.string.mostPopular));
    }

    public ArrayList getHighestRated(){
        return (ArrayList) movieCollection.get(context.getString(R.string.highestRated));
    }

    public ArrayList getFavourites(){
        return (ArrayList) movieCollection.get(context.getString(R.string.favourites));
    }

    public void setMostPopular(List mostPopular){
        movieCollection.put(String.valueOf(context.getString(R.string.mostPopular)), mostPopular);
    }

    public void setHighestRated(List highestRated){
        movieCollection.put(context.getString(R.string.highestRated), highestRated);
    }

    public void setFavourites(List favourites){
        movieCollection.put(context.getString(R.string.favourites), favourites);
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
