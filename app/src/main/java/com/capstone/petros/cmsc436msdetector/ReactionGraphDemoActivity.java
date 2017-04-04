package com.capstone.petros.cmsc436msdetector;

import android.app.Activity;
import android.os.Bundle;

public class ReactionGraphDemoActivity extends Activity {

    private static final String REACTION_TIME_FILE_NAME = "reaction_test_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reaction_graph_demo);
        GraphFragment gf = (GraphFragment) getFragmentManager().findFragmentById(R.id.graph);
        gf.fillWithData(REACTION_TIME_FILE_NAME, 10);
    }
}
