package david.com.popularmovies.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by David on 20-May-17.
 *
 * Utilities class to work with JSON data
 *
 * - string literals have not been put into the strings.xml file as they are not user-facing
 */

public class JsonUtils {
    private static final String TAG = JsonUtils.class.getSimpleName();

    public static JSONObject getJSONObject(String jsonString){
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, " error parsing in getJSONObject(String jsonString)");
        }
        return jsonObject;
    }

    public static JSONObject getJSONObject(JSONArray array, int position){
        JSONObject jsonObject = null;
        try {
            jsonObject = array.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, " error parsing in getJSONObject(JSONArray array, int position)");
        }
        return jsonObject;
    }

    public static String getString(JSONObject object, String key){
        String result = null;
        try {
            result = object.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, " error parsing in getString(JSONObject object, String key)" + object + " : " + key);
        }
        return result;
    }

    public static JSONArray getJSONArray(JSONObject jsonObject, String key){
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = jsonObject.getJSONArray(key);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, " error parsing in getJSONArray(JSONObject jsonObject, String key)" + jsonObject + " : " + key);
        }
        return jsonArray;
    }
}
