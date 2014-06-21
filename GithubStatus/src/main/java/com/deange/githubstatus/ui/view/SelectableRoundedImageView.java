package com.deange.githubstatus.ui.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.makeramen.RoundedImageView;

public class SelectableRoundedImageView extends RoundedImageView {

    private int mSelectedColour;
    private int mNormalColour;
    private final Rect mBounds = new Rect();

    public SelectableRoundedImageView(final Context context) {
        super(context);
        getColours();
    }

    public SelectableRoundedImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        getColours();
    }

    public SelectableRoundedImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        getColours();
    }

    private void getColours() {
        mSelectedColour = getResources().getColor(android.R.color.holo_blue_light);
        mNormalColour = getBorderColor();
    }

    @Override
    public void setBorderColor(final int color) {
        mNormalColour = getBorderColor();
        super.setBorderColor(color);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        final boolean inBounds = isInEllipseBounds(event.getX(), event.getY());

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (inBounds) {
                    super.setBorderColor(mSelectedColour);
                } else {
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (!inBounds) {
                    super.setBorderColor(mNormalColour);
                    return true;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                super.setBorderColor(mNormalColour);
                if (!inBounds) {
                    return true;
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    private boolean isInEllipseBounds(final float x, final float y) {

        final float a = getWidth() / 2;
        final float b = getHeight() / 2;
        final float radius = Math.min(a, b);

        return Math.pow((x - a) / radius, 2) + Math.pow((y - b) / radius, 2) <= 1;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        mBounds.set(0, 0, MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
