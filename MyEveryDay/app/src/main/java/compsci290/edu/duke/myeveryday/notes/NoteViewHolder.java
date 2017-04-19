package compsci290.edu.duke.myeveryday.notes;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import compsci290.edu.duke.myeveryday.R;

/**
 * Created by FD on 4/18/2017.
 */

public class NoteViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.text_view_note_title)
    TextView title;

    @BindView(R.id.text_view_note_date)
    TextView noteDate;

    @BindView(R.id.image_view_expand)
    ImageView delete;

    @BindView(R.id.image_view) ImageView noteIcon;
    @BindView(R.id.circle_image_view) ImageView noteCircleIcon;

    public NoteViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

}
