package com.sketchView.tools;

import android.graphics.Paint;
import android.graphics.Path;

public class SketchPath {
    public Path path;
    public int PathType;

    public SketchPath(Path path, int pathType) {
        this.path = path;
        PathType = pathType;
    }
}
