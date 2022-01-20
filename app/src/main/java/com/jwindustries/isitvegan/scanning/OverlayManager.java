package com.jwindustries.isitvegan.scanning;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Size;

import androidx.core.content.ContextCompat;

import com.jwindustries.isitvegan.Ingredient;
import com.jwindustries.isitvegan.IngredientType;
import com.jwindustries.isitvegan.R;
import com.jwindustries.isitvegan.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OverlayManager {
    private static final List<Integer> BADGE_SIZES = Arrays
            .stream(new int[]{20, 60, 100})
            .boxed()
            .collect(Collectors.toList());
    private static final int BADGE_SIZE_THRESHOLD = 60; // Size of badge to change between normal and small version

    private Map<Ingredient, Rect> ingredientLocations;
    private final Map<Ingredient, Integer> droppedFrames;
    private static final int DROPPED_FRAMES_THRESHOLD = 10;

    private final Context context;
    private final GraphicOverlay overlay;

    public OverlayManager(Context context, GraphicOverlay graphicOverlay) {
        this.context = context;
        this.ingredientLocations = new HashMap<>();
        this.droppedFrames = new HashMap<>();
        this.overlay = graphicOverlay;
    }

    private Bitmap getBitmap(double bSize, IngredientType ingredientType) {
        int badgeSize = getSize(bSize);
        Drawable drawableBadge = ContextCompat.getDrawable(context, getBadgeResource(ingredientType, badgeSize < BADGE_SIZE_THRESHOLD));
        if (drawableBadge == null) {
            return null;
        }
        return drawableToBitmap(drawableBadge, badgeSize);
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

    private void updateOverlay(double badgeSize, Size cameraResolution) {
        overlay.clear();
        List<GraphicOverlay.Graphic> ingredientTypeGraphics = new ArrayList<>();
        for (Map.Entry<Ingredient, Rect> ingredientLocation : ingredientLocations.entrySet()) {
            IngredientType ingredientType = ingredientLocation.getKey().getIngredientType();
            Bitmap badgeBitmap = getBitmap(badgeSize, ingredientType);
            Rect boundingBox = ingredientLocation.getValue();
            IngredientIndicatorGraphic ingredientIndicatorGraphic = new IngredientIndicatorGraphic(
                    overlay,
                    badgeBitmap,
                    getLineColor(ingredientType),
                    boundingBox,
                    cameraResolution
            );
            ingredientTypeGraphics.add(ingredientIndicatorGraphic);
        }
        overlay.addAll(ingredientTypeGraphics);
    }

    private int getLineColor(IngredientType type) {
        switch (type) {
            case VEGAN:
                return context.getColor(R.color.colorSuccess);
            case DEPENDS:
                return context.getColor(R.color.colorWarning);
            case NOT_VEGAN:
                return context.getColor(R.color.colorError);
            default:
                return context.getColor(R.color.colorTertiary);
        }
    }

    public void updateIngredients(Map<Ingredient, Rect> foundIngredientLocations, Size cameraResolution) {
        Utils.debug(this, "-----------------");
        Utils.debug(this, "Ingredients found");
        Utils.debug(this, "-----------------");
        for (Ingredient ingredient : foundIngredientLocations.keySet()) {
            Utils.debug(this, "- " + ingredient.getEnglishName());
        }

        Set<Ingredient> previousIngredients = this.ingredientLocations.keySet();
        Map<Ingredient, Rect> newIngredientLocations = new HashMap<>();
        List<ImageTransform> imageTransformApproximations = new ArrayList<>();
        List<Integer> textHeights = new ArrayList<>();

        for (Ingredient foundIngredient : foundIngredientLocations.keySet()) {
            textHeights.add(foundIngredientLocations.get(foundIngredient).height());

            if (previousIngredients.contains(foundIngredient)) {
                /*
                 * Determine image transform
                 */
                Rect oldBoundingBox = this.ingredientLocations.get(foundIngredient);
                Rect newBoundingBox = foundIngredientLocations.get(foundIngredient);

                // Use width rather than height as it is larger thus produces a smaller error
                double imageZoom = (double) newBoundingBox.width() / oldBoundingBox.width();
                int imageTranslateX = (int) (newBoundingBox.left / imageZoom) - oldBoundingBox.left;
                int imageTranslateY = (int) (newBoundingBox.top / imageZoom) - oldBoundingBox.top;

                ImageTransform imageTransform = new ImageTransform(imageZoom, imageTranslateX, imageTranslateY);
                imageTransformApproximations.add(imageTransform);
            }

            /*
             * Add new ingredients
             */
            newIngredientLocations.put(foundIngredient, foundIngredientLocations.get(foundIngredient));
        }

        if (imageTransformApproximations.size() == 0) {
            Utils.debug(this, "Different image: dropping old and replacing with new");
            this.droppedFrames.clear();
        } else {
            ImageTransform averageImageTransform = ImageTransform.average(imageTransformApproximations);
            Utils.debug(this, "Average image transform");
            printTransform(averageImageTransform);

            for (Ingredient ingredient : previousIngredients) {
                if (!newIngredientLocations.containsKey(ingredient)) {
                    Utils.debug(this, "Lost ingredient: " + ingredient.getEnglishName());

                    /*
                     * Update dropped frames
                     */
                    int frames = droppedFrames.getOrDefault(ingredient, 0);
                    Utils.debug(this, "Dropped frames: " + (frames + 1));
                    if (frames == DROPPED_FRAMES_THRESHOLD) {
                        Utils.debug(this, "Dropped too much frames. Dropping " + ingredient);
                        droppedFrames.remove(ingredient);
                        continue;
                    }
                    droppedFrames.put(ingredient, frames + 1);

                    /*
                     * Add old ingredient with updated location
                     */
                    Rect oldLocation = this.ingredientLocations.get(ingredient);
                    Rect newLocation = translateElement(oldLocation, averageImageTransform);
                    newIngredientLocations.put(ingredient, newLocation);
                }
            }

            Utils.debug(this, "Result: Ingredient locations");
            for (Map.Entry<Ingredient, Rect> ingredientLocation : newIngredientLocations.entrySet()) {
                Utils.debug(this, ingredientLocation.getKey().getEnglishName());
                printRect(ingredientLocation.getValue());
            }
        }

        this.ingredientLocations = newIngredientLocations;

        double averageTextHeight = textHeights.stream().mapToDouble(a -> a).average().orElse(0);
        this.updateOverlay(2 * averageTextHeight, cameraResolution);
    }

    private Rect translateElement(Rect boundingBox, ImageTransform transform) {
        boundingBox.top = (int) ((boundingBox.top + transform.translationY) * transform.zoom);
        boundingBox.left = (int) ((boundingBox.left + transform.translationX) * transform.zoom);
        boundingBox.right = (int) ((boundingBox.right + transform.translationX) * transform.zoom);
        boundingBox.bottom = (int) ((boundingBox.bottom + transform.translationY) * transform.zoom);
        return boundingBox;
    }

    private void printRect(Rect rect) {
        Utils.debug(this,
                rect.left + ", " +
                        rect.top + ", " +
                        rect.right + ", " +
                        rect.bottom);
    }

    private void printTransform(ImageTransform imageTransform) {
        Utils.debug(this, imageTransform.zoom + ", " + imageTransform.translationX + ", " + imageTransform.translationY);
    }
}
