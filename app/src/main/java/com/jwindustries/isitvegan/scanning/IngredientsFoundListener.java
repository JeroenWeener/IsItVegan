package com.jwindustries.isitvegan.scanning;

import android.graphics.Rect;

import com.jwindustries.isitvegan.Ingredient;

import java.util.Map;

public interface IngredientsFoundListener {
    void onIngredientsFound(Map<Ingredient, Rect> ingredientLocations);
}
