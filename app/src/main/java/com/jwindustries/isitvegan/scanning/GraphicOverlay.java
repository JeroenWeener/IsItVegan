package com.jwindustries.isitvegan.scanning;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.jwindustries.isitvegan.Utils;

import java.util.HashSet;
import java.util.Set;

public class GraphicOverlay extends View {
    private final Object lock = new Object();
    private final Set<Graphic> graphics = new HashSet<>();

    // Debugging
    private final static int MARKER_SIZE = 10;
    private final static int MARKER_COLOR = Color.GREEN;

    public abstract static class Graphic {
        private final GraphicOverlay overlay;

        public Graphic(GraphicOverlay overlay) {
            this.overlay = overlay;
        }

        public abstract void draw(Canvas canvas);

        public void postInvalidate() {
            overlay.postInvalidate();
        }
    }

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void clear() {
        synchronized (lock) {
            graphics.clear();
        }
        postInvalidate();
    }

    public void add(Graphic graphic) {
        synchronized (lock) {
            graphics.add(graphic);
        }
        postInvalidate();
    }

    public void remove(Graphic graphic) {
        synchronized (lock) {
            graphics.remove(graphic);
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (Utils.DEBUG) {
            drawMarkers(canvas);
        }

        synchronized (lock) {
            for (Graphic graphic : graphics) {
                graphic.draw(canvas);
            }
        }
    }

    private void drawMarkers(Canvas canvas) {
        Paint markerPaint = new Paint();
        markerPaint.setColor(MARKER_COLOR);
        markerPaint.setStyle(Paint.Style.FILL);

        RectF topLeftMarker = new RectF(
                0,
                0,
                MARKER_SIZE,
                MARKER_SIZE
        );
        RectF topRightMarker = new RectF(
                canvas.getWidth() - MARKER_SIZE,
                0,
                canvas.getWidth(),
                MARKER_SIZE
        );
        RectF bottomLeftMarker = new RectF(
                0,
                canvas.getHeight() - MARKER_SIZE,
                MARKER_SIZE,
                canvas.getHeight()
        );
        RectF bottomRightMarker = new RectF(
                canvas.getWidth() - MARKER_SIZE,
                canvas.getHeight() - MARKER_SIZE,
                canvas.getWidth(),
                canvas.getHeight()
        );
        canvas.drawRect(topLeftMarker, markerPaint);
        canvas.drawRect(topRightMarker, markerPaint);
        canvas.drawRect(bottomLeftMarker, markerPaint);
        canvas.drawRect(bottomRightMarker, markerPaint);
    }
}