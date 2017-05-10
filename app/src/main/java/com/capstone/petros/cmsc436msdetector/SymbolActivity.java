package com.capstone.petros.cmsc436msdetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.petros.cmsc436msdetector.Sheets.Sheets;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SymbolActivity extends Activity implements Sheets.Host {
    private Activity act = this;
    private Sheets sheet;
    public static final int LIB_ACCOUNT_NAME_REQUEST_CODE = 1001;
    public static final int LIB_AUTHORIZATION_REQUEST_CODE = 1002;
    public static final int LIB_PERMISSION_REQUEST_CODE = 1003;
    public static final int LIB_PLAY_SERVICES_REQUEST_CODE = 1004;
    public static final int LIB_CONNECTION_REQUEST_CODE = 1005;
    private CountDownTimer testTimer, secondDelayRight, secondDelayWrong;
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
    private TextView voicePrompts;
    private SpeechRecognizer sr;
    private String startPrompt = "Say the number now.";
    private boolean firstVoice = true, testDone = false;

    private static final int TEST_DURATION = 90000; // 90 seconds in total
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symbol);

        sheet = new Sheets(this, this, getString(R.string.app_name));
        // Set bar red
        ProgressBar bar = (ProgressBar) findViewById(R.id.symbolProgressBar);
        bar.getIndeterminateDrawable().setColorFilter(0xFFDD2400, android.graphics.PorterDuff.Mode.SRC_IN);
        bar.getProgressDrawable().setColorFilter(0xFFDD2400, PorterDuff.Mode.SRC_IN);

        // Delay for 1 second...
        secondDelayRight = new CountDownTimer(1000, 1001) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                changeCurrentSymbol();
                recognizeSpeech("Say the number now.");
            }
        };

        secondDelayWrong = new CountDownTimer(1000, 1001) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                recognizeSpeech("Try again.\nSay the number now.");
            }
        };

        // each test lasts 90 seconds
        testTimer = new CountDownTimer(TEST_DURATION, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                ProgressBar bar = (ProgressBar) findViewById(R.id.symbolProgressBar);
                bar.setProgress((int) millisUntilFinished);
            }

            @Override
            public void onFinish() {
                //Toast.makeText(getApplicationContext(), "Test done!", Toast.LENGTH_SHORT).show();
                testDone = true;
                sr.stopListening();
                String results = getResults();

                // set progress bar to zero
                ProgressBar bar = (ProgressBar) findViewById(R.id.symbolProgressBar);
                bar.setProgress(0);

                findViewById(R.id.symbolNumpadGrid).setVisibility(View.GONE);
                findViewById(R.id.symbolVoicePrompts).setVisibility(View.VISIBLE);
                voicePrompts.setText(results);

                /*
                numSymbolsCorrect = 0;
                numGuesses = 0;
                symbolTimes.clear();

                Button startButton = (Button)findViewById(R.id.symbolStartNumpadButton);
                startButton.setEnabled(true);
                findViewById(R.id.symbolStartNumpadButton).setEnabled(true);
                enableNumpad(false);
                */
            }
        };

        symbolList = Arrays.asList((ImageView) findViewById(R.id.img1), (ImageView) findViewById(R.id.img2), (ImageView) findViewById(R.id.img3)
                , (ImageView) findViewById(R.id.img4), (ImageView) findViewById(R.id.img5), (ImageView) findViewById(R.id.img6), (ImageView) findViewById(R.id.img7)
                , (ImageView) findViewById(R.id.img8), (ImageView) findViewById(R.id.img9));
        reorderSymbols();
        voicePrompts = (TextView)findViewById(R.id.symbolVoicePrompts);
        if(SpeechRecognizer.isRecognitionAvailable(this)) {
            sr = SpeechRecognizer.createSpeechRecognizer(this);
            RecognitionListener rl = new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle bundle) {
                    if(testDone){
                        return;
                    }
                    // Show speech is ready...
                    voicePrompts.setText(startPrompt);
                    if(firstVoice){
                        // start the timer
                        testTimer.start();
                        changeCurrentSymbol();  // Puts the first current symbol up
                        firstVoice = false;
                    }
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
                    if(testDone){
                        return;
                    }
                    // Want to wait for results.
                    // Perhaps show that we're getting the results...
                    voicePrompts.setText("Decoding...");
                }

                @Override
                public void onError(int i) {
                    if(testDone){
                        return;
                    }
                    if(i == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS){
                        Toast.makeText(act, "Insufficient permissions to use speech recognizer",
                                Toast.LENGTH_LONG);
                    }
                    else if (i == SpeechRecognizer.ERROR_NO_MATCH ||
                            i == SpeechRecognizer.ERROR_SPEECH_TIMEOUT){
                        // Prompt the patient to try again
                        recognizeSpeech("No voice detected. Please try again.");
                    }
                    else {
                        // Some other error has happened
                        recognizeSpeech("An error has occured.");
                    }
                }

                private int detectNumber(ArrayList<String> results){
                    int detectedNumber = -1;
                    for(String result : results) {
                        if (result.contains("1") || result.contains("one")) {
                            if (detectedNumber == -1 || detectedNumber == 1) {
                                detectedNumber = 1;
                            }
                            else {
                                detectedNumber = -2;
                            }
                        }
                        if (result.contains("2") || result.contains("two")) {
                            if (detectedNumber == -1 || detectedNumber == 2) {
                                detectedNumber = 2;
                            } else {
                                detectedNumber = -2;
                            }
                        }
                        if (result.contains("3") || result.contains("three")) {
                            if (detectedNumber == -1 || detectedNumber == 3) {
                                detectedNumber = 3;
                            } else {
                                detectedNumber = -2;
                            }
                        }
                        if (result.contains("4") || result.contains("four")) {
                            if (detectedNumber == -1 || detectedNumber == 4) {
                                detectedNumber = 4;
                            } else {
                                detectedNumber = -2;
                            }
                        }
                        if (result.contains("5") || result.contains("five")) {
                            if (detectedNumber == -1 || detectedNumber == 5) {
                                detectedNumber = 5;
                            } else {
                                detectedNumber = -2;
                            }
                        }
                        if (results.contains("6") || results.contains("six")) {
                            if (detectedNumber == -1 || detectedNumber == 6) {
                                detectedNumber = 6;
                            } else {
                                detectedNumber = -2;
                            }
                        }
                        if (result.contains("7") || result.contains("seven")) {
                            if (detectedNumber == -1 || detectedNumber == 7) {
                                detectedNumber = 7;
                            } else {
                                detectedNumber = -2;
                            }
                        }
                        if (result.contains("8") || result.contains("eight")) {
                            if (detectedNumber == -1 || detectedNumber == 8) {
                                detectedNumber = 8;
                            } else {
                                detectedNumber = -2;
                            }
                        }
                        if (result.contains("9") || result.contains("nine")) {
                            if (detectedNumber == -1 || detectedNumber == 9) {
                                detectedNumber = 9;
                            } else {
                                detectedNumber = -2;
                            }
                        }
                    }
                    return detectedNumber;
                }

                @Override
                public void onResults(Bundle bundle) {
                    if(testDone){
                        return;
                    }
                    System.out.println("Results recieved!");
                    ArrayList<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    System.out.println("They are:\n"+results.toString());
                    int detectedNumber = detectNumber(results);
                    // Note: -1 = no number detected, -2 = multiple numbers detected.
                    if(detectedNumber == -1){
                        recognizeSpeech("No number detected. Please try again.");
                    }
                    else if (detectedNumber == -2){
                        recognizeSpeech("Multiple numbers detected. Please try again.");
                    }
                    else {
                        if(checkAnswer(detectedNumber)) {
                            // Show a new symbol
                            voicePrompts.setText("Detected Number:\n" + detectedNumber + "\nCorrect!");
                            secondDelayRight.start();
                        }
                        else {
                            voicePrompts.setText("Detected Number:\n" + detectedNumber + "\nIncorrect.");
                            secondDelayWrong.start();
                        }
                    }
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

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        this.sheet.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.sheet.onActivityResult(requestCode, resultCode, data);
    }

    public void recognizeSpeech(String startMessage){
        Intent i = new Intent();
        startPrompt = startMessage;
        sr.startListening(i);
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

    private boolean checkAnswer(int guessNum){
        numGuesses++;
        if(guessNum == curSymbolAnswer) {   // They got correct symbol number!
            numSymbolsCorrect++;

            // Record the new response time entry for the symbol
            symbolTimes.get(curSymbolAnswer).add(System.currentTimeMillis()-curSymbolAppearanceTime);

            return true;
        }
        return false;
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
        if(checkAnswer(guessNum)) {
            // Show a new symbol
            changeCurrentSymbol();
        }
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

    private void sendToSheets(Sheets.TestType sheetType, Sheets.TestType sheetType1, double result, int correctSymbols) {
        //sheet.writeData(sheetType, getString(R.string.patientID), (float)result);
        sheet.writeTrials(sheetType, getString(R.string.patientID), (float)result);
        sheet.writeTrials(sheetType1, getString(R.string.patientID), (float)correctSymbols);
    }

    public void reorderSymbols() {
        Collections.shuffle(imgList);

        for (int i = 0; i < 9; i++) {
            symbolList.get(i).setImageResource(imgList.get(i));
        }
    }

    public String getResults() {
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
        sendToSheets(Sheets.TestType.SYMBOL, Sheets.TestType.SYMBOL_CORRECT, average/1000, numSymbolsCorrect);
        DecimalFormat df = new DecimalFormat("#.000");
        if(timesDecrease) {
            return "Num symbols gotten: " + numSymbolsCorrect + ". Average time: " + df.format(average / 1000) + "s. The reactions are faster.";
        }
        else {
            return "Num symbols gotten: " + numSymbolsCorrect + ". Average time: " + df.format(average / 1000) + "s. The reactions are slower.";
        }
    }

    public void startNumpadTest(View view) {
        Intent i = new Intent();

        enableNumpad(true);

        // Disable the start test button
        Button startButton = (Button)findViewById(R.id.symbolStartNumpadButton);
        startButton.setEnabled(false);
        findViewById(R.id.symbolStartVoiceButton).setEnabled(false);


        // start the timer
        testTimer.start();
        changeCurrentSymbol();  // Puts the first current symbol up
    }

    public void startVoiceTest(View view) {
        Intent i = new Intent();
        sr.startListening(i);

        findViewById(R.id.symbolNumpadGrid).setVisibility(View.GONE);
        findViewById(R.id.symbolVoicePrompts).setVisibility(View.VISIBLE);

        // Disable the start test button
        Button startButton = (Button)findViewById(R.id.symbolStartNumpadButton);
        startButton.setEnabled(false);
        findViewById(R.id.symbolStartVoiceButton).setEnabled(false);
    }


    protected void onPause(){
        super.onPause();
        sr.stopListening();
        testTimer.cancel();
        secondDelayRight.cancel();
        secondDelayWrong.cancel();
        // Finishing is dangerous, but if a test stops...
        finish();
    }

    protected void onDestroy(){
        super.onDestroy();
        sr.destroy();
    }

    @Override
    public int getRequestCode(Sheets.Action action) {
        switch (action) {
            case REQUEST_ACCOUNT_NAME:
                return LIB_ACCOUNT_NAME_REQUEST_CODE;
            case REQUEST_AUTHORIZATION:
                return LIB_AUTHORIZATION_REQUEST_CODE;
            case REQUEST_PERMISSIONS:
                return LIB_PERMISSION_REQUEST_CODE;
            case REQUEST_PLAY_SERVICES:
                return LIB_PLAY_SERVICES_REQUEST_CODE;
            case REQUEST_CONNECTION_RESOLUTION:
                return LIB_CONNECTION_REQUEST_CODE;
            default:
                return -1;
        }
    }

    public void showTutorial(View v) {
        ScrollView frame = (ScrollView)findViewById(R.id.symbolFrame);
        TextView tutorialView = (TextView) findViewById(R.id.symbolInstructions);
        RelativeLayout shader = (RelativeLayout)findViewById(R.id.symbolShader);
        ImageView tutorialButton = (ImageView)findViewById(R.id.symbolTutorialButton);

        if (frame.getVisibility() == View.GONE) {
            frame.setVisibility(View.VISIBLE);
            tutorialView.setText("INSTRUCTIONS:\n\n" +
                    "This is the symbol test.\n\n" +
                    "It measures your ability to map symbols to numbers. This test lasts 90 seconds.\n\n" +
                    "During the test, a symbol will appear on the left side of the screen. When it comes up, " +
                    "check the key at the top of the screen to associate that symbol with its corresponding number.\n\n" +
                    "For the numpad test, enter the number of that symbol on the numpad. If your answer is right, a new symbol " +
                    "will appear. If your answer is incorrect, the symbol will stay until you choose correctly.\n\n" +
                    "For the voice test, say what the number is. The screen will display what number we detected you saying, " +
                    "and whether that was the correct number or not.\n\n"+
                    "For the voice test, if the app is having a hard time recognizing the number you are saying, try saying, " +
                    "\"The number is...\" and then saying the number.");
            shader.setVisibility(View.VISIBLE);
            tutorialButton.setColorFilter(0xFFF6FF00);
        }
        else {
            frame.setVisibility(View.GONE);
            shader.setVisibility(View.GONE);
            tutorialButton.setColorFilter(0xFF000000);
        }
    }

    @Override
    public void notifyFinished(Exception e) {

    }
}
