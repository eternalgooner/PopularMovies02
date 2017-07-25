package david.com.popularmovies.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import david.com.popularmovies.BuildConfig;
import david.com.popularmovies.R;


/**
 * Created by David on 11-May-17.
 *
 * Class info:
 * - builds Uri with API key
 * - returns a URL to caller
 * - makes http request
 * - retrieves JSON data as String & returns it to caller
 *
 * STRING LITERALS:
 * - string literals have not been put into the strings.xml file as they are not user-facing
 *
 * ATTRIBUTION:
 * - some code was implemented with help from Udacity Android course
 *
 *
 */

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static String base_url_popular = "https://api.themoviedb.org/3/movie/popular?api_key=" + getKey();
    private static String base_url_top_rated = "https://api.themoviedb.org/3/movie/top_rated?api_key=" + getKey();
    private static String base_url_trailers = "https://api.themoviedb.org/3/movie/";
    private static String base_url_reviews = "https://api.themoviedb.org/3/movie/";
    private static final String MOST_POPULAR = "mostPopular";
    private static final String HIGHEST_RATED = "highestRated";
    private static final String VIDEOS = "videos";
    private static final String VIDEO_URL = "/videos?api_key=";
    private static final String REVIEW_URL = "/reviews?api_key=";

    private static String getKey() {
        String apiKey = BuildConfig.MY_MOVIEDB_API_KEY;
        return apiKey;
    }

    public static URL buildUrl(String sortType, String id){
        Log.d(TAG, "NU entering buildUrl");
        String sortParam = null;

        //TODO SUGGESTION Consider using a switch statement instead of if...
        if(sortType.equals(MOST_POPULAR)){
            Log.d(TAG, "match found: mostPopular");
            sortParam = base_url_popular;
        }else if(sortType.equals(HIGHEST_RATED)){
            Log.d(TAG, "match found: highestRated");
            sortParam = base_url_top_rated;
        }else if(sortType.equals(VIDEOS)){
            Log.d(TAG, "match found: videos");
            sortParam = base_url_trailers + id + VIDEO_URL + getKey();
        }else if(sortType.equals("reviews")){
            Log.d(TAG, "match found: reviews");
            sortParam = base_url_reviews + id + REVIEW_URL + getKey();
            Log.d(TAG, "sorParam being used is: " + sortParam);
        }

        Uri theMovieDbUri = Uri.parse(sortParam).buildUpon().build();
        URL theMoveDbUrl = null;

        try {
            theMoveDbUrl = new URL(theMovieDbUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return theMoveDbUrl;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
       Log.d(TAG, "NU entering getResponseFromHttpUrl");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();

            if(hasInput){
                return scanner.next();
            }else{
                return  null;
            }
        }finally {
            urlConnection.disconnect();
        }
    }
}