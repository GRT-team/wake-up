
package com.grt_team.wakeup.entity.puzzle.maze;

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.entity.puzzle.PuzzleTileView;

public class MazeView extends PuzzleTileView {

    /**
     * Whole scene rect.
     */
    private Rect sceneRect;

    /**
     * Maze rect with maze offsets
     */
    private Rect mazeRect;

    /**
     * Scene sky rect
     */
    private Rect skyRect;

    /**
     * One tile rect
     */
    private Rect tileRect;

    /**
     * The tile rect where is user control.
     */
    private RectF currentRect;

    /**
     * The control rect for user input. It probably will be bigger the
     * currentRect so it will be easy to take control by touching display.
     */
    private RectF controlRect;

    /**
     * Current position of circle in tile grid
     */
    private Point controlPosition;

    /**
     * The position in the tile grid that indicate end of maze.
     */
    private Point startPosition;

    /**
     * The position in the tile grid that indicate end of maze.
     */
    private Point finishPosition;

    /**
     * Sky proportion, 25% from all height goes to sky
     */
    private final float skyProportion = .25f;

    /**
     * Required height offset for generated maze.Different maze size may create
     * offsets due to number of pixels and tile size.
     */
    private float mazeOffsetHeight;

    /**
     * Sky bitmap with grass and mountains
     */
    private Bitmap skyBitmap;

    /**
     * Ground bitmap, everything except sky. Will hold generated maze and it's
     * progress.
     */
    private Bitmap groundBitmap;

    /**
     * Canvas to draw on groundBitmap. Is used to reduce performance.
     */
    private Canvas groundCanvas;

    /**
     * The cloud drawable
     */
    private BitmapDrawable cloud;

    /**
     * All clouds rect, indicate where clouds need to be drawn.
     */
    private Rect[] cloudRect;

    /**
     * Animation for tree. Is used only for load and manual frame animation.
     */
    private AnimationDrawable tree;

    /**
     * Animation for sun. Is used only for load and manual fram animbation.
     */
    private AnimationDrawable sun;

    /**
     * Rect where finish tree will be drawn.
     */
    private Rect treeRect;

    /**
     * Ground not visited path color
     */
    private int groundColor;

    /**
     * Water visited path texture
     */
    private Drawable water;

    /**
     * Sky white to blue gradient
     */
    private LinearGradient gradient;

    /**
     * Paint that is used to draw any graphic element in the scene
     */
    private Paint paint;

    /**
     * Animation index of all finish tree bitmap frames
     */
    private int treeIndex = 0;

    /**
     * Animation index of all sun bitmap frames
     */
    private int sunIndex = 0;

    /**
     * Listener for puzzle solve event
     */
    private OnMazeFinishListener onFinishListener;

    /**
     * If we in moving mode then each screen move should try to move control
     * circle in touched direction.
     */
    private boolean isMovingMode;

    /**
     * Indicate if puzzle is finished.
     */
    private boolean finished;

    private final int sunAnimationSpeed = 75; // milliseconds
    private final int cloudAnimationSpeed = 25; // milliseconds
    private final int treeAnimationSpeed = 50; // milliseconds

    public interface OnMazeFinishListener {
        public void onMazeFinish();
    }

    public MazeView(Context context) {
        super(context);
    }

