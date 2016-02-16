
package com.grt_team.wakeup.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.CompoundButton;

import com.grt_team.wakeup.R;

/**
 * Custom switch button to support all android APIs. This switch goes with
 * inverse thumb padding.
 */
public class SwitchButton extends CompoundButton {

    private Drawable thumbDrawableOn;
    private Drawable thumbDrawableOff;
    private int thumbTextPadding;
    private Drawable backgound;
    private Rect thumbRect = new Rect();

    private Layout offLayout;
    private Layout onLayout;
    private String offText;
    private String onText;

    private TextPaint textPaint;
    private Paint paint;

    private boolean isMoving;
    private boolean startDragging;
    private int touchSlop;

    @SuppressLint("Recycle")
    // On KitKat recycling give exception.
    private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
    private int minFlingVelocity;

    // TODO: add support though xml attributes to select thumb padding direction
    // (inverse or normal)
    public SwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO: rework class to allow element to be not clickable
        setClickable(true);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        Resources res = getResources();
        textPaint.density = res.getDisplayMetrics().density;
        textPaint.setTextSize(getTextSize());
        ColorStateList textColors = getTextColors();
        if (textColors != null) {
            textPaint.setColor(textColors.getColorForState(getDrawableState(),
                    textColors.getDefaultColor()));
        }

        // TODO: merge on/off drawables to one and provide support though
        // drawable state. Allow all parametes to be changed though xml
        // attributes.
        thumbDrawableOn = getResources().getDrawable(R.drawable.thumb_on);
        thumbDrawableOff = getResources().getDrawable(R.drawable.thumb_off);
        backgound = getResources().getDrawable(R.drawable.thumb_background);
        backgound.setBounds(0, 0, 0, 0);

        onText = getResources().getString(R.string.switch_button_on);
        offText = getResources().getString(R.string.switch_button_off);

        thumbTextPadding = getResources().getDimensionPixelSize(
                R.dimen.switch_btn_thumb_text_padding);

        paint = new Paint();
        paint.setColor(Color.RED);

