package compsci290.edu.duke.everyday;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class JournalClickActivity extends AppCompatActivity {

    TextView mJournalView;
    public static final String PREV_JOURNAL = "JournalPosition";
    private int prevJournal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_click);
    }
}
