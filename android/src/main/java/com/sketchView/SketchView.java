package com.sketchView;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import com.sketchView.tools.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by keshav on 05/04/17.
 */

public class SketchView extends View {

    SketchTool currentTool;
    SketchTool penTool;
    SketchTool eraseTool;

    final List<SketchPath> paths = new ArrayList<>();
    List<SketchPath> savePaths = new ArrayList<>();

    private float mPrimStartTouchEventX = -1;
    private float mPrimStartTouchEventY = -1;
    private float mSecStartTouchEventX = -1;
    private float mSecStartTouchEventY = -1;

    private float mCurrentScale = 1.0f;
    private float oldDist = 1.0f;
    private float MAX_ZOOM = 5.0f;
    private float MIN_ZOOM = 1.0f;
    private float mViewScaledTouchSlop;
    private float[] values = new float[9];

    private Matrix mCurrentMatrix;
    private Matrix savedMatrix;

    private PointF mid = new PointF(0, 0);
    private PointF start = new PointF(0, 0);

    private boolean isDraw = false;
    private boolean isTwoFinger = false;

    private BitmapDrawable drawable;
    private Bitmap incrementalImage;

    private ScaleGestureDetector mScaleDetector;

    public SketchView(Context context) {
        this(context, null);
    }

    public SketchView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public SketchView(Context context, AttributeSet attr, int defaultStyleAttr) {
        super(context, attr, defaultStyleAttr);
        penTool = new PenSketchTool(this);
        eraseTool = new EraseSketchTool(this);
        setToolType(SketchTool.TYPE_PEN);
        setBackgroundColor(Color.TRANSPARENT);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener(this));
        mCurrentMatrix = new Matrix();
        savedMatrix = new Matrix();
        setFocusable(true);
        final ViewConfiguration viewConfig = ViewConfiguration.get(context);
        mViewScaledTouchSlop = viewConfig.getScaledTouchSlop();
    }

    public void setToolType(int toolType) {
        switch (toolType) {
            case SketchTool.TYPE_PEN:
                currentTool = penTool;
                break;
            case SketchTool.TYPE_ERASE:
                currentTool = eraseTool;
                break;
            default:
                currentTool = penTool;
        }
    }

    public void setViewImage(Bitmap bitmap) {
        drawable = new BitmapDrawable(getResources(), bitmap);
        drawable.setBounds(0, 0, getWidth(), getHeight());
        incrementalImage = Bitmap.createScaledBitmap(drawable.getBitmap(), getWidth(), getHeight(), false);
        invalidate();
    }

    public Bitmap getBitMap(){
        Bitmap drawableBitmap = drawable.getBitmap();
        Bitmap bmOverlay = drawableBitmap.copy(drawableBitmap.getConfig(), true);
        Canvas canvas = new Canvas(bmOverlay);
        float scaleWidth = (float)bmOverlay.getWidth() / (float)getWidth();
        float scaleHeight = (float)bmOverlay.getHeight() / (float)getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        canvas.setMatrix(matrix);
        drawPath(canvas, savePaths);
        return bmOverlay;
    }

    private void drawPath(Canvas canvas, List<SketchPath> drawPaths){
        for(SketchPath path : drawPaths){
            switch (path.pathType){
                case SketchTool.TYPE_ERASE:
                    eraseTool.render(canvas, path.path, path.color, path.thickness);
                    break;
                case SketchTool.TYPE_PEN:
                default:
                    penTool.render(canvas, path.path, path.color, path.thickness);
            }
        }
    }

    private void drawBitmap(){
        Bitmap bm = incrementalImage.copy(incrementalImage.getConfig(), true);
        Canvas canvas = new Canvas(bm);
//        canvas.setMatrix(mCurrentMatrix);
        drawPath(canvas, paths);
        paths.clear();
        incrementalImage = bm;
        invalidate();
    }

    public void setColor(double red, double green, double blue, double alpha){
        String r = Integer.toHexString((int) red);
        String re = r.length() < 2 ? "0"+ r : r;
        String g = Integer.toHexString((int) green);
        String gr = g.length() < 2 ? "0"+ g : g;
        String b = Integer.toHexString((int) blue);
        String bl = b.length() < 2 ? "0"+ b : b;
        String colorString = alpha > 0.5 ? "#FF" + re + gr + bl : "#4D" +  re + gr + bl;
        Integer color = Color.parseColor(colorString);
        ((ToolColor)penTool).setToolColor(color);
    }

    public void setThickness(float thickness){
        ((ToolThickness)penTool).setToolThickness(thickness);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        if(drawable == null){
            drawable = new BitmapDrawable(getResources(), Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565));
        }
        canvas.setMatrix(mCurrentMatrix);
        canvas.drawBitmap(incrementalImage, 0, 0, null);
        if(isDraw)
            currentTool.render(canvas);
