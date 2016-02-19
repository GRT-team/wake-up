
package com.grt_team.wakeup.entity.puzzle.random;

import android.content.Context;
import android.os.Bundle;
import android.text.Html.ImageGetter;
import android.util.DisplayMetrics;
import android.view.View;

import com.grt_team.wakeup.entity.puzzle.Puzzle;
import com.grt_team.wakeup.entity.puzzle.PuzzleActivity;
import com.grt_team.wakeup.entity.puzzle.PuzzleHelper;

public class RandomPuzzle extends Puzzle {

    private static final String EXTRA_PUZZLE_NAME = "randomPuzzleName";

    private Puzzle puzzle;
    private String puzzleName;

    private void initPuzzle(String puzzleName) {
        if (null == puzzle) {
            if (null == puzzleName) {
                puzzleName = PuzzleHelper.getRandomPuzzleName();
            }
            this.puzzleName = puzzleName;
            puzzle = PuzzleHelper.getPuzzleNewInstace(puzzleName, getContext());
        }
    }

    @Override
    public View onPuzzleRun(DisplayMetrics metrics) {
        initPuzzle(null);
        puzzle.setCallback((PuzzleActivity) getContext());
        return puzzle.onPuzzleRun(metrics);
    }

    @Override
    public void onSave(Bundle savedInstanceState) {
        if (null != puzzle) {
            puzzle.onSave(savedInstanceState);
            savedInstanceState.putString(EXTRA_PUZZLE_NAME, puzzleName);
        }
    }
    
    @Override
    public View getPuzzleHelpDescriptionView(Context context, int resId) {
        return puzzle.getPuzzleHelpDescriptionView(context, resId);
    }
    
    @Override
    public ImageGetter getPuzzleDescriptionGetter(Context context) {
        return puzzle.getPuzzleDescriptionGetter(context);
    }

    @Override
    public void onRestore(Bundle savedInstanceState) {
        if (null == puzzle) {
            initPuzzle(savedInstanceState.getString(EXTRA_PUZZLE_NAME));
        }
        puzzle.onRestore(savedInstanceState);
    }

    @Override
    public int getPuzzleResTitle() {
        return puzzle.getPuzzleResTitle();
    }

    @Override
    public int getPuzzleResBigDescription() {
        return puzzle.getPuzzleResBigDescription();
    }
}
