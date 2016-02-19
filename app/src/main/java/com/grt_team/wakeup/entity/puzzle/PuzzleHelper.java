
package com.grt_team.wakeup.entity.puzzle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.SettingsActivity;
import com.grt_team.wakeup.dialog.AlarmDialog;
import com.grt_team.wakeup.dialog.FullScreenAlarmDialog;
import com.grt_team.wakeup.entity.puzzle.card.CardPuzzle;
import com.grt_team.wakeup.entity.puzzle.maze.MazePuzzle;
import com.grt_team.wakeup.entity.puzzle.mosaic.MosaicPuzzle;
import com.grt_team.wakeup.entity.puzzle.random.RandomPuzzle;
import com.grt_team.wakeup.utils.AlarmHelper;

public class PuzzleHelper {

    public static final String PUZZLE_MAZE = "maze";
    public static final String PUZZLE_MOSAIC = "mosaic";
    public static final String PUZZLE_CARDS = "cards";
    public static final String PUZZLE_RANDOM = "random";

    private static Map<String, Class<? extends Puzzle>> puzzles;
    private static Map<String, String> puzzleTitles = null;

    static {
        puzzles = new HashMap<String, Class<? extends Puzzle>>();
        puzzles.put(PUZZLE_MAZE, MazePuzzle.class);
        puzzles.put(PUZZLE_MOSAIC, MosaicPuzzle.class);
        puzzles.put(PUZZLE_CARDS, CardPuzzle.class);
        puzzles.put(PUZZLE_RANDOM, RandomPuzzle.class);
    }

    private static void initPuzzleTitles(Context context) {
        if (puzzleTitles == null) {
            puzzleTitles = new HashMap<String, String>();
        }
        puzzleTitles.clear();
        puzzleTitles.put(PUZZLE_MAZE,
                context.getResources().getString(R.string.puzzle_maze_title));
        puzzleTitles.put(PUZZLE_MOSAIC,
                context.getResources().getString(R.string.puzzle_mosaic_title));
        puzzleTitles.put(PUZZLE_CARDS,
                context.getResources().getString(R.string.puzzle_card_title));
        puzzleTitles.put(PUZZLE_RANDOM,
                context.getResources().getString(R.string.puzzle_random_title));
    }

    /**
     * Call {@code initPuzzleTitles} before to initialize titles from resource.
     * 
     * @param title
     * @return
     */
    private static String getPuzzleNameByTitle(String title) {
        for (String name : puzzleTitles.keySet()) {
            if (puzzleTitles.get(name).equals(title)) {
                return name;
            }
        }
        return null;
    }

    public static String getPuzzleTitle(String puzzleName, Context context) {
        initPuzzleTitles(context);
        return puzzleTitles.get(puzzleName);
    }

    public static String getPuzzleNameByTitle(String title, Context context) {
        initPuzzleTitles(context);
        return getPuzzleNameByTitle(title);
    }

    public static String[] getPuzzleTitles(Context context) {
        initPuzzleTitles(context);
        String[] titles = new String[puzzleTitles.size()];
        puzzleTitles.values().toArray(titles);
        return titles;
    }

    private PuzzleHelper() {
    }

    public interface OnPuzzleSelectedListener {
        public void onPuzzleSelected(String puzzlName);
    }

    public static Puzzle getPuzzleNewInstace(String name, Context context) {
        Puzzle puzzle = null;
        try {
            puzzle = (Puzzle) puzzles.get(name).newInstance();
            puzzle.initDefaults(context);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return puzzle;
    }

    public static String getRandomPuzzleName() {
        Random r = new Random();

        Set<String> set = new HashSet<String>(puzzles.keySet());
        set.remove(PUZZLE_RANDOM);

        List<String> list = new ArrayList<String>(set);

        return list.get(r.nextInt(list.size()));
    }

    public static void runPuzzle(Context context, String puzzleName,
            boolean previewMode, long id) {
        boolean fullScreen = context instanceof FullScreenAlarmDialog;
        Intent i = new Intent(context, (previewMode) ? PuzzleActivityPreview.class
                : PuzzleActivity.class);
        i.setData(AlarmHelper.getDataUri(id));
        i.putExtra(PuzzleActivity.EXTRA_PUZZLE_START_TIME, System.currentTimeMillis());
        i.putExtra(PuzzleActivity.EXTRA_PUZZLE_NAME, puzzleName);
        i.putExtra(PuzzleActivity.EXTRA_PREVIEW_MODE, previewMode);
        i.putExtra(AlarmDialog.FULL_SCREEN_DIALOG, fullScreen);
        context.startActivity(i);
    }

    public static String[] getPuzzleNames() {
        String[] puzzleNames = new String[puzzles.size()];
        return puzzles.keySet().toArray(puzzleNames);
    }

    private static int getElementPosition(String find, String[] all) {
        for (int i = 0; i < all.length; i++) {
            if (all[i].equals(find)) {
                return i;
            }
        }
        return -1;
    }

    public static AlertDialog getPuzzlePickerDialog(Context context,
            String selectedName, final OnPuzzleSelectedListener listener) {
        Builder builder = new Builder(context);
        builder.setTitle(R.string.puzzle_picker_title);
        builder.setNegativeButton(android.R.string.cancel, null);
        String[] titles = getPuzzleTitles(context);

        builder.setSingleChoiceItems(
                titles,
                getElementPosition(getPuzzleTitle(selectedName, context),
                        titles), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (null != listener) {
                            String puzzleTitle = (String) ((AlertDialog) dialog)
                                    .getListView().getItemAtPosition(which);
                            listener.onPuzzleSelected(getPuzzleNameByTitle(puzzleTitle));
                            dialog.dismiss();
                        }
                    }
                });
        return builder.create();
    }

    public static void showPuzzleSettings(Context context, String puzzleName) {
        String action = null;
        String fragment = null;
        Intent i = new Intent(context, SettingsActivity.class);

        boolean useFragments = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB);

        if (PUZZLE_MAZE.equals(puzzleName)) {
            action = SettingsActivity.ACTION_PREFS_PUZZLE_MAZE;
            fragment = (useFragments) ? "com.grt_team.wakeup.fragment.SettingsFragment$MazeSettingsFragment"
                    : null;
        } else if (PUZZLE_MOSAIC.equals(puzzleName)) {
            action = SettingsActivity.ACTION_PREFS_PUZZLE_MOSAIC;
            fragment = (useFragments) ? "com.grt_team.wakeup.fragment.SettingsFragment$MosaicSettingsFragment"
                    : null;
        } else if (PUZZLE_CARDS.equals(puzzleName)) {
            action = SettingsActivity.ACTION_PREFS_PUZZLE_CARDS;
            fragment = (useFragments) ? "com.grt_team.wakeup.fragment.SettingsFragment$CardsSettingsFragment"
                    : null;
        } else {
            action = SettingsActivity.ACTION_PREFS_PUZZLE;
            fragment = (useFragments) ? "com.grt_team.wakeup.fragment.SettingsFragment" : null;
            Bundle bundle = new Bundle();
            bundle.putString("settings", "preference_puzzle");
            i.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle);
        }

        i.setAction(action);
        i.putExtra(SettingsActivity.EXTRA_HEADER_ID, (long) R.id.pref_header_puzzle);
        i.putExtra(SettingsActivity.EXTRA_FORCE_RECREATE_TASK, true);
        i.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, fragment);

        context.startActivity(i);
    }
}
