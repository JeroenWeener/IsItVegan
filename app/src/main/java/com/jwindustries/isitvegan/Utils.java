package com.jwindustries.isitvegan;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    /*
     * Debugging
     */
    public static final boolean DEBUG = true;
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
    public static Resources getLocalizedResources(Context context) {
        Locale desiredLocale = new Locale(getIngredientLocale(context));
        Configuration configuration = context.getResources().getConfiguration();
        configuration = new Configuration(configuration);
        configuration.setLocale(desiredLocale);
        Context localizedContext = context.createConfigurationContext(configuration);
        return localizedContext.getResources();
    }

    @NonNull
    public static Resources getEnglishResources(Context context) {
        Locale desiredLocale = new Locale("en");
        Configuration configuration = context.getResources().getConfiguration();
        configuration = new Configuration(configuration);
        configuration.setLocale(desiredLocale);
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

    public static boolean isTextIngredient(String text, Ingredient ingredient) {
        List<String> keywords = new ArrayList<>();

        /*
         * Localised name
         */
        List<String> unstrippedKeywords = List.of(ingredient.getName().split(","));
        // Consider keywords with text between '()' removed
        List<String> strippedKeywords = unstrippedKeywords
                .stream()
                .map(keyword -> keyword.replaceAll("\\(.*\\)", ""))
                .collect(Collectors.toList());
        keywords.addAll(unstrippedKeywords);
        keywords.addAll(strippedKeywords);

        /*
         * English name
         */
        List<String> unstrippedEnglishKeywords = List.of(ingredient.getEnglishName().split(","));
        // Consider keywords with text between '()' removed
        List<String> strippedEnglishKeywords = unstrippedEnglishKeywords
                .stream()
                .map(keyword -> keyword.replaceAll("\\(.*\\)", ""))
                .collect(Collectors.toList());
        keywords.addAll(unstrippedEnglishKeywords);
        keywords.addAll(strippedEnglishKeywords);

        /*
         * E-numbers
         */
        if (ingredient.hasENumber()) {
            List<String> unstrippedENumbers = List.of(ingredient.getENumber().split(","));
            // Consider E-numbers with text between '()' removed
            List<String> strippedENumbers = unstrippedENumbers
                    .stream()
                    .map(keyword -> keyword.replaceAll("\\(.*\\)", ""))
                    .collect(Collectors.toList());
            keywords.addAll(unstrippedENumbers);
            keywords.addAll(strippedENumbers);
        }

        Stream<String> normalizedKeywordStream = keywords.stream().map(keyword -> Utils.normalizeString(keyword, false));

        return normalizedKeywordStream.anyMatch(keyword -> keyword.equals(Utils.normalizeString(text, false)));
    }

    /**
     * Checks whether text contains the ingredient. Ingredients are checked in both local language as in English
     * @param ingredient ingredient to be possibly contained in the text
     * @param text text that possibly contains the ingredient
     * @return whether text contains ingredient
     */
    public static boolean isIngredientInText(Ingredient ingredient, String text) {
        List<String> keywords = new ArrayList<>();

        /*
         * Localised name
         */
        List<String> unstrippedKeywords = List.of(ingredient.getName().split(","));
        // Consider keywords with text between '()' removed
        List<String> strippedKeywords = unstrippedKeywords
                .stream()
                .map(keyword -> keyword.replaceAll("\\(.*\\)", ""))
                .collect(Collectors.toList());
        keywords.addAll(unstrippedKeywords);
        keywords.addAll(strippedKeywords);

        /*
         * English name
         */
        List<String> unstrippedEnglishKeywords = List.of(ingredient.getEnglishName().split(","));
        // Consider keywords with text between '()' removed
        List<String> strippedEnglishKeywords = unstrippedEnglishKeywords
                .stream()
                .map(keyword -> keyword.replaceAll("\\(.*\\)", ""))
                .collect(Collectors.toList());
        keywords.addAll(unstrippedEnglishKeywords);
        keywords.addAll(strippedEnglishKeywords);

        /*
         * E-numbers
         */
        if (ingredient.hasENumber()) {
            List<String> unstrippedENumbers = List.of(ingredient.getENumber().split(","));
            // Consider E-numbers with text between '()' removed
            List<String> strippedENumbers = unstrippedENumbers
                    .stream()
                    .map(keyword -> keyword.replaceAll("\\(.*\\)", ""))
                    .collect(Collectors.toList());
            keywords.addAll(unstrippedENumbers);
            keywords.addAll(strippedENumbers);
        }

        Stream<String> normalizedKeywordStream = keywords.stream().map(keyword -> Utils.normalizeString(keyword, false));

        return normalizedKeywordStream.distinct().anyMatch(keyword -> {
            int index = text.indexOf(keyword);

            if (index == -1) {
                return false;
            }

            int keywordLength = keyword.length();
            boolean isAtBeginOfText = index == 0;
            boolean isAtEndOfText = text.length() == index + keywordLength;

            // Text is within string
            if (!isAtBeginOfText && !isAtEndOfText) {
                char previousCharacter = text.charAt(index - 1);
                char nextCharacter = text.charAt(index + keywordLength);
                return previousCharacter == ' ' && nextCharacter == ' ';
            // Text is string
            } else if (isAtBeginOfText && isAtEndOfText) {
                return true;
            // Text is at start of string
            } else if (isAtBeginOfText) {
                char nextCharacter = text.charAt(index + keywordLength);
                return nextCharacter == ' ';
            // Text is at end of string
            } else {
                char previousCharacter = text.charAt(index - 1);
                return previousCharacter == ' ';
            }
        });
    }
}