    /**
     * Move control element to specified maze tile coordinate.
     * 
     * @param row
     * @param col
     * @return
     */
    private boolean moveControlRectTo(int row, int col) {
        int[][] maze = getTiles();
        if (row < 0 || col < 0 || row > maze.length - 1 || col > maze[0].length)
            return false;
        if (maze[row][col] != MazePuzzle.FREE && maze[row][col] != MazePuzzle.END
                && maze[row][col] != MazePuzzle.VISITED)
            return false;
        controlPosition.set(row, col);
        currentRect.offsetTo((int) mazeRect.left + col * getTileWidth() + getTileWidth() / 2
                - currentRect.width() / 2, mazeRect.top + row * getTileHeight() + getTileHeight()
                / 2 - currentRect.height() / 2);
        controlRect.set(currentRect);
        // Increase control rect in 25% on each side
        controlRect.inset(-currentRect.width() * .25f, -currentRect.width() * .25f);

        if (controlRect.width() < getResources().getDimensionPixelSize(R.dimen.size_48dp)) {
            int desireSize = getResources().getDimensionPixelSize(R.dimen.size_48dp);
            controlRect
                    .inset(controlRect.width() - desireSize, controlRect.height() - desireSize);
        }
        if (maze[row][col] == MazePuzzle.END) {
            finished = true;
            if (onFinishListener != null) {
                onFinishListener.onMazeFinish();
            }
        }
        if (maze[row][col] != MazePuzzle.VISITED) {
            drawMazeCell(groundCanvas, MazePuzzle.VISITED, row, col);
        }
        maze[row][col] = MazePuzzle.VISITED;
        return true;
    }

    @Override
    public void initTileSize(Rect rect) {
        paint = new Paint();
        sceneRect = new Rect(rect);
        isMovingMode = false;

        // Initialize sky rect
        skyRect = new Rect(sceneRect);
        skyRect.bottom = (int) (sceneRect.height() * skyProportion);

        // Initialize ground rect
        Rect groundRect = new Rect(sceneRect);
        groundRect.top = skyRect.height();

        // Initialize maze on ground part only
        super.initTileSize(groundRect);

        // Add vertical offset for bottom water decoration.
        mazeOffsetHeight = getYOffset();

        // Set y offset to zero to avoid gaps between maze and sky rect
        setYOffset(0);

        // Initialize tile rect
        tileRect = new Rect(0, 0, getTileWidth(), getTileHeight());

        // Initialize maze rect
        mazeRect = new Rect(groundRect);
        mazeRect.offsetTo(mazeRect.left + (int) getXOffset(), mazeRect.top + (int) getYOffset());

        // Initialize control rect and its offset
        currentRect = new RectF(0, 0, getTileWidth(), getTileHeight());
        controlRect = new RectF(currentRect);

        // Gradient from white to blue for sky
        gradient = new LinearGradient(0, 0, 0,
                skyRect.bottom,
                Color.parseColor("#97e1fd"), Color.WHITE, TileMode.REPEAT);

        // Creating sun drawable and moving it to 62% left of skyRect and 10%
        // from the top
        sun = (AnimationDrawable) getResources().getDrawable(R.drawable.maze_sun);
        sun.setBounds(0, 0, skyRect.height(), skyRect.height());
        sun.getBounds().offsetTo((int) (skyRect.width() * .62f),
                (int) (sun.getBounds().height() * .1));

        // Creating 10 cloud rects, 5 per row. Cloud size will be calculated as
        // 30% of skyRect height and 15% of skyRect width.
        cloud = (BitmapDrawable) getResources().getDrawable(R.drawable.maze_cloud);

        cloudRect = new Rect[10];
        Random r = new Random();
        for (int i = 0; i < cloudRect.length; i++) {
            cloudRect[i] = new Rect(0, 0, (int) (skyRect.width() * .15f),
                    (int) (skyRect.height() * .3f));

            // put clouds with odd index to bottom row, otherwise to upper row.
            // Plus add random 35% of cloud height to top coordinates. Left
            // coordinates are calculated as current cloud index + 75% of cloud
            // width.
            cloudRect[i]
                    .offsetTo(
                            (int) (i * cloudRect[i].width() * .75f),
                            i % 2 * cloudRect[i].height()
                                    + r.nextInt((int) (cloudRect[i].height() * .35f)));
        }

        // Initialize ground not visited path texture
        groundColor = Color.parseColor("#2d1f04");

        // Initialize ground visited path texture
        water = (BitmapDrawable) getResources().getDrawable(R.drawable.maze_water);
        ((BitmapDrawable) water).setTileModeXY(TileMode.MIRROR, TileMode.MIRROR);

        // Search for end tile to put control to that position.
        int[][] tiles = getTiles();
        for (int j = 0; j < tiles[0].length; j++) {
            if (tiles[0][j] == MazePuzzle.END) {
                finishPosition = new Point(0, j);
            }
            if (tiles[tiles.length - 1][j] == MazePuzzle.VISITED) {
                startPosition = new Point(tiles.length - 1, j);
                controlPosition = new Point(startPosition);
                moveControlRectTo(tiles.length - 1, j);
            }
        }

        // Initialize tree image and it position according to maze end
        tree = (AnimationDrawable) getResources().getDrawable(R.drawable.maze_tree);
        treeRect = new Rect(0, 0, tree.getIntrinsicWidth(), tree.getIntrinsicHeight());
        float scale = (float) skyRect.height() / treeRect.height();
        treeRect.bottom *= scale;
        treeRect.right *= scale;
        int imagePadding = (int) (.06 * tree.getIntrinsicWidth());
        treeRect.offsetTo(mazeRect.left + finishPosition.y * getTileWidth() + getTileWidth() / 2
                - treeRect.width() / 2 + imagePadding, 0);
        paint.setFilterBitmap(true);

        skyBitmap = createSkyBitmap(skyRect.width(), skyRect.height());
        groundBitmap = createGroundWithMazeBitmap(groundRect.width(), groundRect.height());
        groundCanvas = new Canvas(groundBitmap);
    }

