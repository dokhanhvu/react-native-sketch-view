package com.sketchView.tools;

import android.graphics.Paint;
import android.graphics.Path;

public class SketchPath {
    public Path path;
    public int pathType;
    public int color;
    public float thickness;

    public SketchPath(Path path, int pathType, int color, float thickness) {
        this.path = path;
        this.pathType = pathType;
        this.color = color;
        this.thickness = thickness;
    }

    public SketchPath(Path path, int pathType) {
        this.path = path;
        this.pathType = pathType;
        this.color = Color.WHITE;
        this.thickness = 15;
    }

}
