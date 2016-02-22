package com.grt_team.wakeup.entity.puzzle.catcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.entity.puzzle.PuzzleView;
import com.grt_team.wakeup.utils.DisplayHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by oleh on 2/16/16.
 */
public class CatcherView extends PuzzleView {


    private final int textHeight;
    private long APPLE_GENERATION_INTERVAL = 1000 * 2;  // 2 seconds
    private long BANANA_GENERATION_INTERVAL = 1000 * 6;  // 6 seconds
    private int FAIL_ANIMATION_DURATION = 15;        // number of frames
    final int deltaHide = 30; // %
    final double scaleBucket = 0.06;
    final double scaleApple = 0.05;
    final double speedChange = 0.9;

    private Random rand;

    private Bitmap bucket;
    private Bitmap background;
    private Bitmap apple;
    private Bitmap banana;

    private boolean drugging;

    private int screenWidth;
    private int screenHeight;
    private int bucketX;
    private int bucketY;
    private int bucketOffsetX;
    private int bucketDelta;

    private ConcurrentLinkedQueue<Point> apples = new ConcurrentLinkedQueue<Point>();
    private Point bananaPos;
    private int toCatch;
    private int speed;
    private int bucketSize;
    private int failOverlay = -1;
    private Integer caught;
    private Paint textPaint;

    private long lastAppleGeneratedTime;
    private long lastBananaGeneratedTime;
    private long interval = APPLE_GENERATION_INTERVAL;
    private OnAllApplesCaughtListener listener;
    private boolean running = true;
    private Paint overlay;
    private final int overlayTransparancyStep = 80 / FAIL_ANIMATION_DURATION;

    public CatcherView(Context context) {
        super(context);
        rand = new Random(System.currentTimeMillis());

        screenHeight = DisplayHelper.getScreenHeight(context);
        screenWidth = DisplayHelper.getScreenWidth(context);

        textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textHeight = context.getResources().getDimensionPixelSize(R.dimen.text_size_large);
        textPaint.setTextSize(textHeight);

        lastBananaGeneratedTime = System.currentTimeMillis();
    }

    public void initBitmaps() {
        Bitmap tmpBucket = BitmapFactory.decodeResource(getResources(), R.drawable.catcher_bucket_ic);
        int bucketWidth = (int) (screenWidth * bucketSize * scaleBucket);
        double scalingFactor = bucketWidth / (double)tmpBucket.getWidth();
        int bucketHeight = (int) (tmpBucket.getHeight() * scalingFactor);
        bucket = Bitmap.createScaledBitmap(tmpBucket,
                bucketWidth,
                bucketHeight,
                false);
        bucketX = (screenWidth - bucket.getWidth()) / 2;
        bucketY = screenHeight - bucket.getHeight();
        bucketDelta = bucket.getWidth() * deltaHide / 100;

        Bitmap tmpBg = BitmapFactory.decodeResource(getResources(), R.drawable.catcher_baackground);
        background = Bitmap.createScaledBitmap(tmpBg,
                screenWidth,
                screenHeight,
                false);

        Bitmap tmpApple = BitmapFactory.decodeResource(getResources(), R.drawable.catcher_apple_ic);
        int appleWidth = (int) (screenWidth * scaleApple);
        scalingFactor = appleWidth / (double)tmpApple.getWidth();
        int appleHeight = (int) (tmpApple.getHeight() * scalingFactor);
        apple = Bitmap.createScaledBitmap(tmpApple,
                appleWidth,
                appleHeight,
                false);

        Bitmap tmpBanana = BitmapFactory.decodeResource(getResources(), R.drawable.catcher_banana_ic);
        int bananaWidth = appleWidth;
        scalingFactor = bananaWidth / (double) tmpBanana.getWidth();
        int bananaHeight = (int) (tmpBanana.getHeight() * scalingFactor);
        banana = Bitmap.createScaledBitmap(tmpBanana,
                bananaWidth,
                bananaHeight,
                false);

        overlay = new Paint();
        overlay.setColor(Color.RED);
    }

