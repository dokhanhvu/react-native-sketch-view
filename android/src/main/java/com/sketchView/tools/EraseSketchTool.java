package com.sketchView.tools;

import android.graphics.*;
import android.view.View;

import com.sketchView.utils.ToolUtils;

/**
 * Created by keshav on 08/04/17.
 */
public class EraseSketchTool extends PathTrackingSketchTool implements ToolThickness {

    private static final float DEFAULT_THICKNESS = 10;

    private Paint paint = new Paint();

    private float toolThickness;

    public EraseSketchTool(View touchView) {
        super(touchView);

        setToolThickness(DEFAULT_THICKNESS);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        this.moveDistance = new PointF(0, 0);
    }

    @Override
    public void render(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    @Override
    public void render(Canvas canvas, Path path) {
        canvas.drawPath(path, paint);
    }

    @Override
    public int getType() {
        return SketchTool.TYPE_ERASE;
    }

    @Override
    public void setToolThickness(float thickness) {
        toolThickness = thickness;
        paint.setStrokeWidth(ToolUtils.ConvertDPToPixels(touchView.getContext(), toolThickness));
    }

    @Override
    public float getToolThickness() {
        return toolThickness;
    }
}
