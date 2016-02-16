
package com.grt_team.wakeup.entity.puzzle;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.grt_team.wakeup.R;
import com.grt_team.wakeup.entity.puzzle.PuzzleView.OnUserActionPerformedListener;
import com.grt_team.wakeup.services.AudioService;
import com.grt_team.wakeup.utils.AlarmHelper;

public abstract class Puzzle implements OnUserActionPerformedListener {

    private static final int VOLUME_UP_MSG = 0;

    private static final int delay = 10 * 1000; // 10 seconds

    private PuzzleResultCallback callback;
    private Context context;

    private static boolean isSilentMode = false;

    private static boolean isPreview = true;

    private boolean isPuzzleRunning = true;

    private boolean isVibrating;

    /**
     * ActivityInfo constants to determine what screen orientation should be
     * used
     */
    private int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    private Handler.Callback hCallback = new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            volumeUp();
            return true;
        }
    };

    private static Handler handler;
    
    public void initDefaults(Context context){
        this.context = context;
    }

    public interface PuzzleResultCallback {
        public void onFinish();
    }

    public void setCallback(PuzzleResultCallback callback) {
        this.callback = callback;
    }

    public View runPuzzle(DisplayMetrics metrics) {
        this.isVibrating = AudioService.isVibrating();
        if (handler == null) {
            handler = new Handler(hCallback);
        }
        volumeUp();
        View puzzleView = onPuzzleRun(metrics);
        puzzleView.findViewById(R.id.puzzle_rescue_button).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        FragmentManager fm = ((FragmentActivity) getContext())
                                .getSupportFragmentManager();
                        RescueDialog dialog = RescueDialog.newInstance(getPuzzleResTitle(),
                                getPuzzleResBigDescription(), Puzzle.this);
                        dialog.show(fm, RescueDialog.FRAGMENT_TAG);
                    }
                });
        return puzzleView;
    }

    public void endPuzzle() {
        handler.removeMessages(VOLUME_UP_MSG);
        volumeUp();
        isPuzzleRunning = false;
        callback.onFinish();
    }

    public Context getContext() {
        return context;
    }

    public int getScreenOrientation() {
        return screenOrientation;
    }

    public void setScreenOrientation(int screenOrientation) {
        this.screenOrientation = screenOrientation;
    }

    public void volumeUp() {
        if (isPreview || !isPuzzleRunning || !AudioService.isBusy()) {
            return;
        }
        isSilentMode = false;
        AlarmHelper.changeSoundVolume(context, AudioService.MAX_VOLUME, isVibrating);
    }

    public void volumeDown() {
        if (isPreview || !isPuzzleRunning) {
            return;
        }
        if (!isSilentMode) {
            isSilentMode = true;
            AlarmHelper.changeSoundVolume(context, AudioService.MIN_VOLUME, false);
        }
        handler.removeMessages(VOLUME_UP_MSG);
        handler.sendEmptyMessageDelayed(VOLUME_UP_MSG, delay);
    }

    @Override
    public void onActionPerformed() {
        volumeDown();
    }

    /**
     * Return view that represent rescue button. When user click on it
     * 
     * @return
     */
    protected View getRescueButton() {
        return LayoutInflater.from(getContext()).inflate(R.layout.puzzle_rescure_button, null);
    }

    protected View inflateRescueButton(View parent) {
        RelativeLayout rel = new RelativeLayout(getContext());
        rel.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        rel.addView(parent);

        int buttonSize = getContext().getResources().getDimensionPixelSize(
                R.dimen.puzzle_title_height);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT | RelativeLayout.ALIGN_PARENT_TOP);
        params.width = buttonSize;
        params.height = buttonSize;
        View view = getRescueButton();
        view.setLayoutParams(params);

        rel.addView(view);
        return rel;
    }

    public abstract View onPuzzleRun(DisplayMetrics metrics);

    public abstract void onSave(Bundle savedInstanceState);

    public abstract void onRestore(Bundle savedInstanceState);

    public abstract int getPuzzleResTitle();

    public abstract int getPuzzleResBigDescription();

    public void setPreview(boolean isPreview) {
        Puzzle.isPreview = isPreview;
    }

    public View getPuzzleHelpDescriptionView(Context context, int resId) {
        return null;
    }

    public ImageGetter getPuzzleDescriptionGetter(Context context) {
        return new SimpleResourceGetter(context);
    }

    private CharSequence getHtmlDescription(int resId) {
        String text = context.getResources().getString(resId);
        return Html.fromHtml(text, getPuzzleDescriptionGetter(context), null);
    }

    public static class RescueDialog extends DialogFragment {
        public static final String FRAGMENT_TAG = "fragment_edit_name";

        private static final String TITLE_RES_ID = "title";
        private static final String TITLE_MSG_ID = "msg";

        private Puzzle puzzle;

        public static RescueDialog newInstance(int title, int message, Puzzle puzzle) {
            RescueDialog frag = new RescueDialog();
            frag.setPuzzle(puzzle);

            Bundle args = new Bundle();
            args.putInt(TITLE_RES_ID, title);
            args.putInt(TITLE_MSG_ID, message);
            frag.setArguments(args);
            return frag;
        }

        public void setPuzzle(Puzzle puzzle) {
            this.puzzle = puzzle;
        }

        @SuppressLint("NewApi")
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt(TITLE_RES_ID);
            int message = getArguments().getInt(TITLE_MSG_ID);

            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                builder = new AlertDialog.Builder(getActivity(), R.style.Dialog);
            } else {
                builder = new AlertDialog.Builder(getActivity());
            }

            View view = puzzle.getPuzzleHelpDescriptionView(getActivity(), message);
            if (view != null) {
                builder.setView(view);
            } else {
                builder.setMessage(puzzle.getHtmlDescription(message));
            }

            return builder.setIcon(R.drawable.ic_launcher)
                    .setInverseBackgroundForced(true)
                    .setTitle(title)
                    .setPositiveButton(R.string.rescue_dialog_off_btn,
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int whichButton) {
                                     PuzzleActivity puzzleActivity = (PuzzleActivity) getActivity();

                                     Intent intent = new Intent(puzzleActivity.getIntent());
                                     intent.setClass(puzzleActivity, PuzzleRescueActivity.class);
                                     Bundle puzzleData = new Bundle();
                                     puzzleActivity.savePuzzleToBundle(puzzleData);
                                     intent.putExtra(PuzzleActivity.EXTRA_RESTORE_PUZZLE_DATA,
                                             puzzleData);

                                     startActivity(intent);
                                     puzzleActivity.finish();
                                 }
                             }
                     )
                    .setNegativeButton(R.string.rescue_dialog_cancel_btn, null)
                    .create();
        }
    }

    public static class SimpleResourceGetter implements ImageGetter {
        private Context context;

        public SimpleResourceGetter(Context context) {
            this.context = context.getApplicationContext();
        }

        public Context getContext() {
            return context;
        }

        public int getResourceId(String name) {
            return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        }

        @Override
        public Drawable getDrawable(String source) {
            int resId = getResourceId(source);
            if (resId != 0) {
                Drawable drawable = context.getResources().getDrawable(resId);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight());
                return drawable;
            }
            return null;
        }
    }
}
