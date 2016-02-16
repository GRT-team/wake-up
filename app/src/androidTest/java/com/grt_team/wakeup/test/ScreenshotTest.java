
package com.grt_team.wakeup.test;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import android.os.Build;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;
import android.view.View;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.SettingsActivity;
import com.grt_team.wakeup.WakeUpActivity;
import com.grt_team.wakeup.entity.puzzle.PuzzleRescueActivity;
import com.grt_team.wakeup.test.util.ScreenshotHelper;
import com.grt_team.wakeup.test.util.SimpleWaiter;
import com.grt_team.wakeup.test.util.SoloFetcher;
import com.grt_team.wakeup.test.util.WaiterBuilder;
import com.grt_team.wakeup.test.util.WakeUpHelper;
import com.grt_team.wakeup.utils.DayOfWeekHelper;
import com.grt_team.wakeup.utils.SharedPreferenceUtil;
import com.jayway.android.robotium.solo.Solo;

public class ScreenshotTest extends
        ActivityInstrumentationTestCase2<WakeUpActivity> {

    private String SCREEN_FILE_NAME = "%s%d_%s_%s_SHOTS";
    private Solo solo;
    private WakeUpHelper wHelper;
    private int screenIndex = 0;
    private String screenPrefix;
    private boolean twoPane;
    private SoloFetcher fetcher;

    private String getScreenName(String tag) {
        Locale locale = solo.getCurrentActivity().getResources().getConfiguration().locale;
        return String.format(SCREEN_FILE_NAME, screenPrefix, screenIndex, locale.toString(), tag);
    }

    public ScreenshotTest() {
        super(WakeUpActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        SharedPreferenceUtil.saveNextFeedbackTime(getInstrumentation().getTargetContext(), 0);

        long next = SharedPreferenceUtil.getNextFeedbackTime(getInstrumentation()
                .getTargetContext());
        assertTrue(next == 0);
        next = System.currentTimeMillis() - 259201000;
        SharedPreferenceUtil.saveNextFeedbackTime(getInstrumentation().getTargetContext(), next);

        solo = new Solo(getInstrumentation(), getActivity());

        final String path = Environment.getExternalStorageDirectory() + "/Robotium-Screenshots/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        screenPrefix = String.valueOf(Build.VERSION.SDK_INT) + "_" + metrics.widthPixels + "x"
                + metrics.heightPixels + "_" + Math.round(160 * metrics.density) + "_";

        twoPane = getActivity().getResources().getBoolean(com.grt_team.wakeup.R.bool.has_two_panes);
        wHelper = new WakeUpHelper(solo, twoPane, getInstrumentation());
        fetcher = new SoloFetcher(solo, getInstrumentation());
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
        SharedPreferenceUtil.saveNextFeedbackTime(getInstrumentation().getTargetContext(), 0);
    }

    private void takeScreenshot(String tag) throws TimeoutException {
        takeScreenshot(tag, fetcher.getCurrentRootViewSafe());
    }

    private void takeScreenshot(String tag, View view) {
        ScreenshotHelper.takeScreen(getInstrumentation(), solo, getScreenName(tag), view);
        screenIndex++;
    }

    public void testAppStart() throws TimeoutException {
        String tag = "app_start";

        examFeedback("feedback");

        // Application main activity
        takeScreenshot(tag);

        // Add new alarm
        wHelper.clickBarAddNewAlarm();

        wHelper.openDialogPref(R.string.alarm_setting_day_title);
        wHelper.selectDays(DayOfWeekHelper.MONDAY,
                DayOfWeekHelper.TUESDAY,
                DayOfWeekHelper.WEDNESDAY,
                DayOfWeekHelper.THURSDAY,
                DayOfWeekHelper.FRIDAY,
                DayOfWeekHelper.SATURDAY);

        wHelper.closeDialogText(android.R.string.ok);

        takeScreenshot(tag);

        // Go to 'Settings' - 'Puzzle Settings'
        wHelper.clickBarSettings();

        wHelper.clickAllPuzzleSettings();
        solo.scrollUp();

        exampMazePuzzleFromPuzzleSettings("puzzle_maze");
        exmaMosaicPuzzleFormPuzzleSettings("puzzle_mosaic");
        examCardPuzzleFromPuzzlesSettings("puzzle_card");
    }

    public void examFeedback(String tag) throws TimeoutException {
        SimpleWaiter waiter = WaiterBuilder.initRootCountWaiter(fetcher, 2);
        waiter.beginWait();

        takeScreenshot(tag);
        View view = fetcher.findViewByText(R.string.feedback_not_today);

        waiter = WaiterBuilder.initRootCountWaiter(fetcher, 1);
        wHelper.clickOnView(view);
        waiter.beginWait();
    }

    private void exampMazePuzzleFromPuzzleSettings(String tag) throws TimeoutException {
        // Got to 'Maze' settings
        wHelper.clickPuzzleSettings(R.string.puzzle_maze_title);

        // Set min maze size
        wHelper.openDialogPref(R.string.pref_puzzle_maze_size_title);
        wHelper.setSeekBarValue(0, 0);
        takeScreenshot(tag);
        wHelper.closeDialogText(android.R.string.ok);

        // Preview maze
        View puzzleView = wHelper.clickPuzzlePreview();
        takeScreenshot(tag, puzzleView);

        // Open rescue dialog
        wHelper.clickRescueButton();
        takeScreenshot(tag);

        // Open rescue activity

        SimpleWaiter aWaiter = WaiterBuilder.initActivityWaiter(PuzzleRescueActivity.class,
                getInstrumentation());
        wHelper.closeDialogText(R.string.rescue_dialog_off_btn);
        aWaiter.beginWait();
        // Rescue activity will recreate current preview, so wait for same
        // number

        takeScreenshot(tag);
        wHelper.changeScreenOrientation();
        takeScreenshot(tag);
        wHelper.clickOnText(R.string.rescue_back);

        // Back to maze settings
        wHelper.goBackSimpleWait(SettingsActivity.class, android.R.string.ok, true);

        // Set max maze size
        wHelper.openDialogPref(R.string.pref_puzzle_maze_size_title);
        wHelper.setSeekBarValue(WakeUpHelper.SEEK_BAR_MAX_VALUE, 0);
        takeScreenshot(tag);
        wHelper.closeDialogText(android.R.string.ok);

        // Preview maze
        puzzleView = wHelper.clickPuzzlePreview();
        takeScreenshot(tag, puzzleView);

        // Back to puzzle settings
        wHelper.goBackSimpleWait(SettingsActivity.class, android.R.string.ok, true);
        wHelper.goBackSimpleWait(SettingsActivity.class, !twoPane);
    }

    private void exmaMosaicPuzzleFormPuzzleSettings(String tag) throws
            TimeoutException {
        // Open mosaic settings
        wHelper.clickPuzzleSettings(R.string.puzzle_mosaic_title);

        // Set mosaic size 2x2, mosaic SeekBar step is 2
        wHelper.openDialogPref(R.string.pref_puzzle_mosaic_size_title);
        wHelper.setSeekBarValue(0, 0);
        wHelper.setSeekBarValue(0, 1);
        wHelper.closeDialogText(android.R.string.ok);

        // Preview mosaic
        View puzzleView = wHelper.clickPuzzlePreview();
        takeScreenshot(tag, puzzleView);

        // Click rescue button: open description dialog
        wHelper.clickRescueButton();
        takeScreenshot(tag);
        wHelper.closeDialogText(R.string.rescue_dialog_cancel_btn);

        // Back to mosaic settings
        wHelper.goBackSimpleWait(SettingsActivity.class, android.R.string.ok, true);

        // Set mosaic size 9x9, SeekBar step is 2
        wHelper.openDialogPref(R.string.pref_puzzle_mosaic_size_title);
        wHelper.setSeekBarValue(7, 0);
        wHelper.setSeekBarValue(7, 1);
        wHelper.closeDialogText(android.R.string.ok);

        // Preview mosaic
        puzzleView = wHelper.clickPuzzlePreview();
        takeScreenshot(tag, puzzleView);

        // Back to puzzles settings
        wHelper.goBackSimpleWait(SettingsActivity.class, android.R.string.ok, true);
        wHelper.goBackSimpleWait(SettingsActivity.class, !twoPane);
    }

    private void examCardPuzzleFromPuzzlesSettings(String tag) throws
            TimeoutException {
        // Go to cards settings (must be NOT poker mode)
        wHelper.clickPuzzleSettings(R.string.puzzle_card_title);
        assertFalse("Must be NOT poker mode at start.",
                wHelper.isCardPuzzlePokerMode());

        // Set up cards to be collected max
        wHelper.openDialogPref(R.string.pref_puzzle_cards_task_number_title);
        wHelper.setSeekBarValue(WakeUpHelper.SEEK_BAR_MAX_VALUE, 0);
        wHelper.closeDialogText(android.R.string.ok);

        // Set up cards count in deck 24
        solo.scrollDown();
        wHelper.openDialogPref(R.string.pref_puzzle_cards_deck_size_title);
        String[] cards = solo.getCurrentActivity().getResources()
                .getStringArray(R.array.puzzle_cards_deck_size_titles);
        solo.scrollUp();
        wHelper.clickDialogText(cards[0]);

        // Preview card puzzle
        View puzzleView = wHelper.clickPuzzlePreview();
        takeScreenshot(tag, puzzleView);

        // Click rescue button: open description dialog
        wHelper.clickRescueButton();
        takeScreenshot(tag);
        wHelper.closeDialogText(R.string.rescue_dialog_cancel_btn);

        // Back to card settings
        wHelper.goBackSimpleWait(SettingsActivity.class, android.R.string.ok, true);

        // Set up cards count in deck 36
        solo.scrollDown();
        wHelper.openDialogPref(R.string.pref_puzzle_cards_deck_size_title);
        wHelper.clickDialogText(cards[1]);

        // Preview card puzzle
        puzzleView = wHelper.clickPuzzlePreview();
        takeScreenshot(tag, puzzleView);

        // Back to card settings
        wHelper.goBackSimpleWait(SettingsActivity.class, android.R.string.ok, true);

        // Set up cards count in deck 52
        solo.scrollDown();
        wHelper.openDialogPref(R.string.pref_puzzle_cards_deck_size_title);
        wHelper.clickDialogText(cards[2]);

        // Preview card puzzle
        puzzleView = wHelper.clickPuzzlePreview();
        takeScreenshot(tag, puzzleView);

        // Back to card settings
        wHelper.goBackSimpleWait(SettingsActivity.class, android.R.string.ok, true);

        // Switch to poker mode
        wHelper.setCardPuzzlePokerMode(true);

        // Preview card puzzle
        puzzleView = wHelper.clickPuzzlePreview();
        takeScreenshot(tag, puzzleView);

        // Check confirmation dialog
        SimpleWaiter waiter = WaiterBuilder.initRootChangeWaiter(fetcher);
        solo.goBack();
        waiter.beginWait();
        takeScreenshot(tag);
    }

}
