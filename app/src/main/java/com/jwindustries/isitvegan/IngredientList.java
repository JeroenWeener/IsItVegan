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
                new Ingredient(context, resources, R.string.ingredient_butter_fat, IngredientType.NOT_VEGAN, R.string.ingredient_butter_fat_info),
                new Ingredient(context, resources, R.string.ingredient_buttermilk, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_carmine, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_castoreum, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_cocoa_butter, IngredientType.VEGAN, R.string.ingredient_cacao_butter_info),
                new Ingredient(context, resources, R.string.ingredient_eggplant, IngredientType.VEGAN, R.string.ingredient_eggplant_info),
                new Ingredient(context, resources, R.string.ingredient_egg, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_gelatin, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_glycerine, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_honey, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_l_cysteine, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_lactic_acid, IngredientType.VEGAN, R.string.ingredient_lactic_acid_info),
                new Ingredient(context, resources, R.string.ingredient_lactitol, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_lactobacillus, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_lactose, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_lanolin, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_milk_powder, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_milk_serum, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_milk_sugar, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_shellac, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_silk, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_skatole, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_vitamin_d3, IngredientType.DEPENDS, R.string.ingredient_vitamin_d3_info),

                new Ingredient(context, resources, R.string.ingredient_angora, IngredientType.NOT_VEGAN, R.string.ingredient_angora_info),
                new Ingredient(context, resources, R.string.ingredient_bone_char, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_casein, IngredientType.NOT_VEGAN, R.string.ingredient_casein_info),
                new Ingredient(context, resources, R.string.ingredient_cashmere, IngredientType.NOT_VEGAN, R.string.ingredient_cashmere_info),
                new Ingredient(context, resources, R.string.ingredient_coconut_fiber, IngredientType.VEGAN),
                new Ingredient(context, resources, R.string.ingredient_cotton, IngredientType.VEGAN),
                new Ingredient(context, resources, R.string.ingredient_diglycerides, IngredientType.DEPENDS, R.string.ingredient_diglycerides_info),
                new Ingredient(context, resources, R.string.ingredient_hemp, IngredientType.VEGAN),
                new Ingredient(context, resources, R.string.ingredient_keratin, IngredientType.NOT_VEGAN, R.string.ingredient_keratin_info),
                new Ingredient(context, resources, R.string.ingredient_lard, IngredientType.NOT_VEGAN, R.string.ingredient_lard_info),
                new Ingredient(context, resources, R.string.ingredient_latex, IngredientType.DEPENDS, R.string.ingredient_latex_info),
                new Ingredient(context, resources, R.string.ingredient_lecithin, IngredientType.VEGAN),
                new Ingredient(context, resources, R.string.ingredient_linen, IngredientType.VEGAN),
                new Ingredient(context, resources, R.string.ingredient_lyocell, IngredientType.VEGAN),
                new Ingredient(context, resources, R.string.ingredient_modal, IngredientType.VEGAN),
                new Ingredient(context, resources, R.string.ingredient_monoglycerides, IngredientType.DEPENDS, R.string.ingredient_monoglycerides_info),
                new Ingredient(context, resources, R.string.ingredient_musk, IngredientType.NOT_VEGAN, R.string.ingredient_musk_info),
                new Ingredient(context, resources, R.string.ingredient_pearls, IngredientType.NOT_VEGAN, R.string.ingredient_pearls_info),
                new Ingredient(context, resources, R.string.ingredient_seaweed, IngredientType.VEGAN),
                new Ingredient(context, resources, R.string.ingredient_soybeans, IngredientType.VEGAN),
                new Ingredient(context, resources, R.string.ingredient_soy_lecithin, IngredientType.VEGAN),
                new Ingredient(context, resources, R.string.ingredient_tallow, IngredientType.NOT_VEGAN, R.string.ingredient_tallow_info),
                new Ingredient(context, resources, R.string.ingredient_vanilla, IngredientType.VEGAN),
                new Ingredient(context, resources, R.string.ingredient_whey_powder, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_whey_protein, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_whey, IngredientType.NOT_VEGAN),
                new Ingredient(context, resources, R.string.ingredient_wool, IngredientType.NOT_VEGAN, R.string.ingredient_wool_info)
        );

        ingredientList.sort((a, b) -> a.getName().compareTo(b.getName()));

        return ingredientList;
    }
}
