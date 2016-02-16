
package com.grt_team.wakeup.test.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.util.Log;
import android.view.View;

import com.jayway.android.robotium.solo.Condition;
import com.jayway.android.robotium.solo.Solo;

public class DialogWaiter {
    private static final String TAG = "DialogWaiter";
    public static final int DIALOG_TIMEOUT = 2500;

    private int currentValue;
    private Solo solo;

    public DialogWaiter(Solo solo) {
        this.solo = solo;
    }

    public void prepare() {
        currentValue = getCurrentValue();
    }

    public void waitForDialogClose() {
        solo.waitForCondition(new Condition() {

            @Override
            public boolean isSatisfied() {
                return (currentValue > getCurrentValue());
            }
        }, DIALOG_TIMEOUT);
    }

    public void waitForDialogOpen() {
        solo.waitForCondition(new Condition() {

            @Override
            public boolean isSatisfied() {
                return (currentValue < getCurrentValue());
            }
        }, DIALOG_TIMEOUT);
    }

    private int getCurrentValue() {
        int count = 0;
        Field field;
        try {
            field = solo.getClass().getDeclaredField("viewFetcher");
            field.setAccessible(true);
            Object viwerFetcher = field.get(solo);

            Method decorViews = viwerFetcher.getClass().getDeclaredMethod("getWindowDecorViews");
            Object views = decorViews.invoke(viwerFetcher);
            count = ((View[]) views).length;
        } catch (Exception e) {
            Log.e(TAG, "Problem with retrieving number of root views.");
        }

        return count;
    }

}
