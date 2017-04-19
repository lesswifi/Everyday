package compsci290.edu.duke.myeveryday.notes;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import compsci290.edu.duke.myeveryday.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class JournalEditorFragment extends Fragment {


    public JournalEditorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_note_editor, container, false);
    }



}
