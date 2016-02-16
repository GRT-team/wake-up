
package com.grt_team.wakeup.utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

public class ToastHelper {

    private static Toast mToast;

    private ToastHelper() {
    }

    public static void showToast(Context context, String msg, int duration) {
        if (null == mToast || mToast.getView().getWindowVisibility() != View.VISIBLE) {
            if (mToast != null){
                mToast.cancel();
            }
            mToast = Toast.makeText(context, msg, duration);
        } else {
            mToast.cancel();
            mToast.setText(msg);
        }
        mToast.show();
    }

}
