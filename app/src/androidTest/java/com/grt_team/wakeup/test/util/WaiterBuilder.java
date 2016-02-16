
package com.grt_team.wakeup.test.util;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.view.View;

import com.grt_team.wakeup.test.util.SimpleWaiter.Waiter;

public class WaiterBuilder {
    private WaiterBuilder() {

    }

    public static SimpleWaiter initRootChangeWaiter(final SoloFetcher fetcher)
            throws TimeoutException {
        final WeakReference<View> currentRoot = new WeakReference<View>(
                fetcher.getCurrentRootView());

        Waiter waiter = new Waiter() {

            @Override
            public boolean satisfied() throws TimeoutException {
                return fetcher.getCurrentRootView() != currentRoot.get();
            }
        };

        return new SimpleWaiter(waiter);
    }

    public static SimpleWaiter initRootCountWaiter(final SoloFetcher fetcher, final int count) {
        Waiter waiter = new Waiter() {

            @Override
            public boolean satisfied() throws TimeoutException {
                return fetcher.getRootCount() == count;
            }
        };

        return new SimpleWaiter(waiter);
    }

    public static SimpleWaiter initActivityWaiter(Class<? extends Activity> cls,
            final Instrumentation inst) {
        final ActivityMonitor am = inst.addMonitor(cls.getName(), null, false);
        Waiter waiter = new Waiter() {

            @Override
            public boolean satisfied() throws TimeoutException {
                inst.waitForMonitorWithTimeout(am, SimpleWaiter.TIMEOUT / 1000);
                return true;
            }
        };

        return new SimpleWaiter(waiter);
    }

    public static SimpleWaiter initClassCountWaiter(final SoloFetcher fetcher,
            final Class<?> classFilter, final int count) {
        final Set<View> views = new HashSet<View>();
        Waiter waiter = new Waiter() {

            @Override
            public boolean satisfied() throws TimeoutException {
                views.clear();
                fetcher.findAllViewsByClass(views, classFilter);
                if (count == views.size()) {
                    return true;
                }
                return false;
            }
        };

        return new SimpleWaiter(waiter);
    }

    public static SimpleWaiter initTextWaiter(final SoloFetcher fetcher, final int resId) {

        Waiter waiter = new Waiter() {

            @Override
            public boolean satisfied() throws TimeoutException {
                View view = fetcher.findViewByText(resId);
                if (view != null) {
                    return true;
                }
                return false;
            }
        };

        return new SimpleWaiter(waiter);
    }

    public static SimpleWaiter initConditionWaiter(Waiter waiter) {
        return new SimpleWaiter(waiter);
    }

}
