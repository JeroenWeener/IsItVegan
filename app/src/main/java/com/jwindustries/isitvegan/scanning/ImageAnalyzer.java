package com.jwindustries.isitvegan.scanning;

import android.media.Image;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ImageAnalyzer implements ImageAnalysis.Analyzer {
    private final BarcodeFoundListener barcodeFoundListener;
    private final BarcodeScannerOptions barcodeOptions;
    private final TextFoundListener textFoundListener;

    public ImageAnalyzer(
            BarcodeFoundListener barcodeFoundListener,
            TextFoundListener textFoundListener
    ) {
        this.barcodeFoundListener = barcodeFoundListener;
        this.barcodeOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_EAN_13
                ).build();

        this.textFoundListener = textFoundListener;
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
        return TextRecognition.getClient()
                .process(image)
                .addOnSuccessListener(this::processTextFromImage);
    }

    private void processBarcodeFromImage(Barcode barcode) {
        this.barcodeFoundListener.onBarcodeFound(barcode.getDisplayValue());
    }

    private void processTextFromImage(Text visionText) {
        this.textFoundListener.onTextFound(visionText.getText());
    }
}