package com.capstone.petros.cmsc436msdetector;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by peterkoutras on 2/12/17.
 */

public class SpiralView extends View {

    public SpiralView(Context context) {
        super(context);
        init(null, 0);
    }

    public SpiralView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SpiralView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw spiral/touch lines here.
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return true;
    }

}
