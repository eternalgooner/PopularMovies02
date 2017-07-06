package david.com.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by David on 25-Jun-17.
 *
 * Movie class that represents a movie object
 *
 * - implements Parcelable to allow to pass into Intent as a Movie object
 */

//TODO need to implement and use in code
public class Movie implements Parcelable {

    private String mTitle;
    private String mRating;
    private String mYear;
    private String mSummary;
    private String mTrailer;
    private String mReview;
    private String mMovieId;
    private String mPosterPath;

    public Movie(){

    }

    public Movie(String mTitle, String mRating, String mYear, String mSummary, String mTrailer, String mReview, String mMovieId, String mPosterPath) {
        this.mTitle = mTitle;
        this.mRating = mRating;
        this.mYear = mYear;
        this.mSummary = mSummary;
        this.mTrailer = mTrailer;
        this.mReview = mReview;
        this.mMovieId = mMovieId;
        this.mPosterPath = mPosterPath;
    }

    public Movie(Parcel in) {
        this.mTitle = in.readString();
        this.mRating = in.readString();
        this.mYear = in.readString();
        this.mSummary = in.readString();
        this.mTrailer = in.readString();
        this.mReview = in.readString();
        this.mMovieId = in.readString();
        this.mPosterPath = in.readString();
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
        dest.writeString(mTitle);
        dest.writeString(mRating);
        dest.writeString(mYear);
        dest.writeString(mSummary);
        dest.writeString(mTrailer);
        dest.writeString(mReview);
        dest.writeString(mMovieId);
        dest.writeString(mPosterPath);
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmRating() {
        return mRating;
    }

    public void setmRating(String mRating) {
        this.mRating = mRating;
    }

    public String getmYear() {
        return mYear;
    }

    public void setmYear(String mYear) {
        this.mYear = mYear;
    }

    public String getmSummary() {
        return mSummary;
    }

    public void setmSummary(String mSummary) {
        this.mSummary = mSummary;
    }

    public String getmTrailer() {
        return mTrailer;
    }

    public void setmTrailer(String mTrailer) {
        this.mTrailer = mTrailer;
    }

    public String getmReview() {
        return mReview;
    }

    public void setmReview(String mReview) {
        this.mReview = mReview;
    }

    public String getmMovieId() {
        return mMovieId;
    }

    public void setmMovieId(String mMovieId) {
        this.mMovieId = mMovieId;
    }

    public String getmPosterPath() {
        return mPosterPath;
    }

    public void setmPosterPath(String mPosterPath) {
        this.mPosterPath = mPosterPath;
    }
}
