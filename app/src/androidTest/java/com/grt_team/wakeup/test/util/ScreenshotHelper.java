
package com.grt_team.wakeup.test.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.grt_team.wakeup.entity.puzzle.PuzzleView;
import com.jayway.android.robotium.solo.Solo;

public class ScreenshotHelper {
    private static final String TAG = "ScreenshotHelper";
    private static int TIMEOUT_LIMIT = 10000;

    // set your location
    private static final String SCREEN_SHOTS_LOCATION = Environment.getExternalStorageDirectory()
            + "/Robotium-Screenshots/";

    private ScreenshotHelper() {
    }

    public static void takeScreen(Instrumentation inst, Solo solo, final String name)
            throws TimeoutException {
        SoloFetcher fetcher = new SoloFetcher(solo, inst);
        takeScreen(inst, solo, name, fetcher.getCurrentRootViewSafe());
    }

    public static void takeScreen(final Instrumentation inst, final Solo solo, final String name,
            final View view) {

        final AtomicBoolean needWait = new AtomicBoolean(true);

        int currentTime = 0;
        while (currentTime < TIMEOUT_LIMIT) {
            if (view.getWidth() != 0) {
                break;
            }
            currentTime += 10;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }

        inst.runOnMainSync(new Runnable() {

            @Override
            public void run() {
                Bitmap bmp = null;
                if (view instanceof PuzzleView) {
                    bmp = createPuzzleViewBitmap(view.getRootView(), (PuzzleView) view);
                } else {
                    bmp = createViewBitmap(view);
                }
                ScreenshotHelper.saveBitmap(bmp, name, new FinishCallback() {

                    @Override
                    public void onFinish() {
                        needWait.set(false);
                    }
                });
            }
        });

        currentTime = 0;
        while (needWait.get()) {
            if (currentTime > TIMEOUT_LIMIT) {
                break;
            }
            currentTime += 10;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }

    public static Bitmap createViewBitmap(View view) {
        Bitmap bmp = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Config.RGB_565);
        Canvas canvas = new Canvas(bmp);
        view.draw(canvas);
        return bmp;
    }

    public static Bitmap createPuzzleViewBitmap(View view, PuzzleView puzzleView) {
        Bitmap bmp = createViewBitmap(view);
        int skipHeight = view.getHeight() - puzzleView.getHeight();

        Canvas canvas = new Canvas(bmp);
        canvas.translate(0, skipHeight);
        canvas.clipRect(0, 0, canvas.getWidth(), canvas.getHeight());
        puzzleView.doDraw(canvas);
        return bmp;
    }

    public interface FinishCallback {
        public void onFinish();
    }

    private static void saveBitmap(final Bitmap bmp, final String name,
            final FinishCallback finishCallback) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                FileOutputStream fos = null;
                try {
                    File sddir = new File(SCREEN_SHOTS_LOCATION);
                    if (!sddir.exists()) {
                        sddir.mkdirs();
                    }
                    fos = new FileOutputStream(SCREEN_SHOTS_LOCATION + name + ".jpg");
                    if (fos != null) {
                        bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                        fos.close();
                    }
                    bmp.recycle();
                    Log.v(TAG, "Screen shot saved.");
                } catch (Exception e) {
                    Log.e(TAG, "Error during saving screenshot file.", e);
                }
                if (finishCallback != null) {
                    finishCallback.onFinish();
                }
            }
        }).start();
    }
}
