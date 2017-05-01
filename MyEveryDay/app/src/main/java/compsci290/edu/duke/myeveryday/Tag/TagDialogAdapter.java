package compsci290.edu.duke.myeveryday.Tag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import compsci290.edu.duke.myeveryday.Models.Tag;
import compsci290.edu.duke.myeveryday.R;

/**
 * Created by yx78 on 4/27/17.
 */

public class TagDialogAdapter extends ArrayAdapter<Tag> {
    private List<Tag> mtags;
    private Context mContext;


    public TagDialogAdapter(Context context, List<Tag> tags) {
        super(context, android.R.layout.simple_list_item_1, tags);
        mtags = tags;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mtags.size();
    }

    @Override
    public Tag getItem(int position) {
        if (position < mtags.size()) {
            return mtags.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Tag tag = mtags.get(position);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.tag_list_text, null);
        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        text1.setText(tag.getmTagName());
        return view;
    }

}
