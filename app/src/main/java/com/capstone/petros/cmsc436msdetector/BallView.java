package com.capstone.petros.cmsc436msdetector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by MarkCreamer on 2/16/17.
 */

public class BallView extends View {
    Display disp = ((WindowManager)this.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    Paint paint1 = new Paint();
    Paint paint2 = new Paint();
    Paint paint3 = new Paint();
    Paint ball = new Paint();
    private int x = (disp.getWidth() / 2);
    private int y = (disp.getHeight() / 2);

    public BallView(Context context) {
        super(context);
    }

    public BallView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public BallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint1.setColor(Color.WHITE);
        paint2.setColor(Color.parseColor("#3BBBBB"));
        paint3.setColor(Color.WHITE);
        ball.setColor(Color.RED);
        canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/2, paint1);
        canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/3, paint2);
        canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/6, paint3);
        canvas.drawCircle(x, y, 20, ball);

    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

            case MotionEvent.ACTION_MOVE:

            case MotionEvent.ACTION_UP:

                x = (int) event.getX();
                y = (int) event.getY();
                break;
        }
        int width = getWidth()/2;
        int height = getHeight()/2;

        if(x >= width - width && y >= height - width && x <= width + width && y <= height + width){
            invalidate();
        }
        return true;
    }
}