    @Override
    protected synchronized boolean updatePuzzleState(long timeElapsed) {
        if (!running) {
            return false;
        }

        if (bananaPos == null &&
                System.currentTimeMillis() - lastBananaGeneratedTime >= BANANA_GENERATION_INTERVAL) {

            bananaPos = generatePosition();
            lastBananaGeneratedTime = System.currentTimeMillis();
            lastAppleGeneratedTime = System.currentTimeMillis();    // to not create apple now
        }
        if (bananaPos != null) {
            bananaPos.y += speed;

            // Banana caught
            if (bananaPos.y >= bucketY + bucket.getHeight() / 2
                    && bananaPos.x > bucketX && bananaPos.x + apple.getWidth() < bucketX + bucket.getWidth()) {
                failOverlay = FAIL_ANIMATION_DURATION;
                caught = 0;
                bananaPos = null;
            } else if (bananaPos.y + banana.getHeight() >= screenHeight) {
                // banana reached bottom
                bananaPos = null;
            }
        }

        if (System.currentTimeMillis() - lastAppleGeneratedTime >= interval) {
            apples.add(generatePosition());
            lastAppleGeneratedTime = System.currentTimeMillis();

            // decrease interval to
            if (interval > 700) {
                interval *= speedChange;
            }
        }

        for (Iterator<Point> i = apples.iterator(); i.hasNext(); ) {
            Point p = i.next();
            p.y += speed;

            // if apple missed start again
            if (p.y + apple.getHeight() >= screenHeight) {
                failOverlay = FAIL_ANIMATION_DURATION;
                interval = APPLE_GENERATION_INTERVAL;
                caught = 0;
                i.remove();
                break;
            }

            // apple caught
            if (p.y >= bucketY + bucket.getHeight() / 2
                    && p.x > bucketX && p.x + apple.getWidth() < bucketX + bucket.getWidth()) {
                caught++;
                i.remove();

                checkApplesCaught();
            }
        }

        return false;
    }

    private void checkApplesCaught() {
        post(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    if (caught == toCatch) {
                        apples.clear();
                        running = false;
                        listener.onAllApplesCaught();
                    }
                }
            }
        });
    }

    private Point generatePosition() {
        int x = rand.nextInt(screenWidth - apple.getWidth());
        int y = -apple.getHeight();
        return new Point(x, y);
    }

    @Override
    public void doDraw(Canvas canvas) {

        // Draw background
        canvas.drawBitmap(background, 0, 0, null);

        // Draw score
        if (caught == null) {
            caught = 0;
        }
        canvas.drawText(Integer.toString(toCatch - caught), 15, textHeight, textPaint);

        // Draw applesq
        for (Point p : apples) {
            canvas.drawBitmap(apple, p.x, p.y, null);
        }

        // Draw banana
        if (bananaPos != null) {
            canvas.drawBitmap(banana, bananaPos.x, bananaPos.y, null);
        }

        // Draw bucket
        canvas.drawBitmap(bucket, bucketX, bucketY, null);

        // Draw failure overlay
        if (failOverlay >= 0) {
            overlay.setAlpha(overlayTransparancyStep * failOverlay);
            canvas.drawRect(0, 0, screenWidth, screenHeight, overlay);
            failOverlay--;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                drugging = false;
                return false;
            case MotionEvent.ACTION_DOWN:
                if (overBucket(event.getX(), event.getY())) {
                    drugging = true;
                    bucketOffsetX = (int) (event.getX() - bucketX);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (drugging) {
                    moveBucket((int) event.getX());
                }
                return true;
            default:
                drugging = false;
                return result;
        }
    }

    private void moveBucket(int x) {
        if (x + bucket.getWidth() - bucketOffsetX - bucketDelta <= screenWidth
                && x - bucketOffsetX + bucketDelta >= 0) {
            bucketX = x - bucketOffsetX;
        }
    }


    private boolean overBucket(float x, float y) {
        return x > bucketX - bucketDelta && x < bucketX + bucket.getWidth() + bucketDelta
                && y > bucketY - bucketDelta && y < bucketY + bucket.getHeight() + bucketDelta;
    }

    public void setToCatch(int toCatch) {
        this.toCatch = toCatch;
    }

    public void setCaught(Integer caught) {
        this.caught = caught;
    }

    public Integer getCaught() {
        return caught;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setListener(OnAllApplesCaughtListener listener) {
        this.listener = listener;
    }

    public void setBucketSize(int bucketSize) {
        this.bucketSize = bucketSize;
    }

    public interface OnAllApplesCaughtListener {
        void onAllApplesCaught();
    }
}
