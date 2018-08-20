package com.umberto.medicinetracking.control;

/*
Albin Poignot
Product Owner @ Koolicar ✦ Team energy lover
Jun 12, 2017
Checkable CardView in all Android versions
https://medium.com/@AlbinPoignot/checkable-cardview-in-all-android-versions-7124ca6df1ab
 */
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.Checkable;

import com.umberto.medicinetracking.R;

public class SelectableCardView extends CardView implements Checkable {
    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked,
    };

    private boolean isChecked;

    public SelectableCardView(Context context) {
        super(context);
        init();
    }

    public SelectableCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectableCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setCardBackgroundColor(ContextCompat.getColorStateList(getContext(), R.color.selector_cardview_color));
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @Override
    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        setChecked(!this.isChecked);
    }
}
