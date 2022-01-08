package com.jwindustries.isitvegan.scanning;

import com.google.mlkit.vision.text.Text;

import java.util.List;

public interface IngredientsFoundListener {
    void onIngredientsFound(List<IngredientElement> ingredientElements);
    void printText(String text, Text.Element element);
}
