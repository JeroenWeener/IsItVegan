package com.jwindustries.isitvegan;

import android.media.Image;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;

import org.jetbrains.annotations.NotNull;

public class TextReadAnalyzer implements ImageAnalysis.Analyzer {
    private final TextFoundListener textFoundListener;

    public TextReadAnalyzer(TextFoundListener textFoundListener) {
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
        readTextFromImage(InputImage.fromMediaImage(image, rotation), imageProxy);
    }

    private void readTextFromImage(InputImage image, ImageProxy imageProxy) {
        TextRecognition.getClient()
                .process(image)
                .addOnSuccessListener(visionText -> {
                    processTextFromImage(visionText);
                    imageProxy.close();
                })
                .addOnFailureListener(error -> {
                    error.printStackTrace();
                    imageProxy.close();
                });
    }

    private void processTextFromImage(Text visionText) {
        this.textFoundListener.onTextFound(visionText.getText());
    }
}
