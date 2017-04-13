package compsci290.edu.duke.everyday;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.util.Log;
import android.content.Intent;
/**
 * Created by FD on 4/5/2017.
 */

public class JournalViewHolder extends RecyclerView.ViewHolder{

    TextView title;
    TextView journalDate;
    TextView journalId;

    public JournalViewHolder(View v) {
        super(v);
        title = (TextView) v.findViewById(R.id.view_holder_title);
        journalDate = (TextView) v.findViewById(R.id.view_holder_date);
        journalId = (TextView) v.findViewById(R.id.view_holder_id);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("hey", "hey");
                Intent intent = new Intent(v.getContext(), JournalClickActivity.class);
                intent.putExtra("journalId", journalId.getText().toString());
                v.getContext().startActivity(intent);
            }
        });
    }
}
