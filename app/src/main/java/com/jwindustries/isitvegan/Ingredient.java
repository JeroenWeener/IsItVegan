package com.jwindustries.isitvegan;

public class Ingredient {
    private String name;
    private IngredientType type;

    // Optional
    private String extraInformation;

    public Ingredient(String name, IngredientType type) {
        this.name = name;
        this.type = type;
    }

    public Ingredient(String name, IngredientType type, String extraInformation) {
        this.name = name;
        this.type = type;
        this.extraInformation = extraInformation;
    }

    public String getName() {
        return name;
    }

    public IngredientType getIngredientType() {
        return type;
    }

    public String getExtraInformation() {
        return extraInformation;
    }
}

