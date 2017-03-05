package com.capstone.petros.cmsc436msdetector;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class ReactionView extends View {
    Paint bubblePaint = new Paint();
    float x = -1;
    float y = -1;


    double totalScore = 0;
    double leftHandScore = -1, rightHandScore = -1;
    boolean testActive = false, first = true;

    public ReactionView(Context context) {
        super(context);
        init(null, 0);
    }

    public ReactionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ReactionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

    }

    public void toggleTestActive(boolean active){
        testActive = active;
    }

    // Call when resetting the test.
    public void resetTest() {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (x == -1) {
            x = getWidth()/2;
            y = getHeight()/2;
        }

        bubblePaint.setColor(Color.RED);
        canvas.drawCircle(x, y, getWidth()/18, bubblePaint);
    }
}
