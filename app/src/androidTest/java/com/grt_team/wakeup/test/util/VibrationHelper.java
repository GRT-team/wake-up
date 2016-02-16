
package com.grt_team.wakeup.test.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.Build;
import android.os.Vibrator;

public class VibrationHelper {
    public static boolean hasVibrator(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                Class<? extends Vibrator> vibratorClass = v.getClass();
                Method hasVibrate = vibratorClass.getMethod("hasVibrator");
                if (!(Boolean) hasVibrate.invoke(v)) {
                    return false;
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
