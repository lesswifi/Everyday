package compsci290.edu.duke.everyday;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by FD on 4/5/2017.
 */

public class JournalViewHolder extends RecyclerView.ViewHolder{

    TextView title;
    TextView journalDate;

    public JournalViewHolder(View v) {
        super(v);
        title = (TextView) v.findViewById(R.id.view_holder_title);
        journalDate = (TextView) v.findViewById(R.id.view_holder_date);
    }
}
