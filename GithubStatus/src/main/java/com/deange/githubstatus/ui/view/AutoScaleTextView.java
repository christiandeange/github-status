package com.deange.githubstatus.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.deange.githubstatus.R;

public class AutoScaleTextView extends FontTextView {

    private float mScaleFactor;
    private float mMinTextSize;
    private float mMaxTextSize;

    private static final float DEFAULT_SCALED_FACTOR = 1f;
    private static final float DEFAULT_MIN_TEXT_SIZE = 10f;
    private static final float DEFAULT_MAX_TEXT_SIZE = 256f;
    private static final int DEFAULT_LINE_COUNT = 1;

    public AutoScaleTextView(final Context context) {
        this(context, null);
    }

    public AutoScaleTextView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoScaleTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoScaleTextView, defStyle, 0);

        mScaleFactor = DEFAULT_SCALED_FACTOR;
        mMinTextSize = DEFAULT_MIN_TEXT_SIZE;
        mMaxTextSize = DEFAULT_MAX_TEXT_SIZE;

        if (a != null) {
            mScaleFactor = a.getFraction(R.styleable.AutoScaleTextView_scaleFactor, 1, 1, DEFAULT_SCALED_FACTOR);
            mMinTextSize = a.getDimension(R.styleable.AutoScaleTextView_minTextSize, DEFAULT_MIN_TEXT_SIZE);
            mMaxTextSize = a.getDimension(R.styleable.AutoScaleTextView_maxTextSize, DEFAULT_MAX_TEXT_SIZE);

            a.recycle();
        }

    }

    private void rescaleText() {

        if (TextUtils.isEmpty(getText())) {
            return;
        }

        float size = mMinTextSize;

        final TextPaint paint = new TextPaint(getPaint());
        paint.setTextSize(size);

        // Use modified gallop search to converge to an appropriate text size
        while (getLineCount(paint) <= DEFAULT_LINE_COUNT) {
            if (size >= mMaxTextSize) break;
            size *= 2;
            paint.setTextSize(size);
        }

        while (getLineCount(paint) > DEFAULT_LINE_COUNT) {
            if (size <= mMinTextSize) break;
            size -= 1;
            paint.setTextSize(size);
        }

        size *= mScaleFactor;

        // Renormalize
        size = Math.min(size, mMaxTextSize);
        size = Math.max(size, mMinTextSize);

        setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    public void setScaleFactor(final float scaleFactor) {
        mScaleFactor = scaleFactor;
    }

    public float getScaleFactor() {
        return mScaleFactor;
    }

    public int getLineCount(final TextPaint paint) {
        final String text = getText().toString();
        final int targetWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        return new StaticLayout(text, paint, targetWidth,
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true).getLineCount();
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom); // Does stuff for scroller and editor
        if (changed) {
            rescaleText();
        }
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        rescaleText();
    }

}