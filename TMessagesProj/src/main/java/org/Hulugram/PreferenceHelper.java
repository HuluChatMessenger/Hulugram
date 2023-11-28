package plus;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;

public  class PreferenceHelper {

        private static final String PREF_NAME = "daily_method_pref";
        private static final String LAST_EXECUTED = "last_executed";

        public static void saveLastExecuted(long time) {
            getPreferences(ApplicationLoader.applicationContext).edit().putLong(LAST_EXECUTED, time).apply();
        }

        public static long getLastExecuted(Context context) {
            return getPreferences(context).getLong(LAST_EXECUTED, 0);
        }

        private static SharedPreferences getPreferences(Context context) {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }