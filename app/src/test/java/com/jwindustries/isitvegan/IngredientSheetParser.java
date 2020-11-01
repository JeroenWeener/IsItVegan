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
import java.util.stream.Stream;


public class IngredientSheetParser {
    private static final String CSV_PATH = System.getProperty("user.dir") + "/src/main/assets/ingredient_sheet.tsv";
    private static final String STRINGS_ENGLISH_PATH = System.getProperty("user.dir") + "/src/main/assets/generated/strings-english.txt";
    private static final String STRINGS_DUTCH_PATH = System.getProperty("user.dir") + "/src/main/assets/generated/strings-dutch.txt";
    private static final String STRINGS_GERMAN_PATH = System.getProperty("user.dir") + "/src/main/assets/generated/strings-german.txt";
    private static final String INGREDIENT_LIST_PATH = System.getProperty("user.dir") + "/src/main/assets/generated/ingredient-list.txt";

    private static final String[] STRINGS_RESOURCE_TEMPLATE = new String[]{"<string name=\"ingredient_", "\">", "</string>"};
    private static final String[] INGREDIENT_LIST_TEMPLATE = new String[]{"new Ingredient(context, resources, R.string.ingredient_", ", IngredientType.", ", R.string.ingredient_", "_info),"};

    @Test
    public void parse() {
        List<List<String>> data = this.readTsv();

        List<String> englishNames = data.stream().map(row -> row.get(0)).collect(Collectors.toList());
        List<String> dutchNames = data.stream().map(row -> row.get(1)).collect(Collectors.toList());
        List<String> germanNames = data.stream().map(row -> row.get(2)).collect(Collectors.toList());

        List<String> ingredientTypes = data.stream().map(row -> row.get(3)).collect(Collectors.toList());

        List<String> englishInfo = data.stream().map(row -> row.get(4)).collect(Collectors.toList());
        List<String> dutchInfo = data.stream().map(row -> row.get(5)).collect(Collectors.toList());
        List<String> germanInfo = data.stream().map(row -> row.get(6)).collect(Collectors.toList());

        List<String> stringResourceIdentifiers = this.generateStringResourceIdentifiers(englishNames);

        List<String> englishStringResources = this.generateStringResources(stringResourceIdentifiers, englishNames);
        List<String> dutchStringResources = this.generateStringResources(stringResourceIdentifiers, dutchNames);
        List<String> germanStringResources = this.generateStringResources(stringResourceIdentifiers, germanNames);

        List<String> englishInfoStringResources = this.generateInfoStringResources(stringResourceIdentifiers, englishInfo);
        List<String> dutchInfoStringResources = this.generateInfoStringResources(stringResourceIdentifiers, dutchInfo);
        List<String> germanInfoStringResources = this.generateInfoStringResources(stringResourceIdentifiers, germanInfo);

        List<String> englishResources = Stream.concat(englishStringResources.stream(), englishInfoStringResources.stream()).collect(Collectors.toList());
        List<String> dutchResources = Stream.concat(dutchStringResources.stream(), dutchInfoStringResources.stream()).collect(Collectors.toList());
        List<String> germanResources = Stream.concat(germanStringResources.stream(), germanInfoStringResources.stream()).collect(Collectors.toList());

        String englishResourceFile = String.join("\n", englishResources);
        String dutchResourceFile = String.join("\n", dutchResources);
        String germanResourceFile = String.join("\n", germanResources);

        List<String> ingredientList = this.generateIngredientList(stringResourceIdentifiers, ingredientTypes);
        String ingredientListFile = String.join("\n", ingredientList);

        // Remove trailing comma
        ingredientListFile = ingredientListFile.substring(0, ingredientListFile.length() - 1);

        this.writeFiles(englishResourceFile, dutchResourceFile, germanResourceFile, ingredientListFile);
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

    private void writeFiles(String englishResourceFile, String dutchResourceFile, String germanResourceFile, String ingredientListFile) {
        try {
            FileWriter fileWriterEnglish = new FileWriter(STRINGS_ENGLISH_PATH);
            fileWriterEnglish.write(englishResourceFile);
            fileWriterEnglish.close();

            FileWriter fileWriterDutch = new FileWriter(STRINGS_DUTCH_PATH);
            fileWriterDutch.write(dutchResourceFile);
            fileWriterDutch.close();

            FileWriter fileWriterGerman = new FileWriter(STRINGS_GERMAN_PATH);
            fileWriterGerman.write(germanResourceFile);
            fileWriterGerman.close();

            FileWriter fileWriterIngredientList = new FileWriter(INGREDIENT_LIST_PATH);
            fileWriterIngredientList.write(ingredientListFile);
            fileWriterIngredientList.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> generateStringResourceIdentifiers(List<String> englishNames) {
        return englishNames.stream().map(name ->
            name
                .toLowerCase()
                .split(",")[0]
                .split(" \\(")[0]
                .replace(" ", "_")
                    .replace("-", "_")
                .replace("'", "")
        ).collect(Collectors.toList());
    }

    private List<String> generateStringResources(List<String> stringResourceIdentifiers, List<String> strings) {
        List<String> stringResources = new ArrayList<>();

        for (int index = 0; index < stringResourceIdentifiers.size(); index++) {
            String identifier = stringResourceIdentifiers.get(index);
            String string = strings.get(index);

            string = string.replace("'", "\\'");

            String stringResource =
                    STRINGS_RESOURCE_TEMPLATE[0] +
                    identifier +
                    STRINGS_RESOURCE_TEMPLATE[1] +
                    string +
                    STRINGS_RESOURCE_TEMPLATE[2];

            stringResources.add(stringResource);
        }

        return stringResources;
    }

    private List<String> generateInfoStringResources(List<String> stringResourceIdentifiers, List<String> strings) {
        List<String> stringResources = new ArrayList<>();

        for (int index = 0; index < stringResourceIdentifiers.size(); index++) {
            String identifier = stringResourceIdentifiers.get(index);
            String string = strings.get(index);

            string = string.replace("'", "\\'");

            String stringResource =
                    STRINGS_RESOURCE_TEMPLATE[0] +
                    identifier +
                    "_info" +
                    STRINGS_RESOURCE_TEMPLATE[1] +
                    string +
                    STRINGS_RESOURCE_TEMPLATE[2];

            stringResources.add(stringResource);
        }

        return stringResources;
    }

    private List<String> generateIngredientList(List<String> stringResourceIdentifiers, List<String> ingredientTypes) {
        List<String> stringResources = new ArrayList<>();

        for (int index = 0; index < stringResourceIdentifiers.size(); index++) {
            String identifier = stringResourceIdentifiers.get(index);
            String ingredientType = ingredientTypes.get(index);

            String stringResource =
                    INGREDIENT_LIST_TEMPLATE[0] +
                    identifier +
                    INGREDIENT_LIST_TEMPLATE[1] +
                    ingredientType
                            .replace("No", "NOT_VEGAN")
                            .replace("Yes", "VEGAN")
                            .replace("Depends", "DEPENDS") +
                    INGREDIENT_LIST_TEMPLATE[2] +
                    identifier +
                    INGREDIENT_LIST_TEMPLATE[3];

            stringResources.add(stringResource);
        }

        return stringResources;
    }
}