package com.jwindustries.isitvegan.scanning;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Size;

import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.text.Text;
import com.jwindustries.isitvegan.IngredientType;
import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.Utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Graphic instance for rendering ingredient type badges
 */
public class IngredientTypeGraphic extends GraphicOverlay.Graphic {
    private static final List<Integer> BADGE_SIZES = Arrays
            .stream(new int[]{20, 60, 100})
            .boxed()
            .collect(Collectors.toList());
    private static final int BADGE_SIZE_THRESHOLD = 60; // Size of badge to change between normal and small version

    private final Context context;
    private final Text.Element element;
    private final Size cameraResolution;
    private final IngredientType ingredientType;
    private final double proposedBadgeSize;

    IngredientTypeGraphic(
            Context context,
            GraphicOverlay overlay,
            Text.Element element,
            Size cameraResolution,
            IngredientType ingredientType,
            double proposedBadgeSize
    ) {
        super(overlay);

        this.context = context;
        this.element = element;
        this.cameraResolution = cameraResolution;
        this.ingredientType = ingredientType;
        this.proposedBadgeSize = proposedBadgeSize;

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

        RectF translatedElementBoundingBox = translateCamera2Canvas(canvas, new RectF(element.getBoundingBox()));
        int badgeSize = getSize(this.proposedBadgeSize);
        Drawable drawableBadge = ContextCompat.getDrawable(context, getBadgeResource(ingredientType, badgeSize < BADGE_SIZE_THRESHOLD));
        if (drawableBadge == null) {
            return;
        }
        Bitmap bitmap = drawableToBitmap(drawableBadge, badgeSize);
        canvas.drawBitmap(
                bitmap,
                translatedElementBoundingBox.right - (int) (.5 * badgeSize),
                translatedElementBoundingBox.bottom - (int) (.5 * badgeSize),
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

    private static Bitmap drawableToBitmap(Drawable drawable, int badgeSize) {
        Bitmap bitmap = Bitmap.createBitmap(badgeSize, badgeSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private static int getSize(double proposedSize) {
        return BADGE_SIZES.stream().min(Comparator.comparingInt(size -> Math.abs(size - (int) proposedSize))).orElse(0);
    }

    private static int getBadgeResource(IngredientType ingredientType, boolean isSmall) {
        switch (ingredientType) {
            case VEGAN:
                return isSmall ? R.drawable.vegan_badge_small : R.drawable.vegan_badge;
            case DEPENDS:
                return isSmall ? R.drawable.depends_badge_small : R.drawable.depends_badge;
            case NOT_VEGAN:
                return isSmall ? R.drawable.not_vegan_badge_small : R.drawable.not_vegan_badge;
            default:
                return -1;
        }
    }
}