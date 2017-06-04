package com.mdevsolutions.cc2564.Utilities;

import android.net.Uri;
import android.util.Log;

/**
 * Created by Michi on 23/04/2017.
 */

public class HttpUtils {

    // https://api.ictcommunity.org/v0/Accounts/A3JHBLG1ZBYLK63Q

    public String buildUrl() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(Constants.HTTP_SCHEME)
                .authority(Constants.HTTP_AUTHORITY)
                .appendPath(Constants.HTTP_ICT_PATH_1)
                .appendPath(Constants.HTTP_ICT_PATH_2)
                .appendPath(Constants.HTTP_ICT_PATH_3);
        Log.d(Constants.DEBUG_TAG, " String built is: "+ builder.build().toString());

        return builder.build().toString();
    }
}
