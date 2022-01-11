package com.jwindustries.isitvegan;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ingredient implements Serializable {
    private final String dutchName;
    private final String englishName;
    private final IngredientType type;
    private final String eNumber;
    private final List<String> keywords;


    public Ingredient(String dutchName, String englishName, IngredientType type) {
        this(dutchName, englishName, type, null);
    }

    public Ingredient(String dutchName, String englishName, IngredientType type, String eNumber) {
        this.dutchName = dutchName;
        this.englishName = englishName;
        this.type = type;
        this.eNumber = eNumber;


        /*
         * ---
         * Precompute matching keywords
         * ---
         */

        /*
         * Dutch name
         */
        List<String> unstrippedDutchKeywords = Arrays.asList(this.dutchName.split(","));
        // Consider keywords with text between '()' removed
        List<String> strippedDutchKeywords = unstrippedDutchKeywords
                .stream()
                .map(keyword -> keyword.replaceAll("\\(.*\\)", ""))
                .collect(Collectors.toList());

        /*
         * English name
         */
        List<String> unstrippedEnglishKeywords = Arrays.asList(this.englishName.split(","));
        // Consider keywords with text between '()' removed
        List<String> strippedEnglishKeywords = unstrippedEnglishKeywords
                .stream()
                .map(keyword -> keyword.replaceAll("\\(.*\\)", ""))
                .collect(Collectors.toList());

        /*
         * E-numbers
         */
        List<String> unstrippedENumbers = this.hasENumber()
                ? Arrays.asList(this.eNumber.split(","))
                : new ArrayList<>();
        List<String> strippedENumbers = unstrippedENumbers
                .stream()
                .map(keyword -> keyword.replaceAll("\\(.*\\)", ""))
                .collect(Collectors.toList());

        this.keywords = Stream.of(
                    unstrippedDutchKeywords,
                    strippedDutchKeywords,
                    unstrippedEnglishKeywords,
                    strippedEnglishKeywords,
                    unstrippedENumbers,
                    strippedENumbers
                )
                .flatMap(List::stream)
                .map(keyword -> Utils.normalizeString(keyword, false))
                .collect(Collectors.toList());
    }

    public boolean matches(String normalizedText) {
        return this.keywords.stream().anyMatch(matcher -> matcher.equals(normalizedText));
    }

    /**
     * Checks whether this ingredient is mentioned in the text.
     *
     * @param searchString text that possibly contains this ingredient
     * @return whether text contains this ingredient
     */
    public boolean isContainedIn(String searchString) {
        return keywords.stream().anyMatch(keyword -> {
            int index = searchString.indexOf(keyword);

            if (index == -1) {
                return false;
            }

            int keywordLength = keyword.length();
            boolean isAtBeginOfText = index == 0;
            boolean isAtEndOfText = searchString.length() == index + keywordLength;

            // Text is within string
            if (!isAtBeginOfText && !isAtEndOfText) {
                char previousCharacter = searchString.charAt(index - 1);
                char nextCharacter = searchString.charAt(index + keywordLength);
                return previousCharacter == ' ' && nextCharacter == ' ';
                // Text is string
            } else if (isAtBeginOfText && isAtEndOfText) {
                return true;
                // Text is at start of string
            } else if (isAtBeginOfText) {
                char nextCharacter = searchString.charAt(index + keywordLength);
                return nextCharacter == ' ';
                // Text is at end of string
            } else {
                char previousCharacter = searchString.charAt(index - 1);
                return previousCharacter == ' ';
            }
        });
    }

    public String getName(Context context) {
        return this.getName(Utils.getIngredientLocale(context));
    }

    public String getName(String locale) {
        if (locale.equals("nl")) {
            return getDutchName();
        } else {
            return getEnglishName();
        }
    }

    public String getDutchName() {
        return this.dutchName;
    }

    public String getEnglishName() {
        return this.englishName;
    }

    public IngredientType getIngredientType() {
        return this.type;
    }

    public String getENumber() {
        return this.eNumber;
    }

    public boolean hasENumber() {
        return this.eNumber != null && this.eNumber.length() > 0;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Ingredient)) {
            return false;
        }
        Ingredient ingredient = (Ingredient) obj;
        return ingredient.getDutchName().equals(this.getDutchName());
    }
}

