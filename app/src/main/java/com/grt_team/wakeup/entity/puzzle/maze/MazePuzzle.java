
package com.grt_team.wakeup.entity.puzzle.maze;

import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.View;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.SettingsActivity;
import com.grt_team.wakeup.entity.puzzle.Puzzle;
import com.grt_team.wakeup.utils.DisplayHelper;

public class MazePuzzle extends Puzzle implements MazeView.OnMazeFinishListener {

    private static final String MAZE_LABYRINTH = "Maze-Labyrinth";

    public static final int WALL = 0;
    public static final int FREE = 1;
    public static final int VISITED = 2;
    public static final int END = 3;

    public static final int TOTAL_IMAGES_COUNT = 4;

    private int[][] maze;
    private Point controlPositionA;

    private MazeView puzzleView;

    public MazePuzzle() {
        this.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public View onPuzzleRun(DisplayMetrics metrics) {
        puzzleView = new MazeView(getContext());

        float maze_place_width = DisplayHelper.getScreenWidth(getContext());
        float maze_place_height = getContext().getResources().getDimension(
                R.dimen.puzzle_title_height);
        maze_place_height = DisplayHelper.getScreenHeight(getContext()) - 2 * maze_place_height;

        if (maze == null) {
            int cols = SettingsActivity.getPref(getContext()).getInt(
                    SettingsActivity.PREF_PUZZLE.MAZE_SIZE, -1);
            int rows = (int) (maze_place_height / (maze_place_width / cols));
            maze = MazeGenerator.generateMaze(rows, cols, true);
        }

        puzzleView.setTiles(maze);
        puzzleView.initTileSize(new Rect(0, 0, DisplayHelper.getScreenWidth(getContext()),
                DisplayHelper.getScreenHeight(getContext())));
        puzzleView.setOnFinishListener(this);
        puzzleView.setOnUserActionPerformedListener(this);

        if (controlPositionA != null) {
            puzzleView.setControlPositionA(controlPositionA);
        } else {
            controlPositionA = puzzleView.getControlPositionA();
        }

        return inflateRescueButton(puzzleView);
    }

    @Override
    public void onSave(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(MAZE_LABYRINTH, new TwoDimensionIntArray(maze));
        savedInstanceState.putInt("screen", getScreenOrientation());
        // in 2.3.3. there is error during point serialization. So it is better
        // to send x & y separated
        savedInstanceState.putInt("controlPosition" + "_x", controlPositionA.x);
        savedInstanceState.putInt("controlPosition" + "_y", controlPositionA.y);
    }

    @Override
    public void onRestore(Bundle savedInstanceState) {
        TwoDimensionIntArray parcelableArray = (TwoDimensionIntArray) savedInstanceState
                .getParcelable(MAZE_LABYRINTH);
        maze = parcelableArray.array;
        int x = savedInstanceState.getInt("controlPosition" + "_x");
        int y = savedInstanceState.getInt("controlPosition" + "_y");
        controlPositionA = new Point(x, y);
        setScreenOrientation(savedInstanceState.getInt("screen"));
    }

    @Override
    public void onMazeFinish() {
        endPuzzle();
    }

    public static class TwoDimensionIntArray implements Parcelable {
        int[][] array;

        public TwoDimensionIntArray(int[][] array) {
            this.array = array;
        }

        private TwoDimensionIntArray(Parcel in) {
            int rows = in.readInt();
            int cols = in.readInt();
            array = new int[rows][cols];
            for (int i = 0; i < rows; i++) {
                in.readIntArray(array[i]);
            }
        }

        public static final Parcelable.Creator<TwoDimensionIntArray> CREATOR = new Parcelable.Creator<TwoDimensionIntArray>() {
            public TwoDimensionIntArray createFromParcel(Parcel in) {
                return new TwoDimensionIntArray(in);
            }

            public TwoDimensionIntArray[] newArray(int size) {
                return new TwoDimensionIntArray[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(array.length);
            dest.writeInt(array[0].length);
            for (int i = 0; i < array.length; i++) {
                dest.writeIntArray(array[i]);
            }
        }
    }

    @Override
    public int getPuzzleResTitle() {
        return R.string.puzzle_maze_title;
    }

    @Override
    public int getPuzzleResBigDescription() {
        return R.string.puzzle_maze_aim;
    }

}
