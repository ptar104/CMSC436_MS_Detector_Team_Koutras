package com.capstone.petros.cmsc436msdetector;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ReactionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reaction);
    }

    public void showTutorial(View v) {
        FrameLayout frame = (FrameLayout)findViewById(R.id.reactionFrame);
        TextView tutorialView = (TextView) findViewById(R.id.reactionInstructions);
        RelativeLayout shader = (RelativeLayout)findViewById(R.id.reactionShader);
        ImageView tutorialButton = (ImageView)findViewById(R.id.reactionTutorialButton);

        if (frame.getVisibility() == View.GONE) {
            frame.setVisibility(View.VISIBLE);
            tutorialView.setText("INSTRUCTIONS:\n\n" +
                    "This is the reaction test.\n\n" +
                    "It measures how quickly you can click a moving red dot.\n\n" +
                    "Each time you click the dot, it will disappear and reappear in a new location.\n\n" +
                    "The ball will move 10 times in total. When it reappears, click it as quick as you can.\n\n" +
                    "To begin the test, simply click the red dot in the center of the screen for the first time.");
            shader.setVisibility(View.VISIBLE);
            tutorialButton.setColorFilter(0xFFF6FF00);
        }
        else {
            frame.setVisibility(View.GONE);
            shader.setVisibility(View.GONE);
            tutorialButton.setColorFilter(0xFF000000);
        }
    }
}
