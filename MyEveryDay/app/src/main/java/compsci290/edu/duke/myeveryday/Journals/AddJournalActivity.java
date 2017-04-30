package compsci290.edu.duke.myeveryday.Journals;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import compsci290.edu.duke.myeveryday.R;
import compsci290.edu.duke.myeveryday.util.Constants;

public class AddJournalActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journal);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getIntent() != null && getIntent().hasExtra(Constants.SERIALIZED_NOTE)){
            String serializedNote = getIntent().getStringExtra(Constants.SERIALIZED_NOTE);
            openFragment(JournalEditorFragment.newInstance(serializedNote), getString(R.string.note_editor));
        }
        else{
            openFragment(JournalEditorFragment.newInstance(""), getString(R.string.note_editor));
        }

    }



    private void openFragment(Fragment fragment, String screenTitle){
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.container, fragment)
                .addToBackStack(screenTitle)
                .commit();
        getSupportActionBar().setTitle(screenTitle);
    }

    @Override
    public void onBackPressed() {
        int fragments = getSupportFragmentManager().getBackStackEntryCount();
        if (fragments == 1) {
            finish();
        } else {
            if (getFragmentManager().getBackStackEntryCount() > 1) {
                getFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

}
