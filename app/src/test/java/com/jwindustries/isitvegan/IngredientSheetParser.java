package com.jwindustries.isitvegan;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class IngredientSheetParser {
    private static final String CSV_PATH = System.getProperty("user.dir") + "/src/main/assets/ingredient_sheet.tsv";
    private static final String INGREDIENT_LIST_PATH = System.getProperty("user.dir") + "/src/main/assets/generated/ingredient-list.txt";
    private static final String[] INGREDIENT_LIST_TEMPLATE = new String[]{"new Ingredient(\"", "\", \"", "\", IngredientType.", ", ", "),"};

    @Test
    public void parse() {
        List<List<String>> data = this.readTsv();

        List<String> englishNames = data.stream().map(row -> row.get(0)).collect(Collectors.toList());
        List<String> dutchNames = data.stream().map(row -> row.get(1)).collect(Collectors.toList());

        List<String> ingredientTypes = data.stream().map(row -> row.get(3)).collect(Collectors.toList());
        List<String> eNumbers = data.stream().map(row -> row.get(4)).collect(Collectors.toList());

        List<String> ingredientConstructionStrings = this.generateIngredientList(
                dutchNames,
                englishNames,
                ingredientTypes,
                eNumbers);
        String ingredientConstruction = String.join("\n", ingredientConstructionStrings);
        ingredientConstruction = ingredientConstruction.substring(0, ingredientConstruction.length() - 1); // Remove trailing comma
        this.writeIngredientListToFile(ingredientConstruction);
    }

    private List<List<String>> readTsv() {
        List<List<String>> data = new ArrayList<>();

        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(CSV_PATH));
            String row;
            while ((row = csvReader.readLine()) != null) {
                // Strip " characters
                row = row.replace("\"", "");

                // Split row in items
                row = row.replace("\t", "\t%");
                row = "%" + row;
                List<String> rowItems = Arrays.asList(row.split("\t"));
                rowItems = rowItems.stream().map(item -> item.substring(1)).collect(Collectors.toList());

                data.add(rowItems);
            }
            csvReader.close();

            // Remove header rows
            data.remove(0);
            data.remove(0);

        } catch (IOException exception) {
            exception.getStackTrace();
        }

        return data;
    }

    private void writeIngredientListToFile(String ingredientListFile) {
        try {
            FileWriter fileWriterIngredientList = new FileWriter(INGREDIENT_LIST_PATH);
            fileWriterIngredientList.write(ingredientListFile);
            fileWriterIngredientList.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> generateIngredientList(
            List<String> dutchNames,
            List<String> englishNames,
            List<String> ingredientTypes,
            List<String> eNumbers
    ) {
        List<String> ingredientConstructionStrings = new ArrayList<>();

        for (int index = 0; index < dutchNames.size(); index++) {

            String dutchName = dutchNames.get(index);
            String englishName = englishNames.get(index);
            String ingredientType = ingredientTypes.get(index);
            String eNumber = eNumbers.get(index);
            if (eNumber.length() > 0) {
                eNumber = "\"" + eNumber + "\"";
            } else {
                eNumber = "null";
            }

            String ingredientConstructionString =
                    INGREDIENT_LIST_TEMPLATE[0] +
                    dutchName +
                    INGREDIENT_LIST_TEMPLATE[1] +
                    englishName +
                    INGREDIENT_LIST_TEMPLATE[2] +
                    ingredientType
                            .replace("No", "NOT_VEGAN")
                            .replace("Yes", "VEGAN")
                            .replace("Depends", "DEPENDS") +
                    INGREDIENT_LIST_TEMPLATE[3] +
                    eNumber +
                    INGREDIENT_LIST_TEMPLATE[4]
            ;

            ingredientConstructionStrings.add(ingredientConstructionString);
        }

        return ingredientConstructionStrings;
    }
}