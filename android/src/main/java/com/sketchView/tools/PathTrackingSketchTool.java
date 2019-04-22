package com.sketchView.tools;

import android.graphics.Path;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by keshav on 08/04/17.
 */

public abstract class PathTrackingSketchTool extends SketchTool {

    Path path = new Path();

    PathTrackingSketchTool(View touchView) {
        super(touchView);
    }

    @Override
    public void clear() {
        path = new Path();
    }

    @Override
    void onTouchDown(MotionEvent event) {
        path.moveTo((event.getX() - moveDistance.x)/factor, (event.getY() - moveDistance.y)/factor);
    }

    @Override
    void onTouchMove(MotionEvent event) {
        path.lineTo((event.getX() - moveDistance.x)/factor, (event.getY() - moveDistance.y)/factor);
        touchView.invalidate();
    }

    @Override
    void onTouchUp(MotionEvent event) {
        path.lineTo((event.getX() - moveDistance.x)/factor, (event.getY() - moveDistance.y)/factor);
        touchView.invalidate();
    }

    @Override
    void onTouchCancel(MotionEvent event) {
        onTouchUp(event);
    }

    @Override
    public void setScaleFactor(float factor) {
        this.factor = factor;
    }

    @Override
    public void setMoveDistance(PointF mid) {
        this.moveDistance = mid;
    }

    @Override
    public void reset() {
        moveDistance.set(0, 0);
        factor = 1.f;
    }
}