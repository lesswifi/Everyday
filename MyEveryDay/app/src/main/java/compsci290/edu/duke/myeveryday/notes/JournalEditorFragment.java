package compsci290.edu.duke.myeveryday.notes;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.PathInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import butterknife.BindView;
import butterknife.ButterKnife;
import compsci290.edu.duke.myeveryday.MainActivity;
import compsci290.edu.duke.myeveryday.Models.JournalEntry;
import compsci290.edu.duke.myeveryday.R;
import compsci290.edu.duke.myeveryday.util.Constants;

/**
 * A simple {@link Fragment} subclass.
 */
public class JournalEditorFragment extends Fragment {

    @BindView(R.id.edit_text_tag)
    EditText mTag;

    @BindView(R.id.edit_text_title)
    EditText mTitle;

    @BindView(R.id.edit_text_journal)
    EditText mContent;

    private View mRootView;
    private JournalEntry currentJournal = null;
    private boolean isInEditMode = false;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mdatabase;
    private DatabaseReference mcloudReference;
    private DatabaseReference mTagCloudReference;

    public JournalEditorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public static JournalEditorFragment newInstance(String content){
        JournalEditorFragment fragment = new JournalEditorFragment();
        if (!TextUtils.isEmpty(content)){
            Bundle args = new Bundle();
            args.putString(Constants.SERIALIZED_NOTE, content);
            fragment.setArguments(args);
        }

        return fragment;
    }

    public void getCurrentNode(){
        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.SERIALIZED_NOTE)){
            String serializedJournal = args.getString(Constants.SERIALIZED_NOTE, "");
            if (!serializedJournal.isEmpty()){
                Gson gson = new Gson();
                currentJournal = gson.fromJson(serializedJournal, JournalEntry.class);
                if (currentJournal != null & !TextUtils.isEmpty(currentJournal.getmID())){
                    isInEditMode = true;
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_journal_editor, container, false);
        ButterKnife.bind(this, mRootView);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mcloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        mTagCloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);

        getCurrentNode();
        return mRootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (isInEditMode){
            populateNode(currentJournal);
        }
    }

    private void populateNode(JournalEntry journal) {
        mTitle.setText(journal.getmTitle());
        mTitle.setHint(getString(R.string.placeholder_note_title));
        mContent.setText(journal.getmContent());
        mContent.setHint(getString(R.string.placeholder_note_text));
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_note_editor, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_save:
                validateAndSaveContent();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void validateAndSaveContent() {
        String title = mTitle.getText().toString();

        if (TextUtils.isEmpty(title)){
            mTitle.setError(getString(R.string.title_is_required));
            return;
        }


        String content = mContent.getText().toString();
        if (TextUtils.isEmpty(content)){
            mContent.setError(getString(R.string.note_is_required));
            return;
        }

        addNotetoFirebase();

    }

    private void addNotetoFirebase() {

        if (currentJournal == null){
            currentJournal = new JournalEntry();
            String key = mcloudReference.push().getKey();
            currentJournal.setmID(key);

        }

        currentJournal.setmTitle(mTitle.getText().toString());
        currentJournal.setmContent(mContent.getText().toString());
        currentJournal.setmDateCreated(System.currentTimeMillis());
        currentJournal.setmDateModified(System.currentTimeMillis());

        mcloudReference.child(currentJournal.getmID()).setValue(currentJournal);

        String result = isInEditMode ? "Note updated" : "Note added";
        makeToast(result);
        startActivity(new Intent(getActivity(), MainActivity.class));

    }

    private void makeToast(String message){
        Snackbar snackbar = Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG);

        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary));
        TextView tv = (TextView)snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();
    }


}
