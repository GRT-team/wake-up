
package com.grt_team.wakeup.test.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.Instrumentation;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jayway.android.robotium.solo.Solo;

public class SoloFetcher {
    private static final String TAG = "SoloFetcher";

    Solo solo;
    Instrumentation inst;

    public SoloFetcher(Solo solo, Instrumentation inst) {
        this.solo = solo;
        this.inst = inst;
    }

    public int getRootCount() {
        return getRootViews().length;
    }

    public View[] getRootViews() {
        inst.waitForIdleSync();
        View[] views = null;
        Field field;
        try {
            field = solo.getClass().getDeclaredField("viewFetcher");
            field.setAccessible(true);
            Object viwerFetcher = field.get(solo);

            Method decorViews = viwerFetcher.getClass().getDeclaredMethod("getWindowDecorViews");
            views = (View[]) decorViews.invoke(viwerFetcher);
        } catch (Exception e) {
            Log.e(TAG, "Problem with retrieving number of root views.");
        }

        return views;
    }

    public View getCurrentRootView() throws TimeoutException {
        View viewResult = null;
        inst.waitForIdleSync();
        View[] views = getRootViews();
        for (View view : views) {
            if (view.hasWindowFocus()) {
                viewResult = view;
                break;
            }
        }
        return viewResult;
    }

    public View getCurrentRootViewSafe() throws TimeoutException {
        View view = getCurrentRootView();
        if (view == null) {
            view = solo.getCurrentActivity().getWindow().getDecorView();
        }
        return view;
    }

    public void findAllViewsByClass(Set<View> list, Class<?> classFilter)
            throws TimeoutException {
        View view = getCurrentRootView();
        findAllViewByClass(list, view, classFilter);
    }

    private void findAllViewByClass(Set<View> list, View parent, Class<?> classFilter) {
        if (classFilter.isAssignableFrom(parent.getClass())) {
            list.add(parent);
        } else if (parent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View v = viewGroup.getChildAt(i);
                if (!v.isShown()) {
                    continue;
                }
                findAllViewByClass(list, v, classFilter);
            }
        }
    }

    private View findViewByClass(View parent, Class<?> classFilter) {
        if (classFilter.isAssignableFrom(parent.getClass())) {
            return parent;
        } else if (parent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View v = viewGroup.getChildAt(i);
                if (!v.isShown()) {
                    continue;
                }
                v = findViewByClass(v, classFilter);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

    private View findViewByClassName(View parent, String className) {
        if (className.equals(parent.getClass().getSimpleName())) {
            return parent;
        } else if (parent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View v = viewGroup.getChildAt(i);
                if (!v.isShown()) {
                    continue;
                }
                v = findViewByClassName(v, className);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

    private View findViewByText(View view, String text) throws TimeoutException {
        if (view instanceof TextView) {

            if (text.equals(((TextView) view).getText().toString())) {
                return view;
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View v = viewGroup.getChildAt(i);
                if (!v.isShown()) {
                    continue;
                }
                v = findViewByText(v, text);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

    private View findViewById(View view, int id) {
        if (id == view.getId()) {
            return view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View v = viewGroup.getChildAt(i);
                if (!v.isShown()) {
                    continue;
                }
                v = findViewById(v, id);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

    public View findViewById(int id) throws TimeoutException {
        View view = getCurrentRootView();
        return findViewById(view, id);
    }

    public View findViewByClass(Class<?> classFilter) throws TimeoutException {
        View view = getCurrentRootView();
        return findViewByClass(view, classFilter);
    }

    public View findViewByText(String text) throws TimeoutException {
        View view = getCurrentRootView();
        return findViewByText(view, text);
    }

    public View findViewByText(int resId) throws TimeoutException {
        String text = solo.getCurrentActivity().getResources().getString(resId);
        return findViewByText(text);
    }

    public View findViewByClassName(String className) throws TimeoutException {
        View view = getCurrentRootView();
        return findViewByClassName(view, className);
    }

    public Activity getCurrentActivity() {
        inst.waitForIdleSync();
        return solo.getCurrentActivity();
    }
}
