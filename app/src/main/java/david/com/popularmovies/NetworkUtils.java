package david.com.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


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
    private static Context context;
    private static String base_url_popular;
    private static String base_url_top_rated;
    private static String base_url_trailers;
    private static String base_url_reviews;
    private static String apiKey;

    private static void initData() {
        apiKey = getKey();
        base_url_popular = context.getString(R.string.base_url_popular) + apiKey;
        base_url_top_rated = context.getString(R.string.base_url_top_rated) + apiKey;
        base_url_trailers = context.getString(R.string.base_url);
        base_url_reviews = context.getString(R.string.base_url);
    }

    private static String getKey() {
        String apiKey = BuildConfig.MY_MOVIEDB_API_KEY;
        return apiKey;
    }

    public static URL buildUrl(String sortType, Context context, String id){
        Log.d(TAG, "entering buildUrl");
        NetworkUtils.context = context;
        initData();
        String sortParam = "";

        if(sortType.equals("mostPopular")){
            Log.d(TAG, "match found: mostPopular");
            sortParam = base_url_popular;
        }else if(sortType.equals("highestRated")){
            Log.d(TAG, "match found: highestRated");
            sortParam = base_url_top_rated;
        }else if(sortType.equals("videos")){
            Log.d(TAG, "match found: videos");
            sortParam = base_url_trailers + id + "/videos?api_key=" + apiKey;
        }else if(sortType.equals("reviews")){
            Log.d(TAG, "match found: reviews");
            sortParam = base_url_reviews + id + "/reviews?api_key=" + apiKey;
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