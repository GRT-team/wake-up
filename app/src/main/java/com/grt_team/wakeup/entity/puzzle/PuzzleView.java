
package com.grt_team.wakeup.entity.puzzle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public abstract class PuzzleView extends SurfaceView implements Callback {
    private static final String TAG = "PuzzleView";
    private static final boolean FPS_SHOW = false;
    private static final int FPS_TEXT_SIZE = 50;

    private PuzzleThread thread;
    private long lastFpsTime = 0;
    private int fps = 0, ifps = 0;
    private Paint fpsPaint;

    private OnUserActionPerformedListener actionPerformedListener;

    public interface OnUserActionPerformedListener {
        public void onActionPerformed();
    }

    public PuzzleView(Context context) {
        super(context);
        getHolder().addCallback(this);
        if (FPS_SHOW) {
            fpsPaint = new Paint();
            fpsPaint.setTypeface(Typeface.DEFAULT_BOLD);
            fpsPaint.setTextSize(FPS_TEXT_SIZE);
            fpsPaint.setColor(Color.YELLOW);
            fpsPaint.setStyle(Paint.Style.FILL);
        }
    }

    public void setOnUserActionPerformedListener(OnUserActionPerformedListener listener) {
        this.actionPerformedListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != actionPerformedListener) {
            actionPerformedListener.onActionPerformed();
        }
        return super.onTouchEvent(event);
    }

    protected abstract boolean updatePuzzleState(long time);

    public abstract void doDraw(Canvas canvas);

    private class PuzzleThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private boolean isRunning;

        public PuzzleThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        @Override
        public void run() {
            long timeElapsed = 0;
            long start;
            long end;
            while (isRunning) {
                long startFpsTime = System.currentTimeMillis();
                Canvas c = null;
                try {
                    start = System.currentTimeMillis();
                    c = surfaceHolder.lockCanvas(null);
                    if (c == null) {
                        break;
                    }

                        doDraw(c);
                    if (FPS_SHOW) {
                        String msg = String.valueOf(fps);
                        c.drawText(msg, fpsPaint.measureText(msg), FPS_TEXT_SIZE, fpsPaint);
                    }

                    end = System.currentTimeMillis();
                    timeElapsed += end - start;
                    if (updatePuzzleState(timeElapsed)) {
                        timeElapsed = 0;
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        surfaceHolder.unlockCanvasAndPost(c);
                    }
                }

                ifps++;
                if (startFpsTime > (lastFpsTime + 1000)) {
                    lastFpsTime = startFpsTime;
                    fps = ifps;
                    ifps = 0;
                }
            }
        }

        public void setRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }

    }

    public void setRunningMode(boolean isRunning) {
        if (thread != null && thread.isAlive()) {
            thread.setRunning(isRunning);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (thread == null || !thread.isAlive()) {
            thread = new PuzzleThread(getHolder());
            thread.setRunning(true);
            thread.start();
        } else {
            Log.w(TAG, "Trying to start other thread when it is already started!");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

}
