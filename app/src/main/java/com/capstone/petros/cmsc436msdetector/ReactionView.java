package com.capstone.petros.cmsc436msdetector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class ReactionView extends View {

    private static final String REACTION_TIME_FILE_NAME = "reaction_test_time";

    Random generator = new Random();
    Paint bubblePaint = new Paint();
    boolean destroy = false, redrawn = true, testOver = false;
    int count = 10;
    ArrayList<Long> reactTime = new ArrayList<Long>();
    long startTime = -1;
    long endTime;
    float x = -1;
    float y = -1;
    int adjustWidth;
    int adjustHeight;

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
        // Nothing to init.
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        bubblePaint.setColor(Color.RED);
        adjustWidth = getWidth() - getWidth()/10;
        adjustHeight = getHeight() - getHeight()/10;
        if (x == -1) {
            x = getWidth()/2;
            y = getHeight()/2;
        }

        if (destroy) {
            endTime = System.currentTimeMillis();
            if(startTime != -1){
                reactTime.add(endTime-startTime);
            }
            else{
                // First click, so get rid of the text and help button.
                ((ReactionActivity)getContext()).findViewById(R.id.reactionTextLayout).setVisibility(View.GONE);
                ((ReactionActivity)getContext()).findViewById(R.id.reactionTutorialButton).setVisibility(View.GONE);
            }
            startTime = System.currentTimeMillis();
            float oldX = x;
            float oldY = y;
            while ((oldX-getWidth()/12 <= x && x <= oldX+getWidth()/12) &&
                    (oldY-getWidth()/12 <=y && y <= oldY+getWidth()/12)) {
                x = generator.nextInt(adjustWidth - getWidth()/10) + getWidth()/10;
                y = generator.nextInt(adjustHeight - getHeight()/10) + getHeight()/10;
            }
        }
        else if(testOver){
            x = getWidth()/2;
            y = getHeight()/2;
        }
        canvas.drawCircle(x, y, getWidth()/12, bubblePaint);
        redrawn = true;
        destroy = false;
    }

    private boolean isInCircle(float x, float y, float circleX, float circleY, float radius) {
        double dx = Math.pow(x - circleX, 2);
        double dy = Math.pow(y - circleY, 2);

        if ((dx + dy) < Math.pow(radius, 2)) {
            return true;
        } else {
            return false;
        }
    }

    //Call when the test is done.
    public void finishTest(){
        long sum = 0;
        for (long i : reactTime) {
            sum += i;
        }
        double average = (sum/reactTime.size()/1000.0);
        // Display the results on the screen.

        TextView topText = (TextView) ((ReactionActivity)getContext()).findViewById(R.id.reactionTopText);
        TextView bottomText = (TextView) ((ReactionActivity)getContext()).findViewById(R.id.reactionBottomText);
        DecimalFormat df = new DecimalFormat("#.###");
        topText.setText("TEST COMPLETE.\n\nYour average reaction time was: "+df.format(average));
        bottomText.setText("Click the red dot to exit the test.");
        ((ReactionActivity)getContext()).findViewById(R.id.reactionTextLayout).setVisibility(View.VISIBLE);

        // Also, save the results.
        Utils.appendResultsToInternalStorage(getContext(), REACTION_TIME_FILE_NAME, average);

        testOver = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (redrawn && isInCircle(event.getX(), event.getY(), x, y, getWidth()/12)) {
                    if(testOver){
                        // They want to leave the activity.
                        ((ReactionActivity)getContext()).finish();
                    }
                    else if (count == 0) {
                        // Finish Test
                        finishTest();
                    }
                    else {
                        destroy = true;
                        redrawn = false;
                        //we only do 10 times.
                        count--;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE: case MotionEvent.ACTION_UP:
                break;
        }
        invalidate();
        return true;
    }
}
