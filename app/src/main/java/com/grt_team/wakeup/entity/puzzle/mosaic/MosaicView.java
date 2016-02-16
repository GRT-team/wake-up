
package com.grt_team.wakeup.entity.puzzle.mosaic;

import java.util.Random;
import java.util.Stack;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.MotionEvent;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.entity.puzzle.PuzzleView;
import com.grt_team.wakeup.preferences.PickImagePreferences;
import com.grt_team.wakeup.utils.BitmapHelper;

public class MosaicView extends PuzzleView {

    final int deltaHide = 50; // %

    Paint backGroundPaint = new Paint();
    Paint dashPaint = new Paint();
    Paint puzzleItemBackground = new Paint();
    Bitmap bitmap;
    Bitmap bitmapBackground;
    Rect src, dst;
    Bitmap horizontalShadow, verticalShadow, edgeShadow;

    int eWidth, eHeight; // puzzles width and height
    int puzzleOffsetX, puzzleOffsetY; // offset for picked puzzle
    int imageOffsetX, imageOffsetY; // offset for image
    int deltaX, deltaY; // delta for picked puzzle
    int deltaHideX, deltaHideY; // delta for hide picked puzzle
    boolean drawBackground = true; // draw image on background
    boolean drawDash = true;
    int horizontalPuzzleNumbers; // number of puzzles on horizontal
    int verticalPuzzleNumbers; // number of puzzles on vertical
    int delta = 25; // %
    int shadowOffset; // offset for shadow

    Stack<PuzzleItem> freePuzzles = new Stack<PuzzleItem>();
    Stack<PuzzleItem> placedPuzzles = new Stack<PuzzleItem>();

    PuzzleItem selectedItem = null;

    OnMosaicCompletedListener listener;

    public interface OnMosaicCompletedListener {
        public void onMosaicCompleted();
    }

    public MosaicView(Context context) {
        super(context);
        shadowOffset = getResources().getDimensionPixelSize(R.dimen.puzzle_mosaic_shadow_offset);
    }

    public void setPuzzleSrc(String path, int width, int height, int horizontalNum, int verticalNum) {
        src = new Rect();
        dst = new Rect();
        backGroundPaint.setAlpha(50);
        dashPaint.setStrokeWidth(0);
        dashPaint.setColor(Color.BLACK);
        puzzleItemBackground.setColor(Color.BLACK);

        horizontalPuzzleNumbers = horizontalNum;
        verticalPuzzleNumbers = verticalNum;

        eWidth = width / horizontalPuzzleNumbers;
        if (width % horizontalPuzzleNumbers != 0)
            eWidth = (eWidth / 2) * 2;

        eHeight = height / verticalPuzzleNumbers;
        if (height % verticalPuzzleNumbers != 0)
            eHeight = (eHeight / 2) * 2;

        Bitmap tmp = resolveBitmap(getContext(), path);

        if (tmp.getWidth() < tmp.getHeight()) {
            // Rotate bitmap so it will be suitable for portrait mode.
            bitmap = tmp;
            Matrix matrix = new Matrix();
            matrix.postRotate(270);
            tmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                    true);
            bitmap.recycle();
        }

        bitmap = Bitmap.createScaledBitmap(tmp, eWidth * horizontalPuzzleNumbers, eHeight
                * verticalPuzzleNumbers, true);
        tmp.recycle();

