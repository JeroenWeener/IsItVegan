package com.jwindustries.isitvegan.helpers;

import android.graphics.Point;

/**
 * Helper to pass bounding boxes between UI thread and
 * render thread.
 */
public final class BoundingBoxHelper {
    private Point[] points;
    private Point[] keywordPoints;

    public void add(Point[] points) {
        this.points = points;
    }

    public void addKeyword(Point[] points) {
        keywordPoints = points;
    }

    public Point[] poll() {
        return points;
    }

    public Point[] pollKeyword() {
        return keywordPoints;
    }
}