//        drawPath(canvas);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                isTwoFinger = true;
                isDraw = false;
                mSecStartTouchEventX = ev.getX(1);
                mSecStartTouchEventY = ev.getY(1);
                savedMatrix.set(mCurrentMatrix);
                midPoint(mid, ev);
                mScaleDetector.onTouchEvent(ev);
                break;
            case MotionEvent.ACTION_DOWN:
                isDraw = true;
                isTwoFinger = false;

                start.set(ev.getX(), ev.getY());
                mPrimStartTouchEventX = ev.getX(0);
                mPrimStartTouchEventY = ev.getY(0);
                mCurrentMatrix.getValues(values);
                savedMatrix.set(mCurrentMatrix);

                penTool.setMoveDistance(new PointF(values[Matrix.MTRANS_X], values[Matrix.MTRANS_Y]));
                penTool.setScaleFactor(mCurrentScale);
                eraseTool.setMoveDistance(new PointF(values[Matrix.MTRANS_X], values[Matrix.MTRANS_Y]));
                eraseTool.setScaleFactor(mCurrentScale);
                currentTool.onTouch(this, ev);

                break;
            case MotionEvent.ACTION_MOVE: {
                oldDist = distance(start.x, start.y, ev.getX(), ev.getY());
                if (isDraw) {
                    if (oldDist > 10.0f) {
                        currentTool.onTouch(this, ev);
                        paths.add(currentTool.getPath());
                        savePaths.add(currentTool.getPath());
                        if(paths.size() > 5)
                        {
                            synchronized(paths){
                                drawBitmap();
                            }
                        }
                    }
                } else if (isTwoFinger) {
                    boolean isSecMoving = (isScrollGesture(ev, 1, mSecStartTouchEventX, mSecStartTouchEventY));
                    if (isPinchGesture(ev)) {
                        mScaleDetector.onTouchEvent(ev);
                    } else if (isSecMoving) {
                        if(oldDist > 10.0f && !mScaleDetector.isInProgress()) {
                            mCurrentMatrix.getValues(values);
                            mCurrentMatrix.set(savedMatrix);
                            float dx = (ev.getX() - start.x);
                            float dy = (ev.getY() - start.y);
                            mCurrentMatrix.postTranslate(dx, dy);
                            limitDrag(mCurrentMatrix, this, getWidth(), getHeight());
                            mCurrentMatrix.getValues(values);
                            invalidate();
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                isTwoFinger = false;
                isDraw = false;
                mSecStartTouchEventX = -1;
                mSecStartTouchEventY = -1;
                break;
            case MotionEvent.ACTION_UP: {
                mPrimStartTouchEventX = -1;
                mPrimStartTouchEventY = -1;
                if (isDraw) {
                    mCurrentMatrix.set(savedMatrix);
                    drawBitmap();
                    currentTool.clear();
                } else {
                    mScaleDetector.onTouchEvent(ev);
                }
                isDraw = false;
            }
        }
        return true;
    }

    private void limitDrag(Matrix m, View view, int imageWidth, int imageHeight) {
        float[] values = new float[9];
        m.getValues(values);
        float[] orig = new float[] {0,0, imageWidth, imageHeight};
        float[] trans = new float[4];
        m.mapPoints(trans, orig);

        float transLeft = trans[0];
        float transTop = trans[1];
        float transRight = trans[2];
        float transBottom = trans[3];
        float transWidth = transRight - transLeft;
        float transHeight = transBottom - transTop;

        float xOffset = 0;
        if (transWidth > view.getWidth()) {
            if (transLeft > 0) {
                xOffset = -transLeft;
            } else if (transRight < view.getWidth()) {
                xOffset = view.getWidth() - transRight;
            }
        } else {
            if (transLeft < 0) {
                xOffset = -transLeft;
            } else if (transRight > view.getWidth()) {
                xOffset = -(transRight - view.getWidth());
            }
        }

        float yOffset = 0;
        if (transHeight > view.getHeight()) {
            if (transTop > 0) {
                yOffset = -transTop;
            } else if (transBottom < view.getHeight()) {
                yOffset = view.getHeight() - transBottom;
            }
        } else {
            if (transTop < 0) {
                yOffset = -transTop;
            } else if (transBottom > view.getHeight()) {
                yOffset = -(transBottom - view.getHeight());
            }
        }

        float transX = values[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y];

        values[Matrix.MTRANS_X] = transX + xOffset;
        values[Matrix.MTRANS_Y] = transY + yOffset;
        m.setValues(values);
    }

    private boolean isPinchGesture(MotionEvent event) {
        if (event.getPointerCount() > 1) {
            final float distanceCurrent = distance(event, 0, 1);
            final float diffPrimX = mPrimStartTouchEventX - event.getX(0);
            final float diffPrimY = mPrimStartTouchEventY - event.getY(0);
            final float diffSecX = mSecStartTouchEventX - event.getX(1);
            final float diffSecY = mSecStartTouchEventY - event.getY(1);

            float mPrimSecStartTouchDistance = 0;
            // if the distance between the two fingers has increased past
            // our threshold
            return Math.abs(distanceCurrent - mPrimSecStartTouchDistance) > mViewScaledTouchSlop
                    // and the fingers are moving in opposing directions
                    && (diffPrimY * diffSecY) <= 0
                    && (diffPrimX * diffSecX) <= 0;
        }

        return false;
    }

    private boolean isScrollGesture(MotionEvent event, int ptrIndex, float originalX, float originalY) {
        float moveX = Math.abs(event.getX(ptrIndex) - originalX);
        float moveY = Math.abs(event.getY(ptrIndex) - originalY);

        if (moveX > mViewScaledTouchSlop || moveY > mViewScaledTouchSlop) {
            return true;
        }
        return false;
    }

    public void reset() {
        savedMatrix.reset();
        mCurrentMatrix.reset();
        mCurrentScale = 1.0f;
        penTool.reset();
        eraseTool.reset();
        isDraw = false;
        isTwoFinger = false;
        invalidate();
    }

    private void midPoint(PointF point, MotionEvent event) {
        // ...
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private float distance(float x0, float y0, float x1, float y1) {
        // ...
        float x = x0 - x1;
        float y = y0 - y1;
        return (float) Math.sqrt(x * x + y * y);
    }

    public float distance(MotionEvent event, int first, int second) {
        if (event.getPointerCount() >= 2) {
            final float x = event.getX(first) - event.getX(second);
            final float y = event.getY(first) - event.getY(second);

            return (float) Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        View view;

        ScaleListener(View v) {
            view = v;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            savedMatrix.set(mCurrentMatrix);
            super.onScaleEnd(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float newScale = mCurrentScale * scaleFactor;
            // Prevent from zooming out more than original
            if (newScale > MIN_ZOOM) {
                if(newScale > MAX_ZOOM) {
                    scaleFactor = MAX_ZOOM / mCurrentScale;
                    mCurrentScale = MAX_ZOOM;
                } else
                    mCurrentScale = newScale;
                mCurrentMatrix.postScale(scaleFactor, scaleFactor, mid.x, mid.y);
                limitDrag(mCurrentMatrix, view, getWidth(), getHeight());
                mCurrentMatrix.getValues(values);
                penTool.setMoveDistance(new PointF(values[Matrix.MTRANS_X], values[Matrix.MTRANS_Y]));
                eraseTool.setMoveDistance(new PointF(values[Matrix.MTRANS_X], values[Matrix.MTRANS_Y]));
                invalidate();
            } else if (newScale < MIN_ZOOM){
                reset();
            }

            return true;
        }
    }

}