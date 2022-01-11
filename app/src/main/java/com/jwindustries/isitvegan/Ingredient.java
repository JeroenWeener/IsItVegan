package com.jwindustries.isitvegan;

import android.content.Context;
import android.content.res.Resources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ingredient implements Serializable {
    private final String name;
    private final String englishName;
    private final IngredientType type;
    private final String eNumber;
    private final List<String> matchers;


    public Ingredient(Context context, Resources resources, Resources englishResources, int nameResourceId, IngredientType type) {
        this(context, resources, englishResources, nameResourceId, type, R.string.ingredient_information_empty, null);
    }

    public Ingredient(Context context, Resources resources, Resources englishResources, int nameResourceId, IngredientType type, int informationResourceId) {
        this(context, resources, englishResources, nameResourceId, type, informationResourceId, null);
    }

    public Ingredient(Context context, Resources resources, Resources englishResources, int nameResourceId, IngredientType type, String eNumber) {
        this(context, resources, englishResources, nameResourceId, type, R.string.ingredient_information_empty, eNumber);
    }

    public Ingredient(Context context, Resources resources, Resources englishResources, int nameResourceId, IngredientType type, int informationResourceId, String eNumber) {
        // Get name in the ingredient's locale
        this.name = resources.getString(nameResourceId);
        this.englishName = englishResources.getString(nameResourceId);
        this.type = type;
        this.eNumber = eNumber;


        List<String> keywords = new ArrayList<>();

        /*
         * Localised name
         */
        List<String> unstrippedKeywords = Arrays.asList(this.name.split(","));
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
        List<String> unstrippedEnglishKeywords = Arrays.asList(this.englishName.split(","));
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
        if (this.hasENumber()) {
            List<String> unstrippedENumbers = Arrays.asList(this.eNumber.split(","));
            // Consider E-numbers with text between '()' removed
            List<String> strippedENumbers = unstrippedENumbers
                    .stream()
                    .map(keyword -> keyword.replaceAll("\\(.*\\)", ""))
                    .collect(Collectors.toList());
            keywords.addAll(unstrippedENumbers);
            keywords.addAll(strippedENumbers);
        }

        Stream<String> normalizedKeywordStream = keywords.stream().map(keyword -> Utils.normalizeString(keyword, false));
        this.matchers = normalizedKeywordStream.collect(Collectors.toList());
    }

    public boolean matches(String normalizedText) {
        return this.matchers
                .stream()
                .anyMatch(matcher -> matcher.equals(normalizedText));
    }

    public String getName() {
        return this.name;
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
        return ingredient.getName().equals(this.getName());
    }
}

