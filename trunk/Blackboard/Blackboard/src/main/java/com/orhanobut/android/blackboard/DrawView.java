
package com.orhanobut.android.blackboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class DrawView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private MyPath mPath;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 0;
    private static final long REPLAY_SPEED = 50;
    private Thread mThread;
    private List<MyPath> mPathList;
    private volatile boolean mRunning;
    private final SurfaceHolder mHolder;
    private final Context mContext;

    public DrawView(Context c) {
        super(c);

        mContext = c;
        setZOrderOnTop(true);
        mPath = new MyPath();
        mPathList = new ArrayList<MyPath>();
        getHolder().addCallback(this);
        mHolder = getHolder();
        mHolder.setFormat(PixelFormat.TRANSPARENT);

    }

    public void setPaintColor(int paintColor) {
        mPath.setPaintColor(paintColor);
    }

    public void draw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        for (int i = 0; i < mPathList.size(); i++) {
            mPathList.get(i).draw(canvas);
        }
        mPath.draw(canvas);
    }

    private void touchStart(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                mPath.addPoint(new Point(x, y));
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                mPath.addPoint(new Point(x, y));
                break;
            case MotionEvent.ACTION_UP:
                touchMove(x + 1, y + 1);

                mPath.addPoint(new Point(x + 1, y + 1));
                touchUp();
                mPathList.add(mPath);
                mPath = new MyPath(mPath.getPaint());
                break;
        }
        return true;
    }

    public void replay() {
        List<MyPath> tempList = mPathList;
        mPathList = new ArrayList<MyPath>(tempList.size());

        for (MyPath mp : tempList) {
            mPath = new MyPath(mp.getPaint());
            List<Point> list = mp.getPointList();
            touchStart(list.get(0).getX(), list.get(0).getY());
            for (int i = 1; i < list.size() - 1; i++) {
                Point p = list.get(i);
                touchMove(p.getX(), p.getY());

                try {
                    Thread.sleep(REPLAY_SPEED);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mPathList.add(mp);
        }
        mPath = new MyPath(mPath.getPaint());
    }

    public void reset() {
        mPathList.clear();
        mPath = new MyPath(mPath.getPaint());
    }

    public void setEraser() {
        mPath.setEraserMode();
    }

    public void setPen() {
        mPath.setPenMode();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        onResume();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    private void setRunning(boolean isRunning) {
        mRunning = isRunning;
    }

    public void onPause() {
        setRunning(false);
        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mThread = null;
    }

    public void onResume() {
        setRunning(true);
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void run() {
        while (mRunning) {
            if (mHolder.getSurface().isValid()) {
                Canvas c = mHolder.lockCanvas(null);

                synchronized (mHolder) {
                    draw(c);
                }

                mHolder.unlockCanvasAndPost(c);
            }
        }
    }

    static class MyPath extends Path {
        private final Paint mPaint;
        private final List<Point> mPointList;
        private final float mStrokeWidth = 14;
        private final int mDefaultColor = Color.parseColor("#FFFF99");

        public MyPath(){
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(mDefaultColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(12);
            mPointList = new ArrayList<Point>();
        }

        public MyPath(Paint paint) {
            mPaint = new Paint(paint);
            mPointList = new ArrayList<Point>();
        }

        public void draw(Canvas canvas) {
            canvas.drawPath(this, mPaint);
        }

        public List<Point> getPointList() {
            return mPointList;
        }

        public void addPoint(Point point) {
            mPointList.add(point);
        }

        public void setPaintColor(int color) {
            this.mPaint.setColor(color);
        }

        public void setEraserMode() {
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mPaint.setStrokeWidth(50f);
        }

        public void setPenMode(){
            mPaint.setStrokeWidth(mStrokeWidth);
            mPaint.setXfermode(null);
        }

        public Paint getPaint() {
            return mPaint;
        }

    }

    static class Point {
        private final float mX;
        private final float mY;

        public Point(float x, float y) {
            this.mX = x;
            this.mY = y;
        }

        public float getX() {
            return mX;
        }

        public float getY() {
            return mY;
        }
    }

}
