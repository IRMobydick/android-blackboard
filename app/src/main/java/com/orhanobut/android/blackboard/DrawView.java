
package com.orhanobut.android.blackboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DrawView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final String TAG = DrawView.class.getSimpleName();
    private static final float TOUCH_TOLERANCE = 0;
    private static final long REPLAY_SPEED = 50;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final SurfaceHolder holder;
    private final DrawListener listener;
    private final Handler handler = new Handler();

    private MyPath path;
    private float x;
    private float y;
    private List<MyPath> pathList;
    private volatile boolean running;
    private Bitmap bitmap;
    private Canvas myCanvas;
    private ReplayTask thread;
    private boolean exitReplay;

    public interface DrawListener {

        public void onReplayCompleted();

        public void onPaused();

        public void onPlaying();
    }

    public DrawView(Context context, DrawListener listener) {
        super(context);

        setZOrderOnTop(true);
        if (path == null) {
            path = new MyPath();
        }
        if (pathList == null) {
            pathList = new ArrayList<MyPath>();
            Log.d(TAG, "pathList = new (constructor)");
        }

        getHolder().addCallback(this);
        holder = getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);

        setDrawingCacheEnabled(true);

        this.listener = listener;
    }

    public void setPaintColor(int paintColor) {
        path.setPaintColor(paintColor);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        List<MyPath> tempPathList = new ArrayList<MyPath>(pathList);

        for (MyPath myPath : tempPathList) {
            myPath.draw(canvas);
        }
        path.draw(canvas);
    }

    private void touchStart(float x, float y) {
        path.moveTo(x, y);
        this.x = x;
        this.y = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - this.x);
        float dy = Math.abs(y - this.y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(this.x, this.y, (x + this.x) / 2, (y + this.y) / 2);
            this.x = x;
            this.y = y;
        }
    }

    private void touchUp() {
        path.lineTo(x, y);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        if (isReplaying()) {
            return true;
        }

        final float x = event.getX();
        final float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                path.addPoint(new Point(x, y));
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                path.addPoint(new Point(x, y));
                break;
            case MotionEvent.ACTION_UP:
                touchMove(x + 1, y + 1);

                path.addPoint(new Point(x + 1, y + 1));
                touchUp();
                pathList.add(path);
                Log.d(TAG, "pathList.add ACTION_UP");
                path = new MyPath(path.getPaint());
                break;
        }
        return true;
    }

    public void replay() {
        if (thread == null || thread.getState() == Thread.State.TERMINATED) {
            thread = new ReplayTask();
            thread.state = Thread.State.RUNNABLE;
            thread.start();
        } else if (thread.state == Thread.State.WAITING) {
            thread.state = Thread.State.RUNNABLE;
            synchronized (thread.monitor) {
                thread.monitor.notify();
            }
            listener.onPlaying();
        } else if (thread.state == Thread.State.RUNNABLE) {
            thread.state = Thread.State.WAITING;
            listener.onPaused();
        }
    }

    public boolean isReplaying() {
        return (thread != null);
    }

    public void reset() {
        Log.d(TAG, "pathList clear (reset)");
        pathList.clear();
        path = new MyPath(path.getPaint());
        thread = null;

        if (bitmap != null) {
            bitmap.recycle();
        }
    }

    public void setEraser() {
        path.setEraserMode();
    }

    public void setPen() {
        path.setPenMode();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        onResume();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (bitmap != null) {
            bitmap.recycle();
        }

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        myCanvas = new Canvas(bitmap);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    private void setRunning(boolean isRunning) {
        running = isRunning;
    }

    public void onPause() {
        Log.d(TAG, "onPause");
        setRunning(false);
    }

    //TODO improvement needed for destroying
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        setRunning(false);
        executorService.shutdown();
        //   path = null;
        //   pathList = null;
        Log.d(TAG, "pathList == null (onDestroy)");

        //   thread = null;
        if (bitmap != null) {
            //      bitmap.recycle();
            //     bitmap = null;
        }
    }

    public void onResume() {
        setRunning(true);
        executorService.execute(this);
    }

    public void exitReplay() {
        if (thread.state == Thread.State.WAITING) {
            thread.state = Thread.State.RUNNABLE;
            synchronized (thread.monitor) {
                thread.monitor.notify();
            }
            listener.onPlaying();
        }
        exitReplay = true;
    }

    @Override
    public void run() {
        Log.d(TAG, "run");
        while (running) {
            if (!holder.getSurface().isValid()) {
                break;
            }

            Canvas canvas = holder.lockCanvas(null);

            synchronized (holder) {
                if (myCanvas != null) {
                    draw(myCanvas);
                }
                draw(canvas);
            }
            holder.unlockCanvasAndPost(canvas);
        }
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    static class MyPath extends Path {

        private static final float ERASER_STROKE_WIDTH = 50f;
        private static final float PEN_STROKE_WIDTH = 14;

        private final Paint paint;
        private final List<Point> pointList;
        private final int defaultColor = Color.parseColor("#FFFF99");

        public MyPath() {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setColor(defaultColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(PEN_STROKE_WIDTH);
            pointList = new ArrayList<Point>();
        }

        public MyPath(Paint paint) {
            this.paint = new Paint(paint);
            this.pointList = new ArrayList<Point>();
        }

        public void draw(Canvas canvas) {
            canvas.drawPath(this, paint);
        }

        public List<Point> getPointList() {
            return pointList;
        }

        public void addPoint(Point point) {
            pointList.add(point);
        }

        public void setPaintColor(int color) {
            this.paint.setColor(color);
        }

        public void setEraserMode() {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            paint.setStrokeWidth(ERASER_STROKE_WIDTH);
        }

        public void setPenMode() {
            paint.setStrokeWidth(PEN_STROKE_WIDTH);
            paint.setXfermode(null);
        }

        public Paint getPaint() {
            return paint;
        }

    }

    static class Point {

        private final float x;
        private final float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }

    class ReplayTask extends Thread {

        private final String TAG = ReplayTask.class.getSimpleName();
        private final Object monitor = new Object();

        private State state;

        @Override
        public void run() {
            List<MyPath> tempList = Collections.emptyList();
            if (!pathList.isEmpty()) {
                tempList = new ArrayList<MyPath>(pathList);
                pathList = new ArrayList<MyPath>();
                Log.d(TAG, "pathList = new ArrayList() (ReplayTask run)");
            }

            for (MyPath myPath : tempList) {
                path = new MyPath(myPath.getPaint());
                final List<Point> list = myPath.getPointList();
                touchStart(list.get(0).getX(), list.get(0).getY());
                for (Point point : list) {

                    synchronized (monitor) {
                        while (state != State.RUNNABLE) {
                            try {
                                monitor.wait();
                            } catch (InterruptedException e) {
                                Log.d(TAG, e.getMessage());
                            }
                        }
                    }

                    touchMove(point.getX(), point.getY());

                    try {
                        if (!exitReplay) {
                            Thread.sleep(REPLAY_SPEED);
                        }
                    } catch (InterruptedException e) {
                        Log.d(TAG, e.getMessage());
                    }

                }
                pathList.add(myPath);
                Log.d(TAG, "pathList.add (ReplayTask run)");
            }
            path = new MyPath(path.getPaint());

            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onReplayCompleted();
                    exitReplay = false;
                    thread = null;
                }
            });
        }
    }
}
