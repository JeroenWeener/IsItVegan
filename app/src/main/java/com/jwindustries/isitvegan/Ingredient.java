package com.jwindustries.isitvegan;

import android.content.Context;
import android.content.res.Resources;

import java.io.Serializable;

public class Ingredient implements Serializable {
    private final String name;
    private final IngredientType type;
    private final String information;
    private final String eNumber;


    public Ingredient(Context context, Resources resources, int nameResourceId, IngredientType type) {
        this(context, resources, nameResourceId, type, R.string.ingredient_information_empty, null);
    }

    public Ingredient(Context context, Resources resources, int nameResourceId, IngredientType type, int informationResourceId) {
        this(context, resources, nameResourceId, type, informationResourceId, null);
    }

    public Ingredient(Context context, Resources resources, int nameResourceId, IngredientType type, String eNumber) {
        this(context, resources, nameResourceId, type, R.string.ingredient_information_empty, eNumber);
    }

    public Ingredient(Context context, Resources resources, int nameResourceId, IngredientType type, int informationResourceId, String eNumber) {
        // Get name in the ingredient's locale
        this.name = resources.getString(nameResourceId);
        this.type = type;
        // Get information in the app's locale
        this.information = context.getString(informationResourceId);
        this.eNumber = eNumber;
    }

    public String getName() {
        return this.name;
    }

    public IngredientType getIngredientType() {
        return this.type;
    }

    public String getInformation() {
        return this.information;
    }

    public String getENumber() {
        return this.eNumber;
    }

    public boolean hasENumber() {
        return this.eNumber != null && this.eNumber.length() > 0;
    }
}

