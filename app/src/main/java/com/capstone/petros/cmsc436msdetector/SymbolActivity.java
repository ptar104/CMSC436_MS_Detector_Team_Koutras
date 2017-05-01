package com.capstone.petros.cmsc436msdetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SymbolActivity extends Activity {

    private Activity act = this;
    private CountDownTimer testTimer;
    SpeechRecognizer sr;
    List<Integer> imgList = Arrays.asList(R.drawable.symbol1, R.drawable.symbol2, R.drawable.symbol3, R.drawable.symbol4,
            R.drawable.symbol5, R.drawable.symbol6, R.drawable.symbol7, R.drawable.symbol8, R.drawable.symbol9);
    List<ImageView> symbolList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symbol);

        // each test lasts 90 seconds
        testTimer = new CountDownTimer(90000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                bar.setProgress((int) millisUntilFinished);
            }

            @Override
            public void onFinish() {
                Toast toast = Toast.makeText(getApplicationContext(), "Test done!", Toast.LENGTH_SHORT);
                toast.show();

                // set progress bar to zero
                ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                bar.setProgress(0);
            }
        };

        symbolList = Arrays.asList((ImageView) findViewById(R.id.img1), (ImageView) findViewById(R.id.img2), (ImageView) findViewById(R.id.img3)
                , (ImageView) findViewById(R.id.img4), (ImageView) findViewById(R.id.img5), (ImageView) findViewById(R.id.img6), (ImageView) findViewById(R.id.img7)
                , (ImageView) findViewById(R.id.img8), (ImageView) findViewById(R.id.img9));
        reorderSymbols();
        if(SpeechRecognizer.isRecognitionAvailable(this)) {
            sr = SpeechRecognizer.createSpeechRecognizer(this);
            RecognitionListener rl = new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle bundle) {
                    // Show speech is ready...
                    ((TextView)findViewById(R.id.symbolTextView)).setText("Listening...");
                }

                @Override
                public void onBeginningOfSpeech() {
                    // Show that we have started getting sounds?
                }

                @Override
                public void onRmsChanged(float v) {
                    //k.
                }

                @Override
                public void onBufferReceived(byte[] bytes) {
                    // Getting sound. Can cue user, but no real reason too.
                }

                @Override
                public void onEndOfSpeech() {
                    // Want to wait for results.
                    // Perhaps show that we're getting the results...
                }

                @Override
                public void onError(int i) {
                    if(i == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS){
                        Toast.makeText(act, "Insufficient permissions to use speech recognizer",
                                Toast.LENGTH_LONG);
                    }
                    else if (i == SpeechRecognizer.ERROR_NO_MATCH ||
                            i == SpeechRecognizer.ERROR_SPEECH_TIMEOUT){
                        // Prompt the patient to try again
                    }
                    else {
                        // Some other error has happened
                        Toast.makeText(act, "Speech recognition error", Toast.LENGTH_LONG);
                    }
                }

                @Override
                public void onResults(Bundle bundle) {
                    System.out.println("Results recieved!");
                    ArrayList<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    System.out.println("They are:\n"+results.toString());
                }

                @Override
                public void onPartialResults(Bundle bundle) {
                    // Want to wait for full results.
                }

                @Override
                public void onEvent(int i, Bundle bundle) {
                    // k.
                }
            };
            sr.setRecognitionListener(rl);
        } else {
            Toast.makeText(this, "No Speech recongition available", Toast.LENGTH_LONG);
        }
    }

    public void startSymbolTest(View view) {
        Intent i = new Intent();
        sr.startListening(i);

        // start the timer
        testTimer.start();
    }

    protected void onStop(){
        super.onStop();
        sr.stopListening();
    }

    public void reorderSymbols() {
        Collections.shuffle(imgList);
        for (int i = 0; i < 9; i++) {
            symbolList.get(i).setImageResource(imgList.get(i));
        }
    }
}
