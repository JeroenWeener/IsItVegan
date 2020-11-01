package com.jwindustries.isitvegan;

import android.content.Context;
import android.content.res.Resources;

import java.util.Arrays;
import java.util.List;

public class IngredientList {
    public static List<Ingredient> getIngredientList(Context context) {
        Resources resources = Utils.getLocalizedResources(context);
        List<Ingredient> ingredientList = Arrays.asList(
                new Ingredient(context, resources, R.string.ingredient_agar, IngredientType.VEGAN, R.string.ingredient_agar_info),
                new Ingredient(context, resources, R.string.ingredient_angora_wool, IngredientType.NOT_VEGAN, R.string.ingredient_angora_wool_info),
                new Ingredient(context, resources, R.string.ingredient_beeswax, IngredientType.NOT_VEGAN, R.string.ingredient_beeswax_info),
                new Ingredient(context, resources, R.string.ingredient_bone_char, IngredientType.NOT_VEGAN, R.string.ingredient_bone_char_info),
                new Ingredient(context, resources, R.string.ingredient_butter_fat, IngredientType.NOT_VEGAN, R.string.ingredient_butter_fat_info),
                new Ingredient(context, resources, R.string.ingredient_buttermilk, IngredientType.NOT_VEGAN, R.string.ingredient_buttermilk_info),
                new Ingredient(context, resources, R.string.ingredient_calcium_5_ribonucleotides, IngredientType.VEGAN, R.string.ingredient_calcium_5_ribonucleotides_info),
                new Ingredient(context, resources, R.string.ingredient_calcium_guanylate, IngredientType.VEGAN, R.string.ingredient_calcium_guanylate_info),
                new Ingredient(context, resources, R.string.ingredient_calcium_inosinate, IngredientType.VEGAN, R.string.ingredient_calcium_inosinate_info),
                new Ingredient(context, resources, R.string.ingredient_carmine, IngredientType.NOT_VEGAN, R.string.ingredient_carmine_info),
                new Ingredient(context, resources, R.string.ingredient_casein, IngredientType.NOT_VEGAN, R.string.ingredient_casein_info),
                new Ingredient(context, resources, R.string.ingredient_cashmere, IngredientType.NOT_VEGAN, R.string.ingredient_cashmere_info),
                new Ingredient(context, resources, R.string.ingredient_castoreum, IngredientType.NOT_VEGAN, R.string.ingredient_castoreum_info),
                new Ingredient(context, resources, R.string.ingredient_cera_alba, IngredientType.NOT_VEGAN, R.string.ingredient_cera_alba_info),
                new Ingredient(context, resources, R.string.ingredient_cera_flava, IngredientType.NOT_VEGAN, R.string.ingredient_cera_flava_info),
                new Ingredient(context, resources, R.string.ingredient_cocoa_butter, IngredientType.VEGAN, R.string.ingredient_cocoa_butter_info),
                new Ingredient(context, resources, R.string.ingredient_coconut_fiber, IngredientType.VEGAN, R.string.ingredient_coconut_fiber_info),
                new Ingredient(context, resources, R.string.ingredient_cotton, IngredientType.VEGAN, R.string.ingredient_cotton_info),
                new Ingredient(context, resources, R.string.ingredient_diglycerides_of_fatty_acids, IngredientType.VEGAN, R.string.ingredient_diglycerides_of_fatty_acids_info),
                new Ingredient(context, resources, R.string.ingredient_disodium_5_ribonucleotides, IngredientType.VEGAN, R.string.ingredient_disodium_5_ribonucleotides_info),
                new Ingredient(context, resources, R.string.ingredient_disodium_guanylate, IngredientType.VEGAN, R.string.ingredient_disodium_guanylate_info),
                new Ingredient(context, resources, R.string.ingredient_disodium_inosinate, IngredientType.VEGAN, R.string.ingredient_disodium_inosinate_info),
                new Ingredient(context, resources, R.string.ingredient_egg, IngredientType.NOT_VEGAN, R.string.ingredient_egg_info),
                new Ingredient(context, resources, R.string.ingredient_eggplant, IngredientType.VEGAN, R.string.ingredient_eggplant_info),
                new Ingredient(context, resources, R.string.ingredient_gelatin, IngredientType.NOT_VEGAN, R.string.ingredient_gelatin_info),
                new Ingredient(context, resources, R.string.ingredient_glycerol, IngredientType.VEGAN, R.string.ingredient_glycerol_info),
                new Ingredient(context, resources, R.string.ingredient_guanosine_monophosphate, IngredientType.VEGAN, R.string.ingredient_guanosine_monophosphate_info),
                new Ingredient(context, resources, R.string.ingredient_guanylic_acid, IngredientType.VEGAN, R.string.ingredient_guanylic_acid_info),
                new Ingredient(context, resources, R.string.ingredient_hemp, IngredientType.VEGAN, R.string.ingredient_hemp_info),
                new Ingredient(context, resources, R.string.ingredient_honey, IngredientType.NOT_VEGAN, R.string.ingredient_honey_info),
                new Ingredient(context, resources, R.string.ingredient_inosine_monophosphate, IngredientType.VEGAN, R.string.ingredient_inosine_monophosphate_info),
                new Ingredient(context, resources, R.string.ingredient_inosinic_acid, IngredientType.VEGAN, R.string.ingredient_inosinic_acid_info),
                new Ingredient(context, resources, R.string.ingredient_keratin, IngredientType.NOT_VEGAN, R.string.ingredient_keratin_info),
                new Ingredient(context, resources, R.string.ingredient_l_cysteine, IngredientType.DEPENDS, R.string.ingredient_l_cysteine_info),
                new Ingredient(context, resources, R.string.ingredient_l_cystine, IngredientType.DEPENDS, R.string.ingredient_l_cystine_info),
                new Ingredient(context, resources, R.string.ingredient_lactic_acid, IngredientType.VEGAN, R.string.ingredient_lactic_acid_info),
                new Ingredient(context, resources, R.string.ingredient_lactitol, IngredientType.NOT_VEGAN, R.string.ingredient_lactitol_info),
                new Ingredient(context, resources, R.string.ingredient_lactobacillus, IngredientType.DEPENDS, R.string.ingredient_lactobacillus_info),
                new Ingredient(context, resources, R.string.ingredient_lactose, IngredientType.NOT_VEGAN, R.string.ingredient_lactose_info),
                new Ingredient(context, resources, R.string.ingredient_lard, IngredientType.NOT_VEGAN, R.string.ingredient_lard_info),
                new Ingredient(context, resources, R.string.ingredient_latex, IngredientType.VEGAN, R.string.ingredient_latex_info),
                new Ingredient(context, resources, R.string.ingredient_lecithin, IngredientType.DEPENDS, R.string.ingredient_lecithin_info),
                new Ingredient(context, resources, R.string.ingredient_linen, IngredientType.VEGAN, R.string.ingredient_linen_info),
                new Ingredient(context, resources, R.string.ingredient_lyocell, IngredientType.VEGAN, R.string.ingredient_lyocell_info),
                new Ingredient(context, resources, R.string.ingredient_modal, IngredientType.VEGAN, R.string.ingredient_modal_info),
                new Ingredient(context, resources, R.string.ingredient_monoglycerides_of_fatty_acids, IngredientType.VEGAN, R.string.ingredient_monoglycerides_of_fatty_acids_info),
                new Ingredient(context, resources, R.string.ingredient_musk, IngredientType.NOT_VEGAN, R.string.ingredient_musk_info),
                new Ingredient(context, resources, R.string.ingredient_pearls, IngredientType.NOT_VEGAN, R.string.ingredient_pearls_info),
                new Ingredient(context, resources, R.string.ingredient_potassium_guanylate, IngredientType.VEGAN, R.string.ingredient_potassium_guanylate_info),
                new Ingredient(context, resources, R.string.ingredient_potassium_inosinate, IngredientType.VEGAN, R.string.ingredient_potassium_inosinate_info),
                new Ingredient(context, resources, R.string.ingredient_powdered_milk, IngredientType.NOT_VEGAN, R.string.ingredient_powdered_milk_info),
                new Ingredient(context, resources, R.string.ingredient_schellac, IngredientType.NOT_VEGAN, R.string.ingredient_schellac_info),
                new Ingredient(context, resources, R.string.ingredient_seaweed, IngredientType.VEGAN, R.string.ingredient_seaweed_info),
                new Ingredient(context, resources, R.string.ingredient_silk, IngredientType.NOT_VEGAN, R.string.ingredient_silk_info),
                new Ingredient(context, resources, R.string.ingredient_skatole, IngredientType.NOT_VEGAN, R.string.ingredient_skatole_info),
                new Ingredient(context, resources, R.string.ingredient_soy_lecithin, IngredientType.VEGAN, R.string.ingredient_soy_lecithin_info),
                new Ingredient(context, resources, R.string.ingredient_soybeans, IngredientType.VEGAN, R.string.ingredient_soybeans_info),
                new Ingredient(context, resources, R.string.ingredient_sunflower_lecithin, IngredientType.VEGAN, R.string.ingredient_sunflower_lecithin_info),
                new Ingredient(context, resources, R.string.ingredient_tallow, IngredientType.NOT_VEGAN, R.string.ingredient_tallow_info),
                new Ingredient(context, resources, R.string.ingredient_vanilla, IngredientType.VEGAN, R.string.ingredient_vanilla_info),
                new Ingredient(context, resources, R.string.ingredient_vitamin_d2, IngredientType.VEGAN, R.string.ingredient_vitamin_d2_info),
                new Ingredient(context, resources, R.string.ingredient_vitamin_d3, IngredientType.DEPENDS, R.string.ingredient_vitamin_d3_info),
                new Ingredient(context, resources, R.string.ingredient_whey, IngredientType.NOT_VEGAN, R.string.ingredient_whey_info),
                new Ingredient(context, resources, R.string.ingredient_whey_powder, IngredientType.NOT_VEGAN, R.string.ingredient_whey_powder_info),
                new Ingredient(context, resources, R.string.ingredient_whey_protein, IngredientType.NOT_VEGAN, R.string.ingredient_whey_protein_info),
                new Ingredient(context, resources, R.string.ingredient_wool, IngredientType.NOT_VEGAN, R.string.ingredient_wool_info),
                new Ingredient(context, resources, R.string.ingredient_wool_fat, IngredientType.NOT_VEGAN, R.string.ingredient_wool_fat_info)
        );

        // Sort alphabetically. Ignore '(' characters
        ingredientList.sort((a, b) -> a.getName().replace("(", "").compareTo(b.getName().replace("(", "")));

        return ingredientList;
    }
}