        // Prepare background image for drawing with alpha.
        bitmapBackground = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Config.RGB_565);
        Canvas c = new Canvas(bitmapBackground);
        c.drawColor(Color.GRAY);
        c.drawBitmap(bitmap, 0, 0, backGroundPaint);

        imageOffsetX = (width - bitmap.getWidth()) / 2;
        imageOffsetY = (height - bitmap.getHeight()) / 2;

        deltaX = eWidth * delta / 100;
        deltaY = eHeight * delta / 100;

        deltaHideX = eWidth * deltaHide / 100;
        deltaHideY = eHeight * deltaHide / 100;

        generateShadow(eWidth, eHeight, shadowOffset);
    }

    public static Bitmap resolveBitmap(Context context, Uri uri) {
        Bitmap tmp = null;
        ContentResolver resolver = context.getContentResolver();
        Resources res = context.getResources();

        if (!PickImagePreferences.DEF_FILE.equals(uri.toString())) {
            int maxWidth = res.getDisplayMetrics().widthPixels;
            int maxHeight = res.getDisplayMetrics().heightPixels;

            Options op = new Options();
            op.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(uri.getPath(), op);
            op.inJustDecodeBounds = false;
            if (op.outWidth < op.outHeight) {
                tmp = BitmapHelper.loadScaledBitmap(resolver, uri, maxHeight, maxWidth, op);
            } else {
                tmp = BitmapHelper.loadScaledBitmap(resolver, uri, maxWidth, maxHeight, op);
            }
        }

        if (tmp == null) {
            Options op = new Options();
            op.inPreferredConfig = Config.RGB_565;
            tmp = BitmapFactory.decodeResource(res, R.drawable.puzzle_mosaic_default, op);
        }

        return tmp;
    }

    public static Bitmap resolveBitmap(Context context, String path) {
        Bitmap tmp = null;
        Resources res = context.getResources();

        if (!PickImagePreferences.DEF_FILE.equals(path)) {
            int maxWidth = res.getDisplayMetrics().widthPixels;
            int maxHeight = res.getDisplayMetrics().heightPixels;

            Options op = new Options();
            op.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, op);
            op.inJustDecodeBounds = false;
            if (op.outWidth < op.outHeight) {
                tmp = BitmapHelper.loadScaledBitmap(path, maxHeight, maxWidth, op);
            } else {
                tmp = BitmapHelper.loadScaledBitmap(path, maxWidth, maxHeight, op);
            }
        }

        if (tmp == null) {
            Options op = new Options();
            op.inPreferredConfig = Config.RGB_565;
            tmp = BitmapFactory.decodeResource(res, R.drawable.puzzle_mosaic_default, op);
        }

        return tmp;
    }

    @Override
    public void doDraw(Canvas canvas) {
        PuzzleItem item;
        canvas.drawColor(Color.GRAY);
        if (drawBackground) {
            // draw bitmap on background with alpha
            canvas.drawBitmap(bitmapBackground, imageOffsetX, imageOffsetY, null);
        }

        if (drawDash) {
            // draw horizontal dash
            for (int i = imageOffsetX + eWidth; i < eWidth * horizontalPuzzleNumbers; i += eWidth) {
                canvas.drawLine(i, imageOffsetY, i, imageOffsetY + eHeight * verticalPuzzleNumbers,
                        dashPaint);
            }
            // draw vertical dash
            for (int i = imageOffsetY + eHeight; i < eHeight * verticalPuzzleNumbers; i += eHeight) {
                canvas.drawLine(imageOffsetX, i, imageOffsetX + eWidth * horizontalPuzzleNumbers,
                        i, dashPaint);
            }
        }
        synchronized (this) {
            // draw placed puzzles
            for (int i = 0; i < placedPuzzles.size(); i++) {
                item = placedPuzzles.get(i);
                canvas.drawRect(getDestinationRect(item), puzzleItemBackground);
                canvas.drawBitmap(bitmap, getSourceRect(item), getDestinationRect(item), null);
            }

            // draw free puzzles
            for (int i = 0; i < freePuzzles.size(); i++) {
                item = freePuzzles.get(i);
                Rect r = getDestinationRect(item);
                canvas.drawRect(getDestinationRect(item), puzzleItemBackground);
                canvas.drawBitmap(bitmap, getSourceRect(item), getDestinationRect(item), null);

                // draw horizontal shadow
                canvas.drawBitmap(horizontalShadow, r.left + shadowOffset, r.bottom, null);
                // draw vertical shadow
                canvas.drawBitmap(verticalShadow, r.right, r.top + shadowOffset, null);
                // draw edge shadow
                canvas.drawBitmap(edgeShadow, r.right, r.bottom, null);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        if (pointerIndex == 0) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    if (null != selectedItem) {
                        checkPuzzlePosition(selectedItem);
                    } else {
                        if (freePuzzles.size() > 0) {
                            checkPuzzlePosition(freePuzzles.get(freePuzzles.size() - 1));
                        }
                    }
                    selectedItem = null;
                    return false;
                case MotionEvent.ACTION_DOWN:
                    selectedItem = moveElementOnTop(event.getX(), event.getY());
                    if (null != selectedItem) {
                        puzzleOffsetX = (int) (event.getX() - selectedItem.getCurrentX());
                        puzzleOffsetY = (int) (event.getY() - selectedItem.getCurrentY());
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (null != selectedItem) {
                        movePuzzle((int) event.getX(), (int) event.getY());
                    }
                    return true;
                default:
                    selectedItem = null;
                    return super.onTouchEvent(event);
            }
        }
        return true;
    }

    public void movePuzzle(int x, int y) {
        if (eWidth * horizontalPuzzleNumbers >= x - puzzleOffsetX + deltaHideX
                && x - puzzleOffsetX + deltaHideX >= imageOffsetX)
            selectedItem.setCurrentX(x - puzzleOffsetX);
        if (eHeight * verticalPuzzleNumbers >= y - puzzleOffsetY + deltaHideY
                && y - puzzleOffsetY + deltaHideY >= imageOffsetY)
            selectedItem.setCurrentY(y - puzzleOffsetY);
    }

    public void generatePuzzles() {
        Random random = new Random();
        int maxWidth;
        int maxHeight;
        for (int i = 0; i < verticalPuzzleNumbers; i++) {
            for (int j = 0; j < horizontalPuzzleNumbers; j++) {
                maxWidth = random.nextInt((horizontalPuzzleNumbers == 1) ? deltaHideX : eWidth
                        * horizontalPuzzleNumbers - eWidth);
                maxHeight = random.nextInt((verticalPuzzleNumbers == 1) ? deltaHideY : eHeight
                        * verticalPuzzleNumbers - eHeight);
                freePuzzles.add(new PuzzleItem(j * eWidth, i * eHeight, maxWidth, maxHeight));
            }
        }
    }

    private void generateShadow(int width, int height, int shadowOffset) {
        Paint shadowPaint = new Paint();
        Canvas canvas = new Canvas();
        horizontalShadow = Bitmap.createBitmap(eWidth - shadowOffset, shadowOffset,
                Config.ARGB_8888);
        verticalShadow = Bitmap
                .createBitmap(shadowOffset, eHeight - shadowOffset, Config.ARGB_8888);
        edgeShadow = Bitmap.createBitmap(shadowOffset, shadowOffset, Config.ARGB_8888);
        Shader hShadow = new LinearGradient(0, 0, 0, shadowOffset, Color.BLACK, Color.TRANSPARENT,
                TileMode.CLAMP);
        Shader vShadow = new LinearGradient(0, 0, shadowOffset, 0, Color.BLACK, Color.TRANSPARENT,
                TileMode.CLAMP);
        Shader eShadow = new RadialGradient(0, 0, shadowOffset, Color.BLACK, Color.TRANSPARENT,
                TileMode.CLAMP);
        shadowPaint.setShader(hShadow);
        canvas.setBitmap(horizontalShadow);
        canvas.drawRect(0, 0, eWidth - shadowOffset, shadowOffset, shadowPaint);
        shadowPaint.setShader(vShadow);
        canvas.setBitmap(verticalShadow);
        canvas.drawRect(0, 0, shadowOffset, eHeight - shadowOffset, shadowPaint);
        shadowPaint.setShader(eShadow);
        canvas.setBitmap(edgeShadow);
        canvas.drawRect(0, 0, shadowOffset, shadowOffset, shadowPaint);
    }

    private void checkMosaicCompleted() {
        if (listener != null && freePuzzles.size() == 0)
            listener.onMosaicCompleted();
    }

    private PuzzleItem moveElementOnTop(float x, float y) {
        synchronized (this) {
            PuzzleItem item;
            for (int i = freePuzzles.size() - 1; i >= 0; i--) {
                item = freePuzzles.get(i);
                int x1 = item.getCurrentX();
                int y1 = item.getCurrentY();
                int x2 = x1 + eWidth;
                int y2 = y1 + eHeight;
                if ((x >= x1 && x <= x2) && (y >= y1 && y <= y2)) {
                    freePuzzles.remove(i);
                    freePuzzles.push(item);
                    return item;
                }
            }
            return null;
        }
    }

    private void checkPuzzlePosition(PuzzleItem item) {
        synchronized (this) {
            int x1 = item.getCurrentX();
            int y1 = item.getCurrentY();
            int x2 = item.getSourceX() + imageOffsetX;
            int y2 = item.getSourceY() + imageOffsetY;
            if (Math.abs(x1 - x2) <= deltaX && Math.abs(y1 - y2) <= deltaY) {
                freePuzzles.pop();
                item.setCurrentX(item.getSourceX() + imageOffsetX);
                item.setCurrentY(item.getSourceY() + imageOffsetY);
                placedPuzzles.push(item);
                checkMosaicCompleted();
            }
        }
    }

    private Rect getSourceRect(PuzzleItem item) {
        src.left = item.getSourceX();
        src.top = item.getSourceY();
        src.right = src.left + eWidth;
        src.bottom = src.top + eHeight;
        return src;
    }

    private Rect getDestinationRect(PuzzleItem item) {
        dst.left = item.getCurrentX();
        dst.top = item.getCurrentY();
        dst.right = dst.left + eWidth;
        dst.bottom = dst.top + eHeight;
        return dst;
    }

    public Stack<PuzzleItem> getFreePuzzles() {
        return freePuzzles;
    }

    public Stack<PuzzleItem> getPlacedPuzzles() {
        return placedPuzzles;
    }

    public void setFreePuzzles(Stack<PuzzleItem> items) {
        freePuzzles.clear();
        freePuzzles.addAll(items);
    }

    public void setPlacedPuzzles(Stack<PuzzleItem> items) {
        synchronized (this) {
            placedPuzzles.clear();
            placedPuzzles.addAll(items);
        }
    }

    public void setDrawBackground(boolean draw) {
        drawBackground = draw;
    }

    public void setDrawDash(boolean draw) {
        drawDash = draw;
    }

    /**
     * Should be called before setPuzzleSrc()
     */
    public void setDelta(int delta) {
        this.delta = delta;
    }

    public static class PuzzleItem implements Parcelable {

        private int sourceX;
        private int sourceY;
        private int currentX;
        private int currentY;

        public PuzzleItem(int x1, int y1, int x2, int y2) {
            sourceX = x1;
            sourceY = y1;
            currentX = x2;
            currentY = y2;
        }

        private PuzzleItem(Parcel in) {
            this.sourceX = in.readInt();
            this.sourceY = in.readInt();
            this.currentX = in.readInt();
            this.currentY = in.readInt();
        }

        public static final Parcelable.Creator<PuzzleItem> CREATOR = new Parcelable.Creator<PuzzleItem>() {
            public PuzzleItem createFromParcel(Parcel in) {
                return new PuzzleItem(in);
            }

            public PuzzleItem[] newArray(int size) {
                return new PuzzleItem[size];
            }
        };

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(sourceX);
            dest.writeInt(sourceY);
            dest.writeInt(currentX);
            dest.writeInt(currentY);
        }

        public int getSourceX() {
            return sourceX;
        }

        public int getSourceY() {
            return sourceY;
        }

        public int getCurrentX() {
            return currentX;
        }

        public int getCurrentY() {
            return currentY;
        }

        public void setCurrentX(int x) {
            this.currentX = x;
        }

        public void setCurrentY(int y) {
            this.currentY = y;
        }

        public int describeContents() {
            return 0;
        }
    }

    public void setOnMosaicCompletedListener(OnMosaicCompletedListener listener) {
        this.listener = listener;
    }

    @Override
    protected boolean updatePuzzleState(long time) {
        return false;
    }

}
