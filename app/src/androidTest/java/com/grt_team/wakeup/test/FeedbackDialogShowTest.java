
package com.grt_team.wakeup.test;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.grt_team.wakeup.WakeUpActivity;
import com.grt_team.wakeup.test.util.DialogWaiter;
import com.grt_team.wakeup.test.util.SoloFetcher;
import com.grt_team.wakeup.utils.SharedPreferenceUtil;
import com.jayway.android.robotium.solo.Solo;

public class FeedbackDialogShowTest extends ActivityInstrumentationTestCase2<WakeUpActivity> {
    private Solo solo;
    private SoloFetcher fetcher;

    public FeedbackDialogShowTest() {
        super(WakeUpActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        long next = System.currentTimeMillis() - 259201000;
        SharedPreferenceUtil.saveNextFeedbackTime(getInstrumentation().getTargetContext(), next);
        solo = new Solo(getInstrumentation(), getActivity());

        fetcher = new SoloFetcher(solo, getInstrumentation());
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

    public void testNotShownigDialog1() {
        View[] views = fetcher.getRootViews();
        if (views.length == 1) {
            solo.waitForDialogToOpen(DialogWaiter.DIALOG_TIMEOUT);
        }
        assertTrue(views.length == 2);
    }

}
