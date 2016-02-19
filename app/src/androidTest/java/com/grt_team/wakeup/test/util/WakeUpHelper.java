
package com.grt_team.wakeup.test.util;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import junit.framework.Assert;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SeekBar;

import com.grt_team.wakeup.AlarmClockSettingActivity;
import com.grt_team.wakeup.R;
import com.grt_team.wakeup.entity.puzzle.PuzzleActivityPreview;
import com.grt_team.wakeup.entity.puzzle.PuzzleView;
import com.grt_team.wakeup.test.util.SimpleWaiter.Waiter;
import com.jayway.android.robotium.solo.Condition;
import com.jayway.android.robotium.solo.Solo;

public class WakeUpHelper {
    public static final int SEEK_BAR_MAX_VALUE = -1;

    private static final int TIME_OUT = 10000;

    private boolean twoPaneMode;
    private Solo solo;
    private Instrumentation inst;
    private SoloFetcher fetcher;

    public WakeUpHelper(Solo solo, boolean twoPaneMode, Instrumentation inst) {
        this.solo = solo;
        this.twoPaneMode = twoPaneMode;
        this.inst = inst;
        this.fetcher = new SoloFetcher(solo, inst);
    }

    private String prepareString(int resId) {
        String str = solo.getCurrentActivity().getResources().getString(resId);
        str = Pattern.quote(str);
        return "^" + str + "$";
    }

