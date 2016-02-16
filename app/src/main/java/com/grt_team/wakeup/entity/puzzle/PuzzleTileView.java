
package com.grt_team.wakeup.entity.puzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

public abstract class PuzzleTileView extends PuzzleView {

    private float mXOffset = 0;
    private float mYOffset = 0;

    private int mTileWidth;
    private int mTileHeight;

    private int[][] tiles;
    private Bitmap[] mBitmaps;
    private Rect rect;

    private int rowCount;
    private int colCount;

    private final Paint mPaint = new Paint();
    private OnTileTouchListener onTileTouchListener;

    public interface OnTileTouchListener {
        /**
         * @param row - tile row
         * @param col - tile column
         * @param key - bitmap key index
         * @param action - MotionEvent.getAction() may be only
         *            MotionEvent.ACTION_DOWN or MotionEvent.ACTION_MOVE
         */
        public void onTileTouch(int row, int col, int key, int action);
    }

    public PuzzleTileView(Context context) {
        super(context);
    }

    public void setOnTileTouchListener(OnTileTouchListener onTileTouchListener) {
        this.onTileTouchListener = onTileTouchListener;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColCount() {
        return colCount;
    }

    public int[][] getTiles() {
        return tiles;
    }

    public int getTileWidth() {
        return mTileWidth;
    }

    public int getTileHeight() {
        return mTileHeight;
    }

    public float getXOffset() {
        return mXOffset;
    }

    public void setXOffset(float offset) {
        mXOffset = offset;
    }

    public void setYOffset(float offset) {
        mYOffset = offset;
    }

    public float getYOffset() {
        return mYOffset;
    }

    public void setTiles(int[][] tiles) {
        rowCount = tiles.length;
        colCount = tiles[0].length;
        this.tiles = tiles;
    }

    public void resetTiles(int rowCount, int columCount) {
        this.rowCount = rowCount;
        this.colCount = columCount;
        tiles = new int[rowCount][columCount];
    }

    public void setTile(int row, int col, int bitmapIndex) {
        tiles[row][col] = bitmapIndex;
    }

    public int getTile(int row, int col) {
        return tiles[row][col];
    }

    public void initTileSize(Rect rect) {
        this.rect = rect;
        mTileWidth = rect.width() / tiles[0].length;
        mTileHeight = rect.height() / tiles.length;

        mXOffset = (rect.width() - mTileWidth * tiles[0].length) / 2.0f;
        mYOffset = (rect.height() - mTileHeight * tiles.length) / 2.0f;
    }

    public void resetBitmaps(int totalBitmaps) {
        mBitmaps = new Bitmap[totalBitmaps];
    }

    public void loadBitmap(int key, Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(mTileWidth, mTileHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, mTileWidth, mTileHeight);
        drawable.draw(canvas);

        mBitmaps[key] = bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmaps == null){
            return;
        }
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                canvas.drawBitmap(mBitmaps[tiles[i][j]], rect.left + mXOffset + mTileWidth * j,
                        rect.top + mYOffset + mTileHeight * i, mPaint);
            }
        }
    }

    boolean executed = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);

        if (onTileTouchListener != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    int col = (int) ((event.getX() - mXOffset - rect.left) / mTileWidth);
                    int row = (int) ((event.getY() - mYOffset - rect.top) / mTileHeight);

                    if ((row > -1) && (row < tiles.length) && (col > -1)
                            && (col < tiles[row].length)) {
                        onTileTouchListener.onTileTouch(row, col, tiles[row][col],
                                event.getAction());
                    }
                    result = true; // keep listen for move action
            }
        }

        return result;
    }

}
