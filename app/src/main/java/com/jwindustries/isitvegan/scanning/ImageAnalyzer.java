package com.jwindustries.isitvegan.scanning;

import android.media.Image;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.jwindustries.isitvegan.Ingredient;
import com.jwindustries.isitvegan.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImageAnalyzer implements ImageAnalysis.Analyzer {
    private final List<Ingredient> ingredientList;
    private final BarcodeFoundListener barcodeFoundListener;
    private final BarcodeScannerOptions barcodeOptions;
    private final IngredientsFoundListener ingredientsFoundListener;

    public ImageAnalyzer(
            List<Ingredient> ingredientList,
            BarcodeFoundListener barcodeFoundListener,
            IngredientsFoundListener ingredientsFoundListener
    ) {
        this.ingredientList = ingredientList;
        this.barcodeFoundListener = barcodeFoundListener;
        this.barcodeOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_EAN_13
                ).build();
        this.ingredientsFoundListener = ingredientsFoundListener;
    }

    @Override
    @androidx.camera.core.ExperimentalGetImage
    public void analyze(@NotNull ImageProxy imageProxy) {
        Image image = imageProxy.getImage();
        if (image != null) {
            process(image, imageProxy);
        }
    }

    private void process(Image image, ImageProxy imageProxy) {
        int rotation = imageProxy.getImageInfo().getRotationDegrees();
        InputImage inputImage = InputImage.fromMediaImage(image, rotation);

//        Utils.debug(this, "Image size");
//        Utils.debug(this, String.valueOf(inputImage.getWidth()));
//        Utils.debug(this, String.valueOf(inputImage.getHeight()));

        readBarcodeFromImage(inputImage)
                .continueWithTask(result -> readTextFromImage(inputImage))
                .addOnCompleteListener(complete -> imageProxy.close());
    }

    private Task<List<Barcode>> readBarcodeFromImage(InputImage image) {
        return BarcodeScanning.getClient(this.barcodeOptions)
                .process(image)
                .addOnSuccessListener(visionBarcode -> {
                    if (visionBarcode.size() > 0) {
                        processBarcodeFromImage(visionBarcode.get(0));
                    }
                });
    }

    private Task<Text> readTextFromImage(InputImage image) {
        return TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener(this::processTextFromImage);
    }

    private void processBarcodeFromImage(Barcode barcode) {
        this.barcodeFoundListener.onBarcodeFound(barcode.getDisplayValue());
    }

    private void processTextFromImage(Text visionText) {
        List<IngredientElement> ingredientElements = new ArrayList<>();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                for (Text.Element element : line.getElements()) {
                    String normalizedText = Utils.normalizeString(element.getText(), false);
                    Optional<Ingredient> ingredientOptional = this.ingredientList
                            .stream()
                            .filter(ingredient -> ingredient.matches(normalizedText))
                            .findAny();
                    if (ingredientOptional.isPresent()) {
                        Ingredient ingredient = ingredientOptional.get();
                        Utils.debug(this, "Ingredient found in image: " + ingredient.getEnglishName());
                        ingredientElements.add(new IngredientElement(ingredient, element));
                    }
                }
            }
        }
        ingredientsFoundListener.onIngredientsFound(ingredientElements);
    }
}