        ViewConfiguration config = ViewConfiguration.get(context);
        touchSlop = config.getScaledTouchSlop();
        minFlingVelocity = config.getScaledMinimumFlingVelocity();
    }

    private Layout makeLayout(CharSequence text) {
        return new StaticLayout(text, textPaint, (int) Math.ceil(Layout.getDesiredWidth(text,
                textPaint)), Layout.Alignment.ALIGN_NORMAL, 1.f, 0, true);
    }

    private Rect drawablePadding = new Rect();
    private Rect rectWithPadding = new Rect();

    private void updateInversePadding(Rect rect, Rect padding) {
        rect.left -= padding.left;
        rect.top -= padding.top;
        rect.right += padding.right;
        rect.bottom += padding.bottom;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        placeThumb();
        updateDrawableSize();
    }

    public static int resolveSizeAndStateCopy(int size, int measureSpec, int childMeasuredState) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
                if (specSize < size) {
                    result = specSize | MEASURED_STATE_TOO_SMALL;
                } else {
                    result = size;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result | (childMeasuredState & MEASURED_STATE_MASK);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (onLayout == null) {
            onLayout = makeLayout(onText);
        }
        if (offLayout == null) {
            offLayout = makeLayout(offText);
        }
        //
        thumbDrawableOn.getPadding(drawablePadding);
        final int maxTextWidth = Math.max(onLayout.getWidth(), offLayout.getWidth());
        final int maxTextHeight = onLayout.getHeight();

        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + maxTextWidth * 2 + thumbTextPadding * 4
                + drawablePadding.left + drawablePadding.right;
        minw = Math.max(minw, getResources().getDimensionPixelSize(R.dimen.switch_btn_min_width));
        int minh = getPaddingTop() + getPaddingBottom() + maxTextHeight + thumbTextPadding * 2;

        int w = SwitchButton.resolveSizeAndStateCopy(minw, widthMeasureSpec, 1);
        int h = SwitchButton.resolveSizeAndStateCopy(minh, heightMeasureSpec, 1);

        setMeasuredDimension(w, h);
        placeThumb();
        updateDrawableSize();
    }

    /**
     * Update background, thumb on and thumb off drawables size according to
     * SwitchButton size and drawable paddings.
     */
    private void updateDrawableSize() {
        backgound.getPadding(drawablePadding);
        rectWithPadding.set(0, 0, getWidth(), getHeight());
        updateInversePadding(rectWithPadding, drawablePadding);
        backgound.setBounds(rectWithPadding);

        thumbDrawableOff.getPadding(drawablePadding);
        rectWithPadding.set(0, 0, thumbRect.width(), thumbRect.height());
        updateInversePadding(rectWithPadding, drawablePadding);
        thumbDrawableOff.setBounds(rectWithPadding);

        thumbDrawableOn.getPadding(drawablePadding);
        rectWithPadding.set(0, 0, thumbRect.width(), thumbRect.height());
        updateInversePadding(rectWithPadding, drawablePadding);
        thumbDrawableOn.setBounds(rectWithPadding);
    }

    /**
     * Update thumb rect according to its state. If not checked then thumb will
     * be placed at the left side, otherwise right.
     */
    private void placeThumb() {
        if (isChecked()) {
            thumbRect.set(0, 0, getWidth() / 2, getHeight());
            thumbRect.offsetTo(getWidth() / 2, 0);
        } else {
            thumbRect.set(0, 0, getWidth() / 2, getHeight());
        }
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        if (thumbRect != null) {
            placeThumb();
        }
        invalidate();
    }

    private int xOffset;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mVelocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (thumbRect.contains((int) event.getX(), (int) event.getY())) {
                    xOffset = thumbRect.left - (int) event.getX();
                    isMoving = true;
                    startDragging = false;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isMoving) {
                    int newX = xOffset + (int) event.getX();
                    getParent().requestDisallowInterceptTouchEvent(true);

                    if (startDragging || Math.abs(newX - thumbRect.left) > touchSlop) {
                        startDragging = true;
                        if (newX + thumbRect.width() > getWidth()) {
                            newX = getWidth() - thumbRect.width();
                        } else if (newX < 0) {
                            newX = 0;
                        }
                        thumbRect.offsetTo(newX, 0);

                        invalidate();
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isMoving) {
                    isMoving = false;
                    mVelocityTracker.computeCurrentVelocity(1000);
                    float xvel = mVelocityTracker.getXVelocity();
                    float absYvel = Math.abs(mVelocityTracker.getYVelocity());

                    if ((!startDragging)
                            && (Math.abs(xvel) < minFlingVelocity)
                            && (absYvel < minFlingVelocity)) {
                        // inverse checked status if there is no thumb dragging
                        // and no fling, but was clicked in the thumb.
                        setChecked(!isChecked());
                    } else {
                        if ((isChecked() == isCurrentStateOn())
                                && (Math.abs(xvel) > minFlingVelocity)
                                && (Math.abs(xvel) > absYvel)) {
                            setChecked(xvel > 0);
                        } else if (isCurrentStateOn()) {
                            // update checked status according thumb position
                            setChecked(true);
                        } else {
                            // update checked status according thumb position
                            setChecked(false);
                        }
                    }
                    mVelocityTracker.clear();
                    return false;
                }
            default:

        }
        return super.onTouchEvent(event);
    }

    /**
     * Indicate switch button status according to the thumb position.
     * 
     * @return - true if checked, otherwise false
     */
    private boolean isCurrentStateOn() {
        int delta = thumbRect.width() / 2;
        return thumbRect.left > delta;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        backgound.draw(canvas);

        Drawable drawable = isCurrentStateOn() ? thumbDrawableOn : thumbDrawableOff;
        canvas.save();
        canvas.translate(thumbRect.left, thumbRect.top);
        drawable.draw(canvas);
        canvas.restore();

        Layout layout = (isCurrentStateOn() ? onLayout : offLayout);
        canvas.translate(thumbRect.left
                + (thumbRect.width() - layout.getWidth() - thumbTextPadding * 2) / 2
                + thumbTextPadding,
                (thumbRect.height() - layout.getHeight() - thumbTextPadding * 2) / 2
                        + thumbTextPadding);
        layout.draw(canvas);
    }
}
