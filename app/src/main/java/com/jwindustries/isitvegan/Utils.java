package com.jwindustries.isitvegan;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.util.Locale;

public class Utils {

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
    public static Resources getLocalizedResources(Context context) {
        Locale desiredLocale = new Locale(getIngredientLocale(context));
        Configuration configuration = context.getResources().getConfiguration();
        configuration = new Configuration(configuration);
        configuration.setLocale(desiredLocale);
        Context localizedContext = context.createConfigurationContext(configuration);
        return localizedContext.getResources();
    }

    public static String normalizeString(String string) {
        return string
                .toLowerCase()

                // Remove spaces
                .replace(" ", "")

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
