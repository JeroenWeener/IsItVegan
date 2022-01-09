package com.jwindustries.isitvegan.scanning;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Size;

import com.google.mlkit.vision.text.Text;
import com.jwindustries.isitvegan.Utils;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class TextGraphic extends GraphicOverlay.Graphic {
    private static final int TEXT_COLOR = Color.RED;
    private static final float TEXT_SIZE = 54.0f;
    private static final float STROKE_WIDTH = 4.0f;

    private final Paint rectPaint;
    private final Paint textPaint;
    private final Text.Element element;
    private final Size cameraResolution;

    TextGraphic(GraphicOverlay overlay, Text.Element element, Size cameraResolution) {
        super(overlay);

        this.element = element;

        rectPaint = new Paint();
        rectPaint.setColor(TEXT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);

        this.cameraResolution = cameraResolution;

        // Redraw the overlay, as this graphic has been added
        postInvalidate();
    }

    /**
     * Translates the bounding box from the camera's coordinate system to the canvas' coordinate system.
     * Returns the same instance of the bounding box, not a new object.
     * @param canvas the canvas
     * @param boundingBox the bounding box in the camera's coordinate system
     * @return the same bounding box with translated coordinates
     */
    private RectF translateCamera2Canvas(Canvas canvas, RectF boundingBox) {
        boundingBox.left *= (double) canvas.getWidth() / cameraResolution.getWidth();
        boundingBox.right *= (double) canvas.getWidth() / cameraResolution.getWidth();
        boundingBox.top *= (double) canvas.getHeight() / cameraResolution.getHeight();
        boundingBox.bottom *= (double) canvas.getHeight() / cameraResolution.getHeight();
        return boundingBox;
    }

    @Override
    public void draw(Canvas canvas) {
        Utils.debug(this, "Canvas size");
        Utils.debug(this, String.valueOf(canvas.getWidth()));
        Utils.debug(this, String.valueOf(canvas.getHeight()));

        if (element == null || element.getBoundingBox() == null) {
            throw new IllegalStateException("Attempting to draw an invalid element");
        }

        // Draw bounding box
        RectF translatedElementBoundingBox = translateCamera2Canvas(canvas, new RectF(element.getBoundingBox()));
        canvas.drawRect(translatedElementBoundingBox, rectPaint);

        // Draw text
        canvas.drawText(element.getText(), translatedElementBoundingBox.left, translatedElementBoundingBox.bottom, textPaint);
    }
}