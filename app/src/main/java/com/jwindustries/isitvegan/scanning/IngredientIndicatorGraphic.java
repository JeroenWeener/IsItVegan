package com.jwindustries.isitvegan.scanning;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Size;

import com.jwindustries.isitvegan.Utils;

/**
 * Graphic instance for rendering ingredient indicators
 */
public class IngredientIndicatorGraphic extends GraphicOverlay.Graphic {
    private final Bitmap badgeBitmap;
    private final Rect boundingBox;
    private final Size cameraResolution;
    private final Paint paint;

    private static final float LINE_STROKE_WIDTH = 2f;
    private static final int INDICATOR_ALPHA = 0x40;

    IngredientIndicatorGraphic(
            GraphicOverlay overlay,
            Bitmap badgeBitmap,
            int lineColor,
            Rect boundingBox,
            Size cameraResolution
    ) {
        super(overlay);

        this.badgeBitmap = badgeBitmap;

        this.paint = new Paint();
        paint.setColor(lineColor);
        paint.setStrokeWidth(LINE_STROKE_WIDTH);
        paint.setAlpha(INDICATOR_ALPHA);

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
        boundingBox.top *= (double) canvas.getHeight() / cameraResolution.getHeight();
        boundingBox.right *= (double) canvas.getWidth() / cameraResolution.getWidth();
        boundingBox.bottom *= (double) canvas.getHeight() / cameraResolution.getHeight();
        return boundingBox;
    }

    /**
     * Draws an indicator around the ingredient.
     * The left side of the indicator aligns with the left side of the ingredient. The right side of
     * the indicator ends a badge's length from the right side of the text as to not overlap with it.
     *
     * @param canvas - The canvas object the graphics will be drawn on.
     */
    @Override
    public void draw(Canvas canvas) {
        Rect translatedElementBoundingBox = translateCamera2Canvas(canvas, this.boundingBox);

        int badgeSize = this.badgeBitmap.getHeight();

        int textHeight = translatedElementBoundingBox.bottom - translatedElementBoundingBox.top;
        int textMiddle = translatedElementBoundingBox.top + textHeight / 2;

        int indicatorLeft = translatedElementBoundingBox.left;
        int indicatorTop = textMiddle - badgeSize / 2;
        int indicatorRight = translatedElementBoundingBox.right + badgeSize;
        int indicatorBottom = textMiddle + badgeSize / 2;

        // Draw indicator
        RectF roundedRectangleBounds = new RectF(
                indicatorLeft,
                indicatorTop,
                indicatorRight,
                indicatorBottom
        );
        canvas.drawRoundRect(
                roundedRectangleBounds,
                badgeSize / 2f,
                badgeSize / 2f,
                paint
        );

        // Draw badge
        canvas.drawBitmap(
                this.badgeBitmap,
                indicatorRight - badgeSize,
                indicatorTop,
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