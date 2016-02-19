
package com.grt_team.wakeup.test;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.grt_team.wakeup.WakeUpActivity;
import com.grt_team.wakeup.test.util.SoloFetcher;
import com.grt_team.wakeup.utils.SharedPreferenceUtil;
import com.jayway.android.robotium.solo.Solo;

public class FeedbackDialogNotShowTest extends ActivityInstrumentationTestCase2<WakeUpActivity> {
    private Solo solo;
    private SoloFetcher fetcher;

    public FeedbackDialogNotShowTest() {
        super(WakeUpActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        long next = 0;
        SharedPreferenceUtil.saveNextFeedbackTime(getInstrumentation().getTargetContext(), next);
        solo = new Solo(getInstrumentation(), getActivity());

        fetcher = new SoloFetcher(solo, getInstrumentation());
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

    public void testNotShownigDialog() {
        View[] views = fetcher.getRootViews();
        assertTrue(views.length == 1);
    }
}
