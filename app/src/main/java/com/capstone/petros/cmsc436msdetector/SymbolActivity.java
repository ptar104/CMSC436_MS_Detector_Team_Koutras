package com.capstone.petros.cmsc436msdetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SymbolActivity extends Activity {
    private Activity act = this;
    private CountDownTimer testTimer;
    SpeechRecognizer sr;
    List<Integer> imgList = Arrays.asList(R.drawable.symbol1, R.drawable.symbol2, R.drawable.symbol3, R.drawable.symbol4,
            R.drawable.symbol5, R.drawable.symbol6, R.drawable.symbol7, R.drawable.symbol8, R.drawable.symbol9);
    List<ImageView> symbolList;

    private int curSymbolAnswer;    // The index of the current symbol
    private int numSymbolsCorrect;  // The number of symbols that the user correctly answered
    private int numGuesses;         // The number of symbol guesses
    private double curSymbolAppearanceTime; // The time that the current symbol appeared at
    // Keeps track of the response time for each symbol
    private HashMap<Integer,ArrayList<Double>> symbolTimes;
    private int imageHeight;

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
                Toast.makeText(getApplicationContext(), "Test done!", Toast.LENGTH_SHORT).show();

                showResults();

                // set progress bar to zero
                ProgressBar bar = (ProgressBar) findViewById(R.id.progress_bar);
                bar.setProgress(0);

                numSymbolsCorrect = 0;
                numGuesses = 0;
                symbolTimes.clear();

                Button startButton = (Button)findViewById(R.id.startTestButton);
                startButton.setEnabled(true);
                enableNumpad(false);
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
                    //((TextView)findViewById(R.id.symbolTextView)).setText("Listening...");
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
            Toast.makeText(this, "No Speech recognition available", Toast.LENGTH_LONG).show();
        }

        numSymbolsCorrect = 0;
        symbolTimes = new HashMap<>();
        for(int symbolIndex = 1; symbolIndex <= 9; symbolIndex++) {
            symbolTimes.put(symbolIndex,new ArrayList<Double>());   // Start off the list for each symbol
        }

        enableNumpad(false);
    }

    public void startSymbolTest(View view) {
        Intent i = new Intent();
        sr.startListening(i);

        enableNumpad(true);

        // Disable the start test button
        Button startButton = (Button)findViewById(R.id.startTestButton);
        startButton.setEnabled(false);

        // start the timer
        testTimer.start();
        changeCurrentSymbol();  // Puts the first current symbol up
    }

    public void changeCurrentSymbol() {
        // Get a random number 1-9
        imageHeight = findViewById(R.id.symbolImageHolder).getHeight();
        ImageView curSymbolImageView = (ImageView) findViewById(R.id.currentSymbol);

        Random r = new Random();
        int rand = r.nextInt(9);
        while(rand+1 == curSymbolAnswer) {    // Make sure we don't get the last one again
            rand = r.nextInt(9);
        }

        curSymbolImageView.setImageResource(imgList.get(rand));
        curSymbolAppearanceTime = System.currentTimeMillis();   // Record the time at which it showed up
        curSymbolAnswer = rand+1;

        curSymbolImageView.getLayoutParams().height = imageHeight;
        curSymbolImageView.requestLayout();
    }

    public void onButtonPress(View view) {
        int guessNum;
        switch(view.getId()) {
            case R.id.button1:
                guessNum = 1;
                break;
            case R.id.button2:
                guessNum = 2;
                break;
            case R.id.button3:
                guessNum = 3;
                break;
            case R.id.button4:
                guessNum = 4;
                break;
            case R.id.button5:
                guessNum = 5;
                break;
            case R.id.button6:
                guessNum = 6;
                break;
            case R.id.button7:
                guessNum = 7;
                break;
            case R.id.button8:
                guessNum = 8;
                break;
            case R.id.button9:
                guessNum = 9;
                break;
            default:    // Shouldn't happen
                return;
        }

        if(guessNum == curSymbolAnswer) {   // They got correct symbol number!
            numSymbolsCorrect++;

            // Record the new response time entry for the symbol
            symbolTimes.get(curSymbolAnswer).add(System.currentTimeMillis()-curSymbolAppearanceTime);
        }
        numGuesses++;

        // Show a new symbol
        changeCurrentSymbol();
    }

    public void enableNumpad(boolean doEnable) {
        findViewById(R.id.button1).setEnabled(doEnable);
        findViewById(R.id.button2).setEnabled(doEnable);
        findViewById(R.id.button3).setEnabled(doEnable);
        findViewById(R.id.button4).setEnabled(doEnable);
        findViewById(R.id.button5).setEnabled(doEnable);
        findViewById(R.id.button6).setEnabled(doEnable);
        findViewById(R.id.button7).setEnabled(doEnable);
        findViewById(R.id.button8).setEnabled(doEnable);
        findViewById(R.id.button9).setEnabled(doEnable);
    }
    @Override
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

    public void showResults() {
        double average = 0;

        int numDecreasingSymbolAverages = 0;
        double frontHalfAverage = 0, backHalfAverage = 0;

        for(ArrayList<Double> list: symbolTimes.values()) {
            for(int i = 0; i < list.size(); i++) {  // Contribute to the average
                average += list.get(i);
            }
            frontHalfAverage = 0;
            backHalfAverage = 0;

            if(list.size() > 2) {   // Check if it's decreasing
                for(int i = 0; i < list.size()/2; i++) {
                    frontHalfAverage += list.get(i);
                }
                frontHalfAverage /= list.size()/2;
                for(int i = list.size()/2; i < list.size(); i++) {
                    backHalfAverage += list.get(i);
                }
                backHalfAverage /= (list.size()-list.size()/2);

                if(frontHalfAverage > backHalfAverage) { // This list decreases (roughly)
                    numDecreasingSymbolAverages++;
                }
            }
        }

        average /= numSymbolsCorrect;
        boolean timesDecrease = numDecreasingSymbolAverages > 9 / 2;

        if(timesDecrease) {
            Toast.makeText(this, "Num symbols gotten: " + numSymbolsCorrect + ". Average time: " + (average / 1000) + ". The reactions are faster", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "Num symbols gotten: " + numSymbolsCorrect + ". Average time: " + (average / 1000) + ". The reactions are slower", Toast.LENGTH_LONG).show();
        }
    }
}