    private Bitmap createSkyBitmap(int width, int height) {
        Bitmap result;
        result = Bitmap.createBitmap(skyRect.width(), skyRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        // Mountains height proportion. What skyRect height part will go to
        // mountains.
        float mountainHeightProportion = .4f;
        float mountainWidthProportion = .5f;

        BitmapDrawable mountain = (BitmapDrawable) getResources().getDrawable(
                R.drawable.maze_mountain1);
        mountain.setFilterBitmap(true);
        mountain.setBounds(skyRect);
        mountain.getBounds().top = (int) (skyRect.height() * mountainHeightProportion);
        mountain.getBounds().right = (int) (skyRect.width() * mountainWidthProportion);
        mountain.draw(canvas);

        mountain = (BitmapDrawable) getResources().getDrawable(R.drawable.maze_mountain2);
        mountain.setFilterBitmap(true);
        mountain.setBounds(skyRect);
        mountain.getBounds().top = (int) (skyRect.height() * mountainHeightProportion);
        mountain.getBounds().right = (int) (skyRect.width() * mountainWidthProportion);
        mountain.getBounds().offsetTo((int) (skyRect.width() * .25f), mountain.getBounds().top);
        mountain.draw(canvas);

        mountain = (BitmapDrawable) getResources().getDrawable(R.drawable.maze_mountain3);
        mountain.setFilterBitmap(true);
        mountain.setBounds(skyRect);
        mountain.getBounds().top = (int) (skyRect.height() * mountainHeightProportion);
        mountain.getBounds().left = (int) (skyRect.width() * mountainWidthProportion);
        mountain.draw(canvas);

        // Draw grass as 25% of skyRect height.
        BitmapDrawable grass = (BitmapDrawable) getResources().getDrawable(R.drawable.maze_grass);
        float scale = skyRect.height() * 0.25f / grass.getIntrinsicHeight();
        grass.setBounds(0, 0,
                (int) (grass.getIntrinsicWidth() * scale),
                (int) (grass.getIntrinsicHeight() * scale));

        Bitmap tmpBitmap = Bitmap.createBitmap(grass.getBounds().width(), grass.getBounds()
                .height(), Config.ARGB_8888);
        grass.setFilterBitmap(true);
        grass.draw(new Canvas(tmpBitmap));

        grass = new BitmapDrawable(getResources(), tmpBitmap);
        grass.setFilterBitmap(true);

        // Draw grass tree times(with shifted left position) on sky bitamp to
        // achieve unique pattern.
        grass.setTileModeX(TileMode.MIRROR);
        grass.setBounds(skyRect);
        canvas.save();
        canvas.translate(0, skyRect.height() - grass.getIntrinsicHeight());
        grass.draw(canvas);
        canvas.translate(-grass.getIntrinsicWidth() / 2, 0);
        grass.draw(canvas);
        canvas.translate(2 * grass.getIntrinsicWidth() / 2, 0);
        grass.draw(canvas);
        canvas.restore();

        // Draw not strait top of ground
        BitmapDrawable groundTop = (BitmapDrawable) getResources().getDrawable(
                R.drawable.maze_ground_top);
        groundTop.setTileModeX(TileMode.MIRROR);
        groundTop.setBounds(skyRect);
        canvas.save();
        canvas.translate(0, skyRect.height() - groundTop.getIntrinsicHeight());
        groundTop.draw(canvas);
        canvas.restore();

        return result;
    }

    private Bitmap createGroundWithMazeBitmap(int width, int height) {
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(getResources().getColor(R.color.maze_ground_color));

        // Initialize ground wave decoration
        BitmapDrawable drawableGroundDecor = (BitmapDrawable) getResources().getDrawable(
                R.drawable.maze_ground_decoration);
        drawableGroundDecor.setBounds(0, 0, width, height);
        drawableGroundDecor.setTileModeXY(TileMode.MIRROR, TileMode.MIRROR);
        drawableGroundDecor.draw(canvas);

        // Draw maze
        drawMaze(canvas);

        // Initialize bottom water decoration
        BitmapDrawable waterDecor = (BitmapDrawable) getResources().getDrawable(
                R.drawable.maze_bottom_water);
        waterDecor.setBounds(0, 0, width, waterDecor.getIntrinsicHeight());
        waterDecor.setTileModeXY(TileMode.MIRROR, TileMode.MIRROR);
        int size = (int) currentRect.left / (waterDecor.getIntrinsicWidth() * 2) + 1;
        int shift = (int) Math.abs(currentRect.left - waterDecor.getIntrinsicWidth() * 2 * size
                + getTileWidth() / 2);
        waterDecor.getBounds().right += shift;

        // Draw bottom water
        canvas.save();
        canvas.translate(-shift, height - mazeOffsetHeight * 2 - getTileHeight());
        waterDecor.draw(canvas);

        canvas.translate(0, waterDecor.getIntrinsicHeight());
        water.setBounds(0, 0, waterDecor.getBounds().width(), sceneRect.height() - height);
        water.draw(canvas);
        canvas.restore();

        return result;
    }

    private void drawMazeCell(Canvas canvas, int mazeType, int row, int col) {
        Drawable type;
        switch (mazeType) {
            case MazePuzzle.WALL:
                return;
            case MazePuzzle.VISITED:
                type = water;
                break;

            default:
                type = null;
                break;
        }
        tileRect.offsetTo((int) (getXOffset() + getTileWidth() * col),
                (int) (getYOffset() + getTileHeight() * row));
        if (type != null) {
            type.setBounds(tileRect);
            type.draw(canvas);
        } else {
            canvas.save();
            canvas.clipRect(tileRect);
            canvas.drawColor(groundColor);
            canvas.restore();
        }
    }

    private void drawMaze(Canvas canvas) {
        int[][] tiles = getTiles();
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                drawMazeCell(canvas, tiles[i][j], i, j);
            }
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        // Draw sky gradient
        paint.setShader(gradient);
        canvas.drawRect(skyRect, paint);
        paint.setShader(null);

        // Draw clouds
        for (int i = 0; i < cloudRect.length; i++) {
            cloud.setBounds(cloudRect[i]);
            cloud.draw(canvas);
        }

        // Draw sun
        canvas.save();
        canvas.clipRect(skyRect);
        sun.getFrame(sunIndex).setBounds(sun.getBounds());
        sun.getFrame(sunIndex).draw(canvas);
        canvas.restore();

        // Draw sky and ground decoration
        canvas.drawBitmap(skyBitmap, null, skyRect, paint);

        canvas.save();
        canvas.translate(0, skyRect.height());
        canvas.drawBitmap(groundBitmap, 0, 0, paint);
        canvas.restore();

        // Draw tree
        tree.getFrame(treeIndex).setBounds(treeRect);
        tree.getFrame(treeIndex).draw(canvas);

        // Draw control
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(true);
        paint.setAlpha(127);
        canvas.drawCircle(currentRect.centerX(), currentRect.centerY(),
                currentRect.width()
                        * (isMovingMode ? 1.25f : 0.75f), paint);
        canvas.drawCircle(currentRect.centerX(), currentRect.centerY(),
                currentRect.width() * .25f, paint);
        paint.setAntiAlias(false);
        paint.setAlpha(255);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (controlRect.contains(event.getX(), event.getY())) {
                    isMovingMode = true;
                    result = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isMovingMode) {
                    float widthX = event.getX() - currentRect.centerX();
                    float widthY = event.getY() - currentRect.centerY();

                    float absX = Math.abs(widthX);
                    float absY = Math.abs(widthY);

                    if (absX > getTileWidth() || absY > getTileHeight()) {

                        boolean moved = false;
                        if (absX > absY) {
                            moved = moveControlRectTo(controlPosition.x, controlPosition.y
                                    + (widthX > 0 ? 1 : -1));
                        } else {
                            moved = moveControlRectTo(controlPosition.x + (widthY > 0 ? 1 : -1),
                                    controlPosition.y);
                        }

                        if (!moved) {
                            if (absX > absY) {
                                if (absY > getTileHeight()) {
                                    moved = moveControlRectTo(controlPosition.x
                                            + (widthY > 0 ? 1 : -1), controlPosition.y);
                                }
                            } else {
                                if (absX > getTileWidth()) {
                                    moved = moveControlRectTo(controlPosition.x,
                                            controlPosition.y
                                                    + (widthX > 0 ? 1 : -1));
                                }
                            }
                        }
                        result = true;
                    }
                }
                break;
            default:
                isMovingMode = false;
        }
        return result;
    }

    public void setOnFinishListener(OnMazeFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    public Point getControlPositionA() {
        return controlPosition;
    }

    public void setControlPositionA(Point controlPositionA) {
        this.controlPosition = controlPositionA;
        moveControlRectTo(controlPositionA.x, controlPositionA.y);
    }

    private long prevClouds = 0;
    private long prevSun = 0;
    private long prevTree = 0;

    @Override
    protected boolean updatePuzzleState(long timeElapsed) {
        if (prevClouds > timeElapsed) {
            prevClouds = 0;
        }
        if (prevSun > timeElapsed) {
            prevSun = 0;
        }
        if (prevTree > timeElapsed) {
            prevTree = 0;
        }

        if (timeElapsed - prevClouds > cloudAnimationSpeed) {
            int step = (int) ((timeElapsed - prevClouds) / cloudAnimationSpeed);
            step = Math.max(step, 1);
            for (int i = 0; i < cloudRect.length; i++) {
                cloudRect[i].offset(2 * step, 0);
                if (cloudRect[i].left > skyRect.width()) {

                    cloudRect[i].offsetTo(-cloudRect[i].width(), cloudRect[i].top);
                }
            }
            prevClouds = timeElapsed;
        }
        if (timeElapsed - prevSun > sunAnimationSpeed) {
            if (sunIndex >= sun.getNumberOfFrames() - 1) {
                sunIndex = 0;
            } else {
                sunIndex++;
            }
            prevSun = timeElapsed;
        }
        if (finished && timeElapsed - prevTree > treeAnimationSpeed) {
            if (treeIndex < tree.getNumberOfFrames() - 1) {
                treeIndex++;
            }
            prevTree = timeElapsed;
        }
        return false;
    }
}
