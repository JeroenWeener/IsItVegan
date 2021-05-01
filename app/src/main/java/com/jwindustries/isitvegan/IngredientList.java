package com.jwindustries.isitvegan;

import android.content.Context;
import android.content.res.Resources;

import java.util.Arrays;
import java.util.List;

public class IngredientList {
    public static List<Ingredient> getIngredientList(Context context) {
        Resources resources = Utils.getLocalizedResources(context);
        Resources englishResources = Utils.getEnglishResources(context);
        List<Ingredient> ingredientList = Arrays.asList(
                new Ingredient(context, resources, englishResources, R.string.ingredient_potassium_inosinate, IngredientType.VEGAN, R.string.ingredient_potassium_inosinate_info,"E632"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_disodium_5_ribonucleotides, IngredientType.VEGAN, R.string.ingredient_disodium_5_ribonucleotides_info,"E635"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_disodium_inosinate, IngredientType.VEGAN, R.string.ingredient_disodium_inosinate_info,"E631"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_agar, IngredientType.VEGAN, R.string.ingredient_agar_info,"E406"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_angora_wool, IngredientType.NOT_VEGAN, R.string.ingredient_angora_wool_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_gum_arabic, IngredientType.VEGAN, R.string.ingredient_gum_arabic_info,"E414"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_ascorbate, IngredientType.VEGAN, R.string.ingredient_ascorbate_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_ascorbic_acid, IngredientType.VEGAN, R.string.ingredient_ascorbic_acid_info,"E300"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_ascorbyl_palmitate, IngredientType.VEGAN, R.string.ingredient_ascorbyl_palmitate_info,"E304"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_astaxanthin, IngredientType.VEGAN, R.string.ingredient_astaxanthin_info,"E161j"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_eggplant, IngredientType.VEGAN, R.string.ingredient_eggplant_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_bone_char, IngredientType.NOT_VEGAN, R.string.ingredient_bone_char_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_beeswax, IngredientType.NOT_VEGAN, R.string.ingredient_beeswax_info,"E901"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_butter_fat, IngredientType.NOT_VEGAN, R.string.ingredient_butter_fat_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_cocoa_butter, IngredientType.VEGAN, R.string.ingredient_cocoa_butter_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_cocoa_mass, IngredientType.VEGAN, R.string.ingredient_cocoa_mass_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_calcium_5_ribonucleotides, IngredientType.VEGAN, R.string.ingredient_calcium_5_ribonucleotides_info,"E634"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_calcium_ascorbate, IngredientType.VEGAN, R.string.ingredient_calcium_ascorbate_info,"E302"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_calcium_formate, IngredientType.VEGAN, R.string.ingredient_calcium_formate_info,"E238"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_calcium_guanylate, IngredientType.VEGAN, R.string.ingredient_calcium_guanylate_info,"E629"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_calcium_inosinate, IngredientType.VEGAN, R.string.ingredient_calcium_inosinate_info,"E633"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_calcium_lactate, IngredientType.VEGAN, R.string.ingredient_calcium_lactate_info,"E327"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_casein, IngredientType.NOT_VEGAN, R.string.ingredient_casein_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_castoreum, IngredientType.NOT_VEGAN, R.string.ingredient_castoreum_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_cera_alba, IngredientType.NOT_VEGAN, R.string.ingredient_cera_alba_info,"E901"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_cera_flava, IngredientType.NOT_VEGAN, R.string.ingredient_cera_flava_info,"E901"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_cholic_acid, IngredientType.NOT_VEGAN, R.string.ingredient_cholic_acid_info,"E1000"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_citranaxanthin, IngredientType.VEGAN, R.string.ingredient_citranaxanthin_info,"E161i"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_collagen, IngredientType.NOT_VEGAN, R.string.ingredient_collagen_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_egg, IngredientType.NOT_VEGAN, R.string.ingredient_egg_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_egg_yolk, IngredientType.NOT_VEGAN, R.string.ingredient_egg_yolk_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_gelatin, IngredientType.NOT_VEGAN, R.string.ingredient_gelatin_info,"E441, E485"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_glycerol, IngredientType.VEGAN, R.string.ingredient_glycerol_info,"E422"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_guanosine_monophosphate, IngredientType.VEGAN, R.string.ingredient_guanosine_monophosphate_info,"E626"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_guanylic_acid, IngredientType.VEGAN, R.string.ingredient_guanylic_acid_info,"E626"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_hemp, IngredientType.VEGAN, R.string.ingredient_hemp_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_honey, IngredientType.NOT_VEGAN, R.string.ingredient_honey_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_inosine_monophosphate, IngredientType.VEGAN, R.string.ingredient_inosine_monophosphate_info,"E630"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_inosinic_acid, IngredientType.VEGAN, R.string.ingredient_inosinic_acid_info,"E630"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_potassium_ascorbate, IngredientType.VEGAN, R.string.ingredient_potassium_ascorbate_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_potassium_guanylate, IngredientType.VEGAN, R.string.ingredient_potassium_guanylate_info,"E628"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_potassium_lactate, IngredientType.VEGAN, R.string.ingredient_potassium_lactate_info,"E326"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_potassium_nitrate, IngredientType.VEGAN, R.string.ingredient_potassium_nitrate_info,"E252"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_carmine, IngredientType.NOT_VEGAN, R.string.ingredient_carmine_info,"E120"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_buttermilk, IngredientType.NOT_VEGAN, R.string.ingredient_buttermilk_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_cashmere, IngredientType.NOT_VEGAN, R.string.ingredient_cashmere_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_cotton, IngredientType.VEGAN, R.string.ingredient_cotton_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_keratin, IngredientType.NOT_VEGAN, R.string.ingredient_keratin_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_coconut_fiber, IngredientType.VEGAN, R.string.ingredient_coconut_fiber_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_l_cysteine, IngredientType.DEPENDS, R.string.ingredient_l_cysteine_info,"E920"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_l_cystine, IngredientType.DEPENDS, R.string.ingredient_l_cystine_info,"E921"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_lactitol, IngredientType.NOT_VEGAN, R.string.ingredient_lactitol_info,"E966"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_lactobacillus, IngredientType.DEPENDS, R.string.ingredient_lactobacillus_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_lactose, IngredientType.NOT_VEGAN, R.string.ingredient_lactose_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_latex, IngredientType.VEGAN, R.string.ingredient_latex_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_lecithin, IngredientType.DEPENDS, R.string.ingredient_lecithin_info,"E322"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_linen, IngredientType.VEGAN, R.string.ingredient_linen_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_lyocell, IngredientType.VEGAN, R.string.ingredient_lyocell_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_lysozyme, IngredientType.NOT_VEGAN, R.string.ingredient_lysozyme_info,"E1105"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_milk, IngredientType.DEPENDS, R.string.ingredient_milk_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_powdered_milk, IngredientType.NOT_VEGAN, R.string.ingredient_powdered_milk_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_lactic_acid, IngredientType.VEGAN, R.string.ingredient_lactic_acid_info,"E270"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_formic_acid, IngredientType.VEGAN, R.string.ingredient_formic_acid_info,"E236"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_modal, IngredientType.VEGAN, R.string.ingredient_modal_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_mono__and_diglycerides_of_fatty_acids, IngredientType.VEGAN, R.string.ingredient_mono__and_diglycerides_of_fatty_acids_info,"E471"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_musk, IngredientType.NOT_VEGAN, R.string.ingredient_musk_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_sodium_ascorbate, IngredientType.VEGAN, R.string.ingredient_sodium_ascorbate_info,"E301"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_sodium_formate, IngredientType.VEGAN, R.string.ingredient_sodium_formate_info,"E237"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_disodium_guanylate, IngredientType.VEGAN, R.string.ingredient_disodium_guanylate_info,"E627"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_sodium_lactate, IngredientType.VEGAN, R.string.ingredient_sodium_lactate_info,"E325"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_sodium_nitrite, IngredientType.VEGAN, R.string.ingredient_sodium_nitrite_info,"E250"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_nisin, IngredientType.DEPENDS, R.string.ingredient_nisin_info,"E234"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_pearls, IngredientType.NOT_VEGAN, R.string.ingredient_pearls_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_lard, IngredientType.NOT_VEGAN, R.string.ingredient_lard_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_schellac, IngredientType.NOT_VEGAN, R.string.ingredient_schellac_info,"E904"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_skatole, IngredientType.NOT_VEGAN, R.string.ingredient_skatole_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_soybeans, IngredientType.VEGAN, R.string.ingredient_soybeans_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_soy_lecithin, IngredientType.VEGAN, R.string.ingredient_soy_lecithin_info,"E322"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_tallow, IngredientType.NOT_VEGAN, R.string.ingredient_tallow_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_vanilla, IngredientType.VEGAN, R.string.ingredient_vanilla_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_violoxanthin, IngredientType.VEGAN, R.string.ingredient_violoxanthin_info,"E161e"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_vitamin_b12, IngredientType.VEGAN, R.string.ingredient_vitamin_b12_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_vitamin_d2, IngredientType.VEGAN, R.string.ingredient_vitamin_d2_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_vitamin_d3, IngredientType.DEPENDS, R.string.ingredient_vitamin_d3_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_whey_protein, IngredientType.NOT_VEGAN, R.string.ingredient_whey_protein_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_whey, IngredientType.NOT_VEGAN, R.string.ingredient_whey_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_whey_powder, IngredientType.NOT_VEGAN, R.string.ingredient_whey_powder_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_wool, IngredientType.NOT_VEGAN, R.string.ingredient_wool_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_wool_fat, IngredientType.NOT_VEGAN, R.string.ingredient_wool_fat_info,"E913"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_zeaxanthin, IngredientType.VEGAN, R.string.ingredient_zeaxanthin_info,"E161h"),
                new Ingredient(context, resources, englishResources, R.string.ingredient_seaweed, IngredientType.VEGAN, R.string.ingredient_seaweed_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_silk, IngredientType.NOT_VEGAN, R.string.ingredient_silk_info,null),
                new Ingredient(context, resources, englishResources, R.string.ingredient_sunflower_lecithin, IngredientType.VEGAN, R.string.ingredient_sunflower_lecithin_info,"E322")
        );

        // Sort alphabetically, ignoring '(' characters
        ingredientList.sort((a, b) -> a.getName().replace("(", "").compareTo(b.getName().replace("(", "")));

        return ingredientList;
    }
}
