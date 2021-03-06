package org.weibeld.simpletweets.misc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.weibeld.simpletweets.R;
import org.weibeld.simpletweets.models.User;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;

/**
 * Created by dw on 28/02/17.
 */

public class Util {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // Checks if the app has permission to write to device storage. If the app does not has
    // permission then the user will be prompted to grant permissions
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    // Write a string to the specified file in the public Downloads directory
    public static void writeToFile(String filename, String str) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(dir, filename);
        Log.v(LOG_TAG, "Writing file " + file.getAbsolutePath());
        try {
            FileUtils.writeStringToFile(file, str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }

    public static void toast(Activity a, String msg) {
        Toast.makeText(a, msg, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(Activity a, String msg) {
        Toast.makeText(a, msg, Toast.LENGTH_LONG).show();
    }

    // TODO: find a better way to test Internet connection, because in some networks pings are blocked (e.g. American Consulate)
    // Check if the device is connected to the internet by pinging a known server
    public static boolean hasInternetConnection() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException | InterruptedException e) { e.printStackTrace(); }
        return false;
    }

    // Check if the device has an active network interface. This is NOT the case if the device has
    // mobile date turned off or is in airplane mode (not sure about the case when the device has
    // its mobile data quota exceeded or when it's out of mobile network coverage).
    public static boolean hasActiveNetworkInterface(Context context) {
        ConnectivityManager c = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = c.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static void showIfHidden(View v) {
        if (v.getVisibility() == View.GONE) v.setVisibility(View.VISIBLE);
    }

    public static void hideIfShown(View v) {
        if (v.getVisibility() == View.VISIBLE) v.setVisibility(View.GONE);
    }

    public static void toggleVisibility(View v) {
        if (v.getVisibility() == View.VISIBLE) v.setVisibility(View.GONE);
        else v.setVisibility(View.VISIBLE);
    }

    public static User getCurrentUserFromPrefs(Context c) {
        SharedPreferences prefs = MyApplication.getPrefs();
        Gson gson = new Gson();
        String json = prefs.getString(c.getString(R.string.pref_current_user), gson.toJson(new User()));
        return gson.fromJson(json, User.class);
    }

    // Hide the soft keyboard if it is currently displayed. This should be called immediately before
    // the activity is exited to prevent that the keyboard is still shown in the parent activity
    // for a short moment.
    public static void hideKeyboard(Activity a, EditText et) {
        InputMethodManager imm = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    public static boolean isToday(Calendar date) {
        Calendar today = Calendar.getInstance();
        return date.get(Calendar.ERA) == today.get(Calendar.ERA)
                && date.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isYesterday(Calendar date) {
        date.add(Calendar.DATE, 1);
        return isToday(date);
    }

    // Extract the message of the first error from a Twitter API error response JSON object
    public static String extractJsonErrorMsg(JSONObject errorResponse) {
        String msg = null;
        try {
            JSONObject error = (JSONObject) errorResponse.getJSONArray("errors").get(0);
            msg = error.getString("message");
        } catch (JSONException e) { e.printStackTrace(); }
        return msg;
    }

}
