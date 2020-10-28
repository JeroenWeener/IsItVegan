package com.jwindustries.isitvegan;

import android.content.Context;
import android.content.res.Resources;

import java.util.Arrays;
import java.util.List;

public class IngredientList {
    public static List<Ingredient> getIngredientList(Context context) {
        Resources resources = Utils.getLocalizedResources(context);
        List<Ingredient> ingredientList = Arrays.asList(
                new Ingredient(context, resources, R.string.ingredient_beeswax, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_butter_fat, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_buttermilk, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_carmine, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_castoreum, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_cocoa_butter, IngredientType.VEGAN, R.string.ingredient_cacoa_butter_info),
                new Ingredient(context, resources, R.string.ingredient_eggplant, IngredientType.VEGAN, R.string.ingredient_eggplant_info),
                new Ingredient(context, resources, R.string.ingredient_egg, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_gelatin, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_glycerine, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_honey, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_lactic_acid, IngredientType.VEGAN, R.string.ingredient_lactic_acid_info),
                new Ingredient(context, resources, R.string.ingredient_lactitol, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_lactobacillus, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_lactose, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_lanolin, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_lcysteine, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_milk_powder, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_milk_serum, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_milk_sugar, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_shellac, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_silk, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_skatole, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_vitamin_d3, IngredientType.DEPENDS, R.string.ingredient_vitamin_d3_info)
        );

        ingredientList.sort((a, b) -> a.getName().compareTo(b.getName()));

        return ingredientList;
    }
}
