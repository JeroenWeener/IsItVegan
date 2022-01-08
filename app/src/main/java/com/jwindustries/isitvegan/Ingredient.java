package com.jwindustries.isitvegan;

import android.content.Context;
import android.content.res.Resources;

import java.io.Serializable;

public class Ingredient implements Serializable {
    private final String name;
    private final String englishName;
    private final IngredientType type;
    private final String information;
    private final String eNumber;


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
        // Get information in the app's locale
        this.information = context.getString(informationResourceId);
        this.eNumber = eNumber;
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

