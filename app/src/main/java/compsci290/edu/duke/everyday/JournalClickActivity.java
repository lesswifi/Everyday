package compsci290.edu.duke.everyday;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class JournalClickActivity extends AppCompatActivity {

    public DatabaseReference mDatabase;
    public DatabaseReference journalCloudEndPoint;
    private String journalId;
    private List<JournalEntry> mJournalEntries = new ArrayList<>();
    private String LOG_TAG = "JournalClick";

    private TextView titleView;
    private TextView contentView;
    private TextView dateView;
    private TextView tagView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_click);

        Bundle extras = getIntent().getExtras();
        journalId = extras.getString("journalId");
        Log.d("JournalClickActivity", journalId);

        mDatabase =  FirebaseDatabase.getInstance().getReference();
        journalCloudEndPoint = mDatabase.child("journalentris");


        titleView = (TextView)findViewById(R.id.journal_title);
        contentView = (TextView)findViewById(R.id.journal_content);
        dateView = (TextView)findViewById(R.id.journal_date);
        tagView = (TextView)findViewById(R.id.journal_tag);


    }

    private void pullDatafromFirebase(){

        journalCloudEndPoint.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                for (DataSnapshot noteSnapshot: dataSnapshot.getChildren()){
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
