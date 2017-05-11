# CMSC436_MS_Detector_Team_Koutras
The CMSC436 MS detector project, with Peter being the project manager, done at the University of Maryland.

This MS test suite includes the following tests: Tap test, Spiral test, Ball test, Reaction test, Flex test, Sway test, Walking test, Symbol test, and Vibration test.

For Emily, keep in mind the following notes about each test:
Tap Test: We made the tap test when we were still on the trial system, so it requires you do three trials with each hand before submitting.

Spiral Test: The "save" option that appears after the test times out (no interaction for five seconds) only saves to the gallery. Press the "save to sheets" buttons on the top to save the results to the Google sheets.

Ball Test (level test): The metric for this test is the harmonic mean between the change in orientation angle and the magnitude of orientation change at each orientation sampled as the test ran. The lower the score, the better.

Reaction Test (Balloon Test): No notes, except that it is hooked up to a graph demo intended to show the patients their test results (see below).

Flex Test: When doing this test, please keep your thumb on the screen as you flex in and out, and make sure the long edge of the phone is perpendicular to you as you flex (like you are holding the phone in landscape mode). This test was implemented when we thought the patients had to do a 180º rotation, so try to do a complete arm-straight to shoulder-touch flex. Finally, this test is extremely sensitive to wrist motion - try to keep your wrist straight when doing this test.

Sway Test: This test requires a headband to perform, as it required you to put your phone inside the headband. The audio prompts will guide you from there. The metrics for this test are: 1) The angle score, which is how much the person rotated their head over the course of the test, and 2) the movement score, which is how much the person shifted their head over the course of the test.

Step test (indoor walking test): This test similarly requires a headband, and a step detector on your phone to run. Furthermore, since it uses the acceleration sensor, error tends to build up, so this test can be very inaccurate.

Walking test (outdoor walking test): This test uses GPS data, so be sure to be outside with location turned on to perform this test.

Symbol Test: If the Google voice recognizer is not your default speech recognizer on your phone, this app may have some problems. For example, the Samsung S-voice does not play nice with other apps, and if that is the default voice recognizer, it may cause the voice recognition to be unresponsive. Furthermore, since the voice recognizer may have to connect to Google servers, be sure to have data connectivity when testing this app. If the speech recognizer is having trouble recognizing the number you are saying (especially the number "three"), try saying, "the number is ___", to give the recognizer more context. At the end of the test, the following metrics are displayed: the number of symbols gotten, the average time between symbols, and whether or not the user started to recognize the number tied to each symbol and click that symbol faster as the test progressed (it will say, "the reactions are faster" if this is the case, and "the reactions are slower" if this is not the case).

Vibration Test: Because we could not find a viable way to lower/raise the vibration intensity with exact control over the Hertz, the test works as follows: the user puts their knuckles on the screen, and the phone vibrates for progressively less and less time at set intervals. When the user can no longer feel the vibration, they hit the "end test" button. The metric is the user's threshold of vibration length they can detect, in milliseconds.

Furthermore, we have implemented an in-app grapher to show the user the data from their tests. This grapher can be found under "graph demo," and is currently hooked up to the reaction test. It displays the test score for the past 10 reaction test trials on a line graph. Furthermore, the user can click the data points on the graph to see the time the test was taken, and their score. The x-axis was the time the test was taken, so if you want to see the graph clearly, I recommend doing the reaction trials at regular intervals (ex: all at once, every 5 minutes, etc). If less then 10 data points are present, it will just display the data it does have.

General notes:
Most tests have a question mark icon, located in the corner, which will explain how the test works, in case you are confused.
As these tests are demos, we have do not handle the case when the user leaves the app. As such, locking the phone or navigating away from the app while a test is ongoing will immediately cancel the test, and bring the user back to the main screen when they come back from the app.
To send data from the app to sheets, make sure it is signed properly, and that the fingerprint you use can send data to google sheets (especially if you are making a release build).

The sheets data for our app is located here:
https://docs.google.com/spreadsheets/d/16-GKcyq_X0bFV8ZwughOTK-ciZrhMiSqF5IbZZYZb_s/edit#gid=1207701066
An explanation of each metric is also written on each Google sheet tab.
