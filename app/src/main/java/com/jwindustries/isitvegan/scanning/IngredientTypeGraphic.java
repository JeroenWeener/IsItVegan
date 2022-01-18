package com.jwindustries.isitvegan.scanning;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Size;

import com.jwindustries.isitvegan.Utils;

/**
 * Graphic instance for rendering ingredient type badges
 */
public class IngredientTypeGraphic extends GraphicOverlay.Graphic {
    private final Bitmap badgeBitmap;
    private final Rect boundingBox;
    private final Size cameraResolution;
    private final Paint linePaint;

    private static final float LINE_STROKE_WIDTH = 2f;

    IngredientTypeGraphic(
            GraphicOverlay overlay,
            Bitmap badgeBitmap,
            int lineColor,
            Rect boundingBox,
            Size cameraResolution
    ) {
        super(overlay);

        this.badgeBitmap = badgeBitmap;

        this.linePaint = new Paint();
        linePaint.setStrokeWidth(LINE_STROKE_WIDTH);
        linePaint.setColor(lineColor);

        this.boundingBox = boundingBox;
        this.cameraResolution = cameraResolution;

        // Redraw the overlay, as this graphic has been added
        postInvalidate();
    }

    /**
     * Translates the bounding box from the camera's coordinate system to the canvas' coordinate system.
     * Returns the same instance of the bounding box, not a new object.
     *
     * @param canvas      the canvas
     * @param boundingBox the bounding box in the camera's coordinate system
     * @return the same bounding box with translated coordinates
     */
    private Rect translateCamera2Canvas(Canvas canvas, Rect boundingBox) {
        boundingBox.left *= (double) canvas.getWidth() / cameraResolution.getWidth();
        boundingBox.right *= (double) canvas.getWidth() / cameraResolution.getWidth();
        boundingBox.top *= (double) canvas.getHeight() / cameraResolution.getHeight();
        boundingBox.bottom *= (double) canvas.getHeight() / cameraResolution.getHeight();
        return boundingBox;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect translatedElementBoundingBox = translateCamera2Canvas(canvas, this.boundingBox);

        canvas.drawLine(
                translatedElementBoundingBox.left,
                translatedElementBoundingBox.bottom,
                translatedElementBoundingBox.right,
                translatedElementBoundingBox.bottom,
                linePaint
        );

        canvas.drawBitmap(
                this.badgeBitmap,
                translatedElementBoundingBox.right - (int) (.5 * this.badgeBitmap.getWidth()),
                translatedElementBoundingBox.bottom - (int) (.5 * this.badgeBitmap.getHeight()),
                null
        );


        if (Utils.DEBUG) {
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4.0f);
            canvas.drawRect(translatedElementBoundingBox, paint);
        }
    }
}