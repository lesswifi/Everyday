package compsci290.edu.duke.myeveryday.Tag;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import compsci290.edu.duke.myeveryday.Listeners.TagSelectedListener;
import compsci290.edu.duke.myeveryday.Models.Tag;
import compsci290.edu.duke.myeveryday.R;

/**
 * Created by yx78 on 4/23/17.
 * This is the adapter used for recyclerview to display the tags
 */

public class TagListAdapter extends RecyclerView.Adapter<TagListAdapter.ViewHolder> {
    private List<Tag> mTag;
    private final Context mContext;
    private final TagSelectedListener mtagselectedlistner;

    public TagListAdapter(List<Tag> mTag, Context mContext, TagSelectedListener mtagselectedlistner) {
        this.mTag = mTag;
        this.mContext = mContext;
        this.mtagselectedlistner = mtagselectedlistner;
    }

    @Override
    public TagListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_tag_list, parent, false);
        ViewHolder holder = new ViewHolder(row);
        return holder;
    }

    @Override
    public void onBindViewHolder(TagListAdapter.ViewHolder holder, int position) {
        Tag tag = mTag.get(position);
        holder.mTagname.setText(tag.getmTagName());
        int numNote;
        try {
            numNote = tag.getJournalcount();
        } catch (Exception e) {
            numNote = 0;
        }
        String notes = numNote > 1 ? "Notes" : "Note";
        holder.mjournalcounttextview.setText(numNote + " " + notes);

    }

    public void replaceData(List<Tag> tags)
    {
        mTag = tags;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mTag.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.image_button_edit_category)
        ImageView editTag;
        @BindView(R.id.image_button_delete_category)
        ImageView deleteTag;
        @BindView(R.id.text_view_category_name)
        TextView mTagname;
        @BindView(R.id.text_view_note_count)
        TextView mjournalcounttextview;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tag tagtobedisplayed = mTag.get(getLayoutPosition());
                    mtagselectedlistner.onTagSelected(tagtobedisplayed);
                }
            });
            editTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tag tagtobeedited = mTag.get(getLayoutPosition());
                    mtagselectedlistner.onEditButtionClicked(tagtobeedited);

                }
            });

            deleteTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tag tagtobechanged = mTag.get(getLayoutPosition());
                    mtagselectedlistner.onDeleteButtonClicked(tagtobechanged);
                }
            });
        }
    }
}
