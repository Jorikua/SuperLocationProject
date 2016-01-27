package ua.kaganovych.superlocationproject.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

    public static boolean isInternetAvailable(Context context) {
        final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = manager.getActiveNetworkInfo();

        return info != null && info.isConnected() && !info.isRoaming();
    }

}
