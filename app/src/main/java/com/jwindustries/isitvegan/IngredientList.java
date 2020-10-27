package com.jwindustries.isitvegan;

import android.content.Context;

import java.util.Arrays;
import java.util.List;

public class IngredientList {
    public static List<Ingredient> getIngredientList(Context context) {
        List<Ingredient> ingredientList = Arrays.asList(
                new Ingredient(context.getResources().getString(R.string.ingredient_beeswax), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_butter_fat), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_buttermilk), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_butter), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_carmine), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_castoreum), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_cheese), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_cocoa_butter), IngredientType.VEGAN, context.getResources().getString(R.string.ingredient_cacoa_butter_info)),
                new Ingredient(context.getResources().getString(R.string.ingredient_eggplant), IngredientType.VEGAN, context.getResources().getString(R.string.ingredient_eggplant_info)),
                new Ingredient(context.getResources().getString(R.string.ingredient_egg), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_gelatin), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_glycerine), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_honey), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_lactic_acid), IngredientType.VEGAN, context.getResources().getString(R.string.ingredient_lactic_acid_info)),
                new Ingredient(context.getResources().getString(R.string.ingredient_lactitol), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_lactobacillus), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_lactose), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_lanolin), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_lcysteine), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_milk_powder), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_milk_serum), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_milk_sugar), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_milk), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_shellac), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_silk), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_skatole), IngredientType.NOT_VEGAN),
                new Ingredient(context.getResources().getString(R.string.ingredient_vitamin_d3), IngredientType.DEPENDS, context.getResources().getString(R.string.ingredient_vitamin_d3_info)),
                new Ingredient(context.getResources().getString(R.string.ingredient_yogurt), IngredientType.NOT_VEGAN)
        );

        ingredientList.sort((a, b) -> a.getName().compareTo(b.getName()));
        
        return ingredientList;
    }
}
