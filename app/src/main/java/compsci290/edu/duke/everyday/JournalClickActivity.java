package compsci290.edu.duke.everyday;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

public class JournalClickActivity extends AppCompatActivity {

    TextView mJournalView;
    public static final String PREV_JOURNAL = "JournalPosition";
    private int prevJournal;
    private String journalId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        journalId = extras.getString("journalId");
        Log.d("JournalClickActivity", journalId);
        setContentView(R.layout.activity_journal_click);
    }
}
