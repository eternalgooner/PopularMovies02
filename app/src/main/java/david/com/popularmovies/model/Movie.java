package david.com.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by David on 25-Jun-17.
 */

//TODO need to implement and use in code
public class Movie implements Parcelable {
    protected Movie(Parcel in) {
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
