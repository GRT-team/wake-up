
package com.grt_team.wakeup.test.util;

import java.util.concurrent.TimeoutException;

public class SimpleWaiter {
    public static final int TIMEOUT = 10000;
    public static final int STEP = 100;

    private Waiter waiter;

    public interface Waiter {
        public boolean satisfied() throws TimeoutException;
    }

    SimpleWaiter(Waiter waiter) {
        this.waiter = waiter;
    }

    public void beginWait() throws TimeoutException {
        int time = TIMEOUT;

        while (time >= 0) {
            if (waiter.satisfied()) {
                return;
            }
            try {
                Thread.sleep(STEP);
            } catch (InterruptedException e) {
            }
            time -= STEP;
        }
        throw new TimeoutException();
    }

}
