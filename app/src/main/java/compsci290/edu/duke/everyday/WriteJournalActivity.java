package compsci290.edu.duke.everyday;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Created by Divya on 4/10/17.
 */


public class WriteJournalActivity extends Activity {

    private static final String TAG = "NewJournalEntryActivity";
    private static final String REQUIRED = "Required";
    // Get the database reference
    private DatabaseReference mDatabase;

    private EditText mTitleField;
    private EditText mTagField;
    private EditText mContentField;
    private FloatingActionButton mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_quiz);

        // Initialize the DB Reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mTitleField = (EditText) findViewById(R.id.edit_title);
        mTagField = (EditText) findViewById(R.id.edit_tag);
        mContentField = (EditText) findViewById(R.id.edit_content);
        mSubmitButton = (FloatingActionButton) findViewById(R.id.fab_submit_quiz);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = mTitleField.getText().toString();
                final String tag = mTagField.getText().toString();
                final String content = mContentField.getText().toString();

                if(TextUtils.isEmpty(title)) {
                    mTitleField.setError(REQUIRED);
                    return;
                } else if (TextUtils.isEmpty(tag)) {
                    mTagField.setError(REQUIRED);
                    return;
                }
                setEditingEnabled(false);
                Snackbar.make(v, "Uploading entry...", Snackbar.LENGTH_SHORT).show();

                Calendar calendar = GregorianCalendar.getInstance();
                long date = calendar.getTimeInMillis();
                submitEntry(title, content, tag, date);
/**                mDatabase.child("journalentries").addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Date date = new Date();
                                submitEntry(title, content, tag, date);
                                setEditingEnabled(true);
                                finish();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(TAG, "onCancelled", databaseError.toException());
                                setEditingEnabled(true);
                            }
                        }
                );  **/
            }
        });
    }

    private void setEditingEnabled(boolean value) {
        mTitleField.setEnabled(value);
        mTagField.setEnabled(value);
        mContentField.setEnabled(value);
        if (value) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    private void submitEntry(String title, String content, String tag, long date) {

        // journal entries key
        String key = mDatabase.child("journalentries").push().getKey();
        JournalEntry journalEntry = new JournalEntry(title, content, tag, date);

        mDatabase.child(key).setValue(journalEntry);
        journalEntry.setJournalId(key);
    }

}
