package com.jwindustries.isitvegan;

public class Ingredient {
    private String name;
    private boolean isVegan;

    public Ingredient(String name, boolean isVegan) {
        this.name = name;
        this.isVegan = isVegan;
    }

    public String getName() {
        return name;
    }

    public boolean isVegan() {
        return isVegan;
    }
}
