package com.jwindustries.isitvegan.helpers;

import android.graphics.Point;
import android.graphics.RectF;

/**
 * Helper to pass bounding boxes between UI thread and
 * render thread.
 */
public final class BoundingBoxHelper {
//    private final BlockingQueue<RectF> queuedBoundingBoxes = new ArrayBlockingQueue<>(16);
//
//    public void add(RectF boundingBox) {
//        queuedBoundingBoxes.offer(boundingBox);
//    }
//
//    public RectF poll() {
//        return queuedBoundingBoxes.poll();
//    }

//    private RectF lastBoundingBox;
//
//    public void add(RectF boundingBox) {
//        lastBoundingBox = boundingBox;
//    }
//
//    public RectF poll() {
//        return lastBoundingBox;
//    }

    private Point[] points;
    public void add(Point[] points) { this.points = points; }
    public Point[] poll() { return points; }
}

