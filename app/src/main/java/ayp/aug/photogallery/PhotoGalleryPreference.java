package ayp.aug.photogallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Hattapong on 8/19/2016.
 */
public class PhotoGalleryPreference {
    private static final String TAG = "PhotoGalleryPref";
    private static final String PREF_SEARCH_KEY = "PhotoGalleryPref";
    private static final String PREF_LAST_ID = "PREF_LAST_ID";
    private static final String PREF_IS_ALARM_ON = "PREF_ALARM_ON";
    private static final String PREF_USE_GPS = "use_gps";
    public static String getStoredSearchKey(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_SEARCH_KEY,null);
    }

    public static SharedPreferences mySharedPref(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean getUseGPS(Context ctx){
        return mySharedPref(ctx).getBoolean(PREF_USE_GPS,false);
    }

    public static void setUseGPS(Context ctx,boolean use_GPS){
        mySharedPref(ctx).edit().putBoolean(PREF_USE_GPS,use_GPS).apply();
    }

    public static void setStoredSearchKey(Context context, String key){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit()
                .putString(PREF_SEARCH_KEY,key)
                .apply();
    }
    public static boolean getStoredIsAlarmOn(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(PREF_IS_ALARM_ON,false);
    }

    public static void setStoredIsAlarmOn(Context context, boolean key){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit()
                .putBoolean(PREF_IS_ALARM_ON,key)
                .apply();
    }
    public static String getStoredLastId(Context context){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(PREF_LAST_ID,null);
    }

    public static void setStoredLastId(Context context, String key){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit()
                .putString(PREF_LAST_ID,key)
                .apply();
    }
}
