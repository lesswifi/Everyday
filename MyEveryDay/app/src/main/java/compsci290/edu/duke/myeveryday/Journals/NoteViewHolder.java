package compsci290.edu.duke.myeveryday.Journals;

import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
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

    @BindView(R.id.rootView)
    public CardView card;

    @BindView(R.id.image_view_note_photo)
    public ImageView photo;

    @BindView(R.id.text_view_note_title)
    public TextView title;

    @BindView(R.id.text_view_note_content)
    public TextView content;

    @BindView(R.id.text_view_note_location_weather)
    public TextView location_weather;

    @BindView(R.id.weather_icon)
    public ImageView weather_icon;


    @BindView(R.id.text_view_note_time)
    @Nullable
    public TextView noteTime;

    public NoteViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

}
