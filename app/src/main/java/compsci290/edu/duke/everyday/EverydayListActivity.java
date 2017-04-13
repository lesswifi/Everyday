package compsci290.edu.duke.everyday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EverydayListActivity extends AppCompatActivity {

    public DatabaseReference mDatabase;

    private List<JournalEntry> mJournalEntries = new ArrayList<>();
    private List<Tag> mTags = new ArrayList<>();

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String FIRST_RUN = "FIRST_RUN";

    private TextView tagView;
    private String LOG_TAG = "DiaryList";

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set default layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_everyday_list);
        tagView = (TextView)findViewById(R.id.tagId);

        //set recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //initiate firebase
        mDatabase =  FirebaseDatabase.getInstance().getReference();

        //sharepreferences to check and call addInitialDataToFirebase() only once
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        if (sharedPreferences.getBoolean(FIRST_RUN, true)) {
            //addInitialDataToFirebase();
            editor.putBoolean(FIRST_RUN, false).commit();
        }

        // Floating Action Button Launches the NewJournalEntryActivity
        findViewById(R.id.fab_new_quiz).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(LOG_TAG, "onClick: clicked on add  button");
                        startActivity(new Intent(EverydayListActivity.this, WriteJournalActivity.class));

                    }
                }
        );

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        databaseHelper.getWritableDatabase();

        //pullDatafromFirebase();
        setUpRecyclerView();

    }

    private void setUpRecyclerView(){

        FirebaseRecyclerAdapter<JournalEntry, JournalViewHolder> mJournalFirebaseAdapter =
                new FirebaseRecyclerAdapter<JournalEntry, JournalViewHolder>(
                        JournalEntry.class,
                        R.layout.journal_custom_row,
                        JournalViewHolder.class,
                        mDatabase){

                    @Override
                    protected void populateViewHolder
                            (JournalViewHolder holder, JournalEntry model, final int position) {
                        holder.title.setText(model.getTitle());
                        holder.journalDate.setText("" + model.getDateModified());
                        holder.journalId.setText(model.getJournalId());
                    }
                };

        mRecyclerView.setAdapter(mJournalFirebaseAdapter);
    }


    private void addInitialDataToFirebase() {

        List<JournalEntry> sampleJournalEntries = SampleData.getSampleJournalEntries();
        for (JournalEntry journalEntry: sampleJournalEntries){
            String key = mDatabase.push().getKey();
            journalEntry.setJournalId(key);
            mDatabase.child(key).setValue(journalEntry);
        }

    }


    private void pullDatafromFirebase() {


        mDatabase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                    JournalEntry note = noteSnapshot.getValue(JournalEntry.class);
                    mJournalEntries.add(note);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, databaseError.getMessage());
            }
        });

    }

}
