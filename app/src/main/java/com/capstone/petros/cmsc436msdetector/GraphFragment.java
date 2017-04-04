package com.capstone.petros.cmsc436msdetector;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class GraphFragment extends Fragment {

    GraphView graphView;

    public GraphFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graph, container, false);
        graphView = (GraphView)view.findViewById(R.id.graphView);
        return view;
    }

    // Fills the graph with all the data in the file
    public void fillWithData(String fileName){
        fillWithData(fileName, -1);
    }

    // Fills the graph with the data from the file, but only the 'numberOfDataPoints'
    // most recent points.
    public void fillWithData(String fileName, int numberOfDataPoints){
        TreeMap<Long,Double> data = Utils.getResultsFromInternalStorage(getActivity(), fileName);
        long beginning = 0;
        if(numberOfDataPoints > 0) {
            Iterator<Long> it = data.descendingKeySet().iterator();
            for(int i = 0; it.hasNext() && i < numberOfDataPoints; i++){
                beginning = it.next();
            }
        }
        SortedMap<Long, Double> endData = data.tailMap(beginning);
        graphView.setData(endData);
        graphView.invalidate();
    }

}
