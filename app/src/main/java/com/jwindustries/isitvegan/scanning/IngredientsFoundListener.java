package com.jwindustries.isitvegan.scanning;

import java.util.List;

public interface IngredientsFoundListener {
    void onIngredientsFound(List<IngredientElement> ingredientElements, double averageElementHeight);
}
