package com.jwindustries.isitvegan.scanning;

import com.google.mlkit.vision.text.Text;
import com.jwindustries.isitvegan.Ingredient;

public class IngredientElement {
    private final Ingredient ingredient;
    private final Text.Element element;

    public IngredientElement(
            Ingredient ingredient,
            Text.Element element
    ) {
        this.ingredient = ingredient;
        this.element = element;
    }

    public Ingredient getIngredient() {
        return this.ingredient;
    }

    public Text.Element getElement() {
        return this.element;
    }
}