    public void changeScreenOrientation() throws TimeoutException {
        Activity activity = solo.getCurrentActivity();
        int orientation = activity.getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setOrientationPortrait();
        } else {
            setOrientationLand();
        }
    }

    private void setOrientationLand() throws TimeoutException {
        solo.setActivityOrientation(Solo.LANDSCAPE);
        waitForScreenRotation(Configuration.ORIENTATION_LANDSCAPE);
    }

    private void setOrientationPortrait() throws TimeoutException {
        solo.setActivityOrientation(Solo.PORTRAIT);
        waitForScreenRotation(Configuration.ORIENTATION_PORTRAIT);
    }

    private void waitForScreenRotation(final int desireScreenOrientation) {
        solo.waitForCondition(new Condition() {

            @Override
            public boolean isSatisfied() {
                int orientation = solo.getCurrentActivity().getResources().getConfiguration().orientation;
                return (orientation == desireScreenOrientation);
            }
        }, TIME_OUT);
    }

    public void clickBarAddNewAlarm() throws TimeoutException {
        SimpleWaiter classWaiter = WaiterBuilder.initClassCountWaiter(fetcher, ListView.class, 2);
        SimpleWaiter activityWaiter = WaiterBuilder.initActivityWaiter(
                AlarmClockSettingActivity.class, inst);

        View buttonView = fetcher.findViewById(R.id.menu_add_clock);
        clickOnView(buttonView);

        if (twoPaneMode) {
            classWaiter.beginWait();
        } else {
            activityWaiter.beginWait();
        }
    }

    public void clickBarSettings() throws TimeoutException {
        View view = fetcher.findViewByClassName("OverflowMenuButton");
        if (view != null) {
            clickOnView(view);

            view = fetcher.findViewByText(R.string.menu_settings);
            clickOnView(view);
        } else {
            solo.clickOnMenuItem(prepareString(R.string.menu_settings));
        }

        WaiterBuilder.initTextWaiter(fetcher, R.string.pref_header_alarm_title).beginWait();
        WaiterBuilder.initTextWaiter(fetcher, R.string.pref_header_alarm_summary).beginWait();
    }

    public void clickAllPuzzleSettings() throws TimeoutException {
        WaiterBuilder.initTextWaiter(fetcher, R.string.pref_header_puzzle_title).beginWait();
        View view = fetcher.findViewByText(R.string.pref_header_puzzle_title);
        clickOnView(view);

        WaiterBuilder.initTextWaiter(fetcher, R.string.puzzle_maze_title).beginWait();
        WaiterBuilder.initTextWaiter(fetcher, R.string.puzzle_mosaic_title).beginWait();
        WaiterBuilder.initTextWaiter(fetcher, R.string.puzzle_card_title).beginWait();
    }

    public void clickPuzzleSettings(int puzzleNameResId) throws TimeoutException {
        WaiterBuilder.initTextWaiter(fetcher, puzzleNameResId).beginWait();
        clickOnText(puzzleNameResId);
        WaiterBuilder.initTextWaiter(fetcher, R.string.pref_puzzle_preview_title).beginWait();
    }

    public View clickPuzzlePreview() throws TimeoutException {
        solo.scrollToTop();

        SimpleWaiter rootChangeWaiter = WaiterBuilder.initRootChangeWaiter(fetcher);
        SimpleWaiter aWaiter = WaiterBuilder.initActivityWaiter(PuzzleActivityPreview.class, inst);
        clickOnText(R.string.pref_puzzle_preview_title);
        rootChangeWaiter.beginWait();
        aWaiter.beginWait();

        PuzzleView view = (PuzzleView) fetcher.findViewByClass(PuzzleView.class);
        view.setRunningMode(false);
        inst.waitForIdleSync();
        return view;
    }

    public void setSeekBarValue(int value, int index) throws TimeoutException {
        Set<View> list = new LinkedHashSet<View>();
        fetcher.findAllViewsByClass(list, SeekBar.class);

        SeekBar bar = null;

        int count = 0;
        Iterator<View> it = list.iterator();

        while (it.hasNext()) {
            View view = it.next();
            if (count == index) {
                bar = (SeekBar) view;
                break;
            }
            count++;
        }

        if (bar == null) {
            throw new IllegalArgumentException("SeekBar not found.");
        }

        if (SEEK_BAR_MAX_VALUE == value) {
            bar.setProgress(bar.getMax());
        } else {
            bar.setProgress(value);
        }
    }

    public void clickRescueButton() throws TimeoutException {
        SimpleWaiter waiter = WaiterBuilder.initRootChangeWaiter(fetcher);

        View view = fetcher.findViewById(R.id.puzzle_rescue_button);
        clickOnView(view);

        waiter.beginWait();
    }

    public void clickDialogText(String text) throws TimeoutException {
        SimpleWaiter waiter = WaiterBuilder.initRootChangeWaiter(fetcher);
        View view = fetcher.findViewByText(text);

        clickOnView(view);
        waiter.beginWait();
    }

    public boolean isCardPuzzlePokerMode() {
        CheckBox pokerCheckbox = solo.getView(CheckBox.class, 0);
        return pokerCheckbox.isChecked();
    }

    public void setCardPuzzlePokerMode(boolean b) throws TimeoutException {
        CheckBox pokerCheckbox = (CheckBox) fetcher.findViewByClass(CheckBox.class);
        clickOnView(pokerCheckbox);
        SimpleWaiter waiter = WaiterBuilder.initConditionWaiter(new Waiter() {

            @Override
            public boolean satisfied() throws TimeoutException {
                return ((CheckBox) fetcher.findViewByClass(CheckBox.class)).isChecked();
            }
        });
        waiter.beginWait();
    }

    public void selectDays(int... days) throws TimeoutException {
        ListView listView = (ListView) fetcher.findViewByClass(ListView.class);

        int result = 0;
        for (int day : days) {
            result |= day;
        }
        listView.setTag(result);
    }

    public void clickOnScreen(float x, float y) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();

        MotionEvent event = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, x, y, 0);
        MotionEvent event2 = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_UP, x, y, 0);

        try {
            inst.sendPointerSync(event);
            inst.sendPointerSync(event2);
        } catch (SecurityException e) {
            Assert.assertTrue("Click can not be completed!", false);
        }
        event.recycle();
        event2.recycle();
    }

    public void clickOnText(int resId) throws TimeoutException {
        View view = fetcher.findViewByText(resId);
        clickOnView(view);
    }

    public void clickOnView(final View view) throws TimeoutException {
        if (view == null) {
            throw new IllegalArgumentException("View is null.");
        }
        final int[] xy = new int[2];
        view.getLocationOnScreen(xy);

        if (xy[1] == 0) {
            Rect r = new Rect();
            solo.getCurrentActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
            xy[1] = r.top;
        }
        // Add one pixel to each side to make sure that we defiantly in the
        // bounds
        clickOnScreen(xy[0] + 1, xy[1] + 1);
    }

    public void openDialogPref(int resId) throws TimeoutException {
        View view = fetcher.findViewByText(resId);
        SimpleWaiter waiter = WaiterBuilder.initRootChangeWaiter(fetcher);
        clickOnView(view);
        waiter.beginWait();
    }

    public void closeDialogText(int resId) throws TimeoutException {
        SimpleWaiter waiter = WaiterBuilder.initRootChangeWaiter(fetcher);

        View view = fetcher.findViewByText(resId);
        clickOnView(view);

        waiter.beginWait();
    }

    public void goBackSimpleWait(Class<? extends Activity> cls, boolean closed)
            throws TimeoutException {
        SimpleWaiter rootWaiter = WaiterBuilder.initRootChangeWaiter(fetcher);
        SimpleWaiter aWaiter = WaiterBuilder.initActivityWaiter(cls, inst);

        solo.goBack();
        aWaiter.beginWait();

        if (closed) {
            rootWaiter.beginWait();
        }
    }

    public void goBackSimpleWait(Class<? extends Activity> cls, int resId, boolean closed)
            throws TimeoutException {
        SimpleWaiter rootWaiter = WaiterBuilder.initRootChangeWaiter(fetcher);
        SimpleWaiter aWaiter = WaiterBuilder.initActivityWaiter(cls, inst);
        solo.goBack();
        WaiterBuilder.initTextWaiter(fetcher, resId);
        closeDialogText(resId);
        aWaiter.beginWait();

        if (closed) {
            rootWaiter.beginWait();
        }
    }
    
}
