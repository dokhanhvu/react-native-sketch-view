package com.sketchView.tools;

import android.graphics.*;
import android.view.View;

import com.sketchView.utils.ToolUtils;

/**
 * Created by keshav on 08/04/17.
 */
public class PenSketchTool extends PathTrackingSketchTool implements ToolThickness, ToolColor {

    private static final float DEFAULT_THICKNESS = 5;
    private static final int DEFAULT_COLOR = Color.BLACK;

    private float toolThickness;
    private int toolColor;

    public Paint paint = new Paint();

    public PenSketchTool(View touchView) {
        super(touchView);

        setToolColor(DEFAULT_COLOR);
        setToolThickness(DEFAULT_THICKNESS);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(15);
        this.moveDistance = new PointF(0, 0);
    }

    @Override
    public void render(Canvas canvas) {
        paint.setColor(toolColor);
        paint.setStrokeWidth(toolThickness);
        canvas.drawPath(path, paint);
    }

    @Override
    public void render(Canvas canvas, Path path, int color, float thickness) {
        paint.setColor(color);
        paint.setStrokeWidth(thickness);
        canvas.drawPath(path, paint);
    }

    @Override
    public int getType() {
        return SketchTool.TYPE_PEN;
    }

    @Override
    public void setToolThickness(float toolThickness) {
        this.toolThickness = ToolUtils.ConvertDPToPixels(touchView.getContext(), toolThickness);
    }

    @Override
    public float getToolThickness() {
        return toolThickness;
    }

    @Override
    public void setToolColor(int toolColor) {
        this.toolColor = toolColor;
    }

    @Override
    public int getToolColor() {
        return toolColor;
    }

    @Override
    public SketchPath getPath() {
        return new SketchPath(path, getType(), toolColor, toolThickness);
    }
}