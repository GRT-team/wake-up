
package com.grt_team.wakeup.entity.puzzle.mosaic;

import java.util.ArrayList;
import java.util.Stack;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.SettingsActivity;
import com.grt_team.wakeup.entity.puzzle.Puzzle;
import com.grt_team.wakeup.entity.puzzle.mosaic.MosaicView.OnMosaicCompletedListener;
import com.grt_team.wakeup.entity.puzzle.mosaic.MosaicView.PuzzleItem;
import com.grt_team.wakeup.preferences.MosaicSizeBarPreferences;
import com.grt_team.wakeup.preferences.PickImagePreferences;
import com.grt_team.wakeup.utils.DisplayHelper;

public class MosaicPuzzle extends Puzzle implements OnMosaicCompletedListener {

    private final static String MOSAIC_PUZZLES_FREE = "MOSAIC_PUZZLES_FREE";
    private final static String MOSAIC_PUZZLES_PLACED = "MOSAIC_PUZZLES_PLACED";

    private final static String MOSAIC_USE_CUSTOM_IMG = "MOSAIC_USE_CUSTOM_IMG";
    private final static String MOSAIC_CUSTOM_FILE_NAME = "MOSAIC_CUSTOM_FILE_NAME";
    private final static String MOSAIC_SIZE_X = "MOSAIC_SIZE_X";
    private final static String MOSAIC_SIZE_Y = "MOSAIC_SIZE_Y";
    private final static String MOSAIC_DRAW_DASH = "MOSAIC_DRAW_DASH";
    private final static String MOSAIC_DRAW_BACKGROUND = "MOSAIC_DRAW_BACKGROUND";

    MosaicView view;

    Stack<PuzzleItem> free = new Stack<PuzzleItem>();
    Stack<PuzzleItem> placed = new Stack<PuzzleItem>();

    private boolean useCustomImage;
    private String customFileName;
    private Point mosaicSize;
    private boolean drawDash;
    private boolean drawBackground;

    public MosaicPuzzle() {
        this.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void initDefaults(Context context) {
        super.initDefaults(context);

        useCustomImage = SettingsActivity.getPref(getContext()).getBoolean(
                SettingsActivity.PREF_PUZZLE.MOSAIC_USE_CUSTOM_IMG, true);
        customFileName = SettingsActivity.getPref(getContext()).getString(
                SettingsActivity.PREF_PUZZLE.MOSAIC_IMAGE, PickImagePreferences.DEF_FILE);
        mosaicSize = MosaicSizeBarPreferences.valueFromString(SettingsActivity
                .getPref(getContext()).getString(SettingsActivity.PREF_PUZZLE.MOSAIC_SIZE, null));
        drawDash = SettingsActivity.getPref(getContext()).getBoolean(
                SettingsActivity.PREF_PUZZLE.MOSAIC_GRID, true);
        drawBackground = SettingsActivity.getPref(getContext()).getBoolean(
                SettingsActivity.PREF_PUZZLE.MOSAIC_BG, true);
    }

    private String getValidImageAddress() {
        String fileName = PickImagePreferences.DEF_FILE;
        if (useCustomImage) {
            fileName = customFileName;
        }

        return fileName;
    }

    @Override
    public View onPuzzleRun(DisplayMetrics metrics) {
        View scene = LayoutInflater.from(getContext()).inflate(R.layout.puzzle_mosaic_layout, null);
        FrameLayout mosaic_place = (FrameLayout) scene.findViewById(R.id.mosaic_pazle_field);

        int mosaic_place_height = (int) getContext().getResources().getDimension(
                R.dimen.puzzle_title_height);
        mosaic_place_height = DisplayHelper.getScreenHeight(getContext()) - mosaic_place_height;

        view = new MosaicView(getContext());
        view.setOnMosaicCompletedListener(this);
        view.setOnUserActionPerformedListener(this);
        view.setDrawDash(drawDash);
        view.setDrawBackground(drawBackground);
        view.setDelta(25);

        view.setPuzzleSrc(getValidImageAddress(), DisplayHelper.getScreenWidth(getContext()),
                mosaic_place_height,
                mosaicSize.x, mosaicSize.y);

        if (free.size() != 0) {
            view.setFreePuzzles(free);
            view.setPlacedPuzzles(placed);
        } else {
            view.generatePuzzles();
            free.clear();
            placed.clear();
            free.addAll(view.getFreePuzzles());
            placed.addAll(view.getPlacedPuzzles());
        }
        mosaic_place.addView(view);
        return scene;
    }

    @Override
    public void onSave(Bundle savedInstanceState) {
        if (view != null) {
            free.clear();
            placed.clear();
            free.addAll(view.getFreePuzzles());
            placed.addAll(view.getPlacedPuzzles());
        }
        savedInstanceState.putParcelableArrayList(MOSAIC_PUZZLES_FREE, new ArrayList<Parcelable>(
                free));
        savedInstanceState.putParcelableArrayList(MOSAIC_PUZZLES_PLACED, new ArrayList<Parcelable>(
                placed));

        savedInstanceState.putBoolean(MOSAIC_USE_CUSTOM_IMG, useCustomImage);
        savedInstanceState.putString(MOSAIC_CUSTOM_FILE_NAME, customFileName);
        savedInstanceState.putInt(MOSAIC_SIZE_X, mosaicSize.x);
        savedInstanceState.putInt(MOSAIC_SIZE_Y, mosaicSize.y);
        savedInstanceState.putBoolean(MOSAIC_DRAW_DASH, drawDash);
        savedInstanceState.putBoolean(MOSAIC_DRAW_BACKGROUND, drawBackground);
    }

    @Override
    public void onRestore(Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            ArrayList<PuzzleItem> free = savedInstanceState
                    .getParcelableArrayList(MOSAIC_PUZZLES_FREE);
            ArrayList<PuzzleItem> placed = savedInstanceState
                    .getParcelableArrayList(MOSAIC_PUZZLES_PLACED);
            if (free == null || placed == null) {
                return;
            }
            this.free.clear();
            this.placed.clear();
            this.free.addAll(free);
            this.placed.addAll(placed);

            this.useCustomImage = savedInstanceState.getBoolean(MOSAIC_USE_CUSTOM_IMG);
            this.customFileName = savedInstanceState.getString(MOSAIC_CUSTOM_FILE_NAME);
            int x = savedInstanceState.getInt(MOSAIC_SIZE_X);
            int y = savedInstanceState.getInt(MOSAIC_SIZE_Y);
            this.mosaicSize = new Point(x, y);
            this.drawDash = savedInstanceState.getBoolean(MOSAIC_DRAW_DASH);
            this.drawBackground = savedInstanceState.getBoolean(MOSAIC_DRAW_BACKGROUND);
        }
    }

    @Override
    public void onMosaicCompleted() {
        endPuzzle();
    }

    @Override
    public int getPuzzleResTitle() {
        return R.string.puzzle_mosaic_title;
    }

    @Override
    public int getPuzzleResBigDescription() {
        return R.string.puzzle_mosaic_aim;
    }

    @Override
    public View getPuzzleHelpDescriptionView(Context context, int resId) {
        ImageView imageView = new ImageView(context);
        String file = getValidImageAddress();
        imageView.setImageBitmap(MosaicView.resolveBitmap(getContext(), file));
        return imageView;
    }
}
