package com.jwindustries.isitvegan;

import java.io.Serializable;

public class Ingredient implements Serializable {
    private String name;
    private IngredientType type;

    // Optional
    private String information;


    public Ingredient(String name, IngredientType type) {
        this.name = name;
        this.type = type;
    }

    public Ingredient(String name, IngredientType type, String information) {
        this.name = name;
        this.type = type;
        this.information = information;
    }

    public String getName() {
        return name;
    }

    public IngredientType getIngredientType() {
        return type;
    }

    public boolean hasExtraInformation() {
        return information != null;
    }

    public String getInformation() {
        return information;
    }
}

