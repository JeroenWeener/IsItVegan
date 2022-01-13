package com.jwindustries.isitvegan;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.util.Locale;

public class Utils {
    /*
     * Debugging
     */
    public static boolean DEBUG = false; // Do not update outside of this file
    private static final String TAG = "DEBUG";
    public static void debug(Object object, String message) {
        if (DEBUG) {
            Log.d(TAG + ": " + object.getClass().getSimpleName() + "\t", message);
        }
    }

    public static void handleTheme(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String themeSetting = preferences.getString("theme", "system");
        switch (themeSetting) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static String getAppLocale(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String locale = preferences.getString("language_app", "system");
        if (locale.equals("system")) {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
        }
        return locale;
    }

    public static String handleAppLocale(Context context) {
        String localeString = getAppLocale(context);
        Locale locale = new Locale(localeString);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        return localeString;
    }

    public static String getIngredientLocale(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String locale = preferences.getString("language_ingredients", "app");
        if (locale.equals("app")) {
            locale = getAppLocale(context);
        }
        return locale;
    }

    @NonNull
    public static Resources getDutchResources(Context context) {
        Locale desiredLocale = new Locale("nl");
        return getResources(context, desiredLocale);
    }

    @NonNull
    public static Resources getEnglishResources(Context context) {
        Locale desiredLocale = new Locale("en");
        return getResources(context, desiredLocale);
    }

    @NonNull
    private static Resources getResources(Context context, Locale locale) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration = new Configuration(configuration);
        configuration.setLocale(locale);
        Context localizedContext = context.createConfigurationContext(configuration);
        return localizedContext.getResources();
    }

    public static void storeTutorialFinished(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean("tutorial", true).apply();
    }

    public static boolean isTutorialFinished(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("tutorial", false);
    }

    public static void setDebugging(Context context, boolean debugging) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean("debug", debugging).apply();
        DEBUG = debugging;
    }

    public static boolean toggleDebugging(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean debugging = !preferences.getBoolean("debug", false);
        setDebugging(context, debugging);
        return debugging;
    }

    public static boolean isDebugging(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean debugging = preferences.getBoolean("debug", false);
        DEBUG = debugging;
        return debugging;
    }

    public static String normalizeString(String string, boolean removeSpacing) {
        return string
                .toLowerCase()
                .trim()

                // Normalize whitespace characters
                .replace("\t", " ")
                .replace("\n", " ")

                // Optionally remove whitespace
                .replace(" ", removeSpacing ? "" : " ")

                // Remove special characters
                .replace("-", "")
                .replace("," , "")
                .replace("'", "")
                .replace("(", "")
                .replace(")", "")

                // Transform special latin characters to their base form
                .replace("ä", "a")
                .replace("á", "a")
                .replace("à", "a")
                .replace("ë", "e")
                .replace("é", "e")
                .replace("è", "e")
                .replace("ï", "i")
                .replace("í", "i")
                .replace("ì", "i")
                .replace("ö", "o")
                .replace("ó", "o")
                .replace("ò", "o")
                .replace("ü", "u")
                .replace("ú", "u")
                .replace("ù", "u")
                .replace("ÿ", "y")
                .replace("ý", "y");
    }
}
