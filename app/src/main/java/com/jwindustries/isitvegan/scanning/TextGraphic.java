package com.jwindustries.isitvegan.scanning;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.google.mlkit.vision.text.Text;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class TextGraphic extends GraphicOverlay.Graphic {

    private static final String TAG = "TextGraphic";
    private static final int TEXT_COLOR = Color.RED;
    private static final float TEXT_SIZE = 54.0f;
    private static final float STROKE_WIDTH = 4.0f;

    private final Paint rectPaint;
    private final Paint textPaint;
    private final Text.Element element;

    TextGraphic(GraphicOverlay overlay, Text.Element element) {
        super(overlay);

        this.element = element;

        rectPaint = new Paint();
        rectPaint.setColor(TEXT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);

        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Log.d(TAG, "on draw text graphic");
        if (element == null) {
            throw new IllegalStateException("Attempting to draw a null text.");
        }

        Paint markerPaint = new Paint();
        markerPaint.setColor(Color.GREEN);
        markerPaint.setStyle(Paint.Style.FILL);
        RectF topLeftMarker = new RectF(new Rect(0, 0, 10, 10));
        RectF topRightMarker = new RectF(new Rect(1070, 0, 1080, 10));
        RectF bottomLeftMarker = new RectF(new Rect(0, 800, 10, 810));
        RectF bottomRightMarker = new RectF(new Rect(1070, 800, 10800, 810));
        canvas.drawRect(topLeftMarker, markerPaint);
        canvas.drawRect(topRightMarker, markerPaint);
        canvas.drawRect(bottomLeftMarker, markerPaint);
        canvas.drawRect(bottomRightMarker, markerPaint);

        // Draws the bounding box around the TextBlock.
        RectF rect = new RectF(element.getBoundingBox());
//        rect.left *= 1080. / 640;
//        rect.right *= 1080. / 640;
//        rect.top *= 810. / 480;
//        rect.bottom *= 810. / 480;
        canvas.drawRect(rect, rectPaint);

        // Renders the text at the bottom of the box.
        canvas.drawText(element.getText(), rect.left, rect.bottom, textPaint);

        Log.d("DRAW5", "Bounding box");
        Log.d("DRAW5", String.valueOf(element.getBoundingBox().top));
        Log.d("DRAW5", String.valueOf(element.getBoundingBox().right));
        Log.d("DRAW5", String.valueOf(element.getBoundingBox().bottom));
        Log.d("DRAW5", String.valueOf(element.getBoundingBox().left));

        Log.d("DRAW5", "Canvas");
        Log.d("DRAW5", String.valueOf(canvas.getWidth()));
        Log.d("DRAW5", String.valueOf(canvas.getHeight()));

        Log.d("DRAW5", "-----------------");
    }
}