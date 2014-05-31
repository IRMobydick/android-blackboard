
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
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class DrawView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private MyPath path;
    private float x, y;
    private static final float TOUCH_TOLERANCE = 0;
    private static final long REPLAY_SPEED = 50;
    private Thread drawThread;
    private List<MyPath> pathList;
    private volatile boolean running;
    private final SurfaceHolder holder;
    private Bitmap bitmap;
    private Canvas myCanvas;
    private ReplayTask thread;
    private DrawListener listener;

    public interface DrawListener{
        public void onReplayCompleted();
        public void onPaused();
    }

    public DrawView(Context c, DrawListener listener) {
        super(c);

        setZOrderOnTop(true);
        path = new MyPath();
        pathList = new ArrayList<MyPath>();
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
    public void draw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        for (int i = 0; i < pathList.size(); i++) {
            pathList.get(i).draw(canvas);
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
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

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
                path = new MyPath(path.getPaint());
                break;
        }
        return true;
    }

    class ReplayTask extends Thread {

        private final Object monitor = new Object();
        private State state;

        @Override
        public void run() {
            List<MyPath> tempList = pathList;
            pathList = new ArrayList<MyPath>(tempList.size());

            for (MyPath mp : tempList) {
                path = new MyPath(mp.getPaint());
                List<Point> list = mp.getPointList();
                touchStart(list.get(0).getX(), list.get(0).getY());
                for (int i = 1; i < list.size() - 1; i++) {

                    synchronized (monitor) {
                        while (state != State.RUNNABLE) {
                            try {
                                monitor.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    Point p = list.get(i);
                    touchMove(p.getX(), p.getY());

                    try {
                        Thread.sleep(REPLAY_SPEED);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                pathList.add(mp);
            }
            path = new MyPath(path.getPaint());

            Message m = new Message();
            handler.sendMessage(m);
        }
    }

    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            listener.onReplayCompleted();
        }
    };

    public void replay() throws InterruptedException {
        if (thread == null || thread.getState() == Thread.State.TERMINATED) {
            thread = new ReplayTask();
            thread.state = Thread.State.RUNNABLE;
            thread.start();
        } else if (thread.state == Thread.State.WAITING) {
            thread.state = Thread.State.RUNNABLE;
            synchronized (thread.monitor) {
                thread.monitor.notify();
            }
        } else if (thread.state == Thread.State.RUNNABLE) {
            thread.state = Thread.State.WAITING;
            listener.onPaused();
        }
    }

    public boolean isReplaying(){
        if (thread != null && thread.getState() == Thread.State.RUNNABLE){
            return true;
        }
        return false;
    }

    public boolean isPaused(){
        if (thread.state == Thread.State.WAITING){
            return true;
        }
        return false;
    }

    public void reset() {
        pathList.clear();
        path = new MyPath(path.getPaint());
        thread = null;
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
        setRunning(false);
        try {
            drawThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        drawThread = null;
        thread = null;
    }

    public void onResume() {
        setRunning(true);
        drawThread = new Thread(this);
        drawThread.start();
    }

    @Override
    public void run() {
        while (running) {
            if (holder.getSurface().isValid()) {
                Canvas c = holder.lockCanvas(null);

                synchronized (holder) {
                    if (myCanvas != null) {
                        draw(myCanvas);
                    }
                    draw(c);
                }

                holder.unlockCanvasAndPost(c);
            }
        }
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    static class MyPath extends Path {
        private final Paint paint;
        private final List<Point> pointList;
        private final float strokeWidth = 14;
        private final int defaultColor = Color.parseColor("#FFFF99");

        public MyPath() {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setColor(defaultColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(12);
            pointList = new ArrayList<Point>();
        }

        public MyPath(Paint paint) {
            this.paint = new Paint(paint);
            pointList = new ArrayList<Point>();
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
            paint.setStrokeWidth(50f);
        }

        public void setPenMode() {
            paint.setStrokeWidth(strokeWidth);
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

}
