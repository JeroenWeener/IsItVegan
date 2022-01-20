package com.jwindustries.isitvegan.scanning;

import java.util.Collection;

public class ImageTransform {
    public double zoom;
    public int translationX;
    public int translationY;

    public ImageTransform(double zoom, int translationX, int translationY) {
        this.zoom = zoom;
        this.translationX = translationX;
        this.translationY = translationY;
    }

    public ImageTransform() {
        this(1, 0, 0);
    }

    public static ImageTransform average(Collection<ImageTransform> imageTransforms) {
        double averageZoom = imageTransforms.stream().map(element -> element.zoom).reduce(0., Double::sum) / imageTransforms.size();
        int averageTranslateX = imageTransforms.stream().map(element -> element.translationX).reduce(0, Integer::sum) / imageTransforms.size();
        int averageTranslateY = imageTransforms.stream().map(element -> element.translationY).reduce(0, Integer::sum) / imageTransforms.size();
        return new ImageTransform(averageZoom, averageTranslateX, averageTranslateY);
    }
}
