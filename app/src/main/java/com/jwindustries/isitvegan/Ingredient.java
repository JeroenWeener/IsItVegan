package com.jwindustries.isitvegan;

import android.content.Context;
import android.content.res.Resources;

import java.io.Serializable;

public class Ingredient implements Serializable {
    private final String name;
    private final IngredientType type;
    private final String information;


    public Ingredient(Context context, Resources resources, int nameResourceId, IngredientType type) {
        this(context, resources, nameResourceId, type, R.string.ingredient_information_empty);
    }

    public Ingredient(Context context, Resources resources, int nameResourceId, IngredientType type, int informationResourceId) {
        this.name = resources.getString(nameResourceId);
        this.type = type;
        this.information = context.getString(informationResourceId);
    }

    public String getName() {
        return name;
    }

    public IngredientType getIngredientType() {
        return type;
    }

    public String getInformation() {
        return information;
    }
}

