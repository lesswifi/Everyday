package compsci290.edu.duke.myeveryday.Tag;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import compsci290.edu.duke.myeveryday.Listeners.TagSelectedListener;
import compsci290.edu.duke.myeveryday.Models.Tag;
import compsci290.edu.duke.myeveryday.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectTagFragment extends DialogFragment {

    private List<Tag> mtags;
    private TagDialogAdapter mtagdialogadapter;
    private TagSelectedListener mtagselectedlistener;


    public SelectTagFragment() {
        // Required empty public constructor
    }

    public void settagselectedlistner(TagSelectedListener mtagselectedlistener)
    {
        this.mtagselectedlistener = mtagselectedlistener;
    }
    public void settags(List<Tag>  tags)
    {
        mtags = tags;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    public static SelectTagFragment newInstance()
    {
        return new SelectTagFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = (View) inflater.inflate(R.layout.tag_dialog_list, null);
        builder.setView(view);
        View titleView = (View)inflater.inflate(R.layout.choose_tag_dialog_title, null);
        builder.setCustomTitle(titleView);
        builder.setTitle("Choose Tag");

        ListView dialogList = (ListView) view.findViewById(R.id.dialog_listview);
        TextView emptyText = (TextView) view.findViewById(R.id.category_list_empty);
        dialogList.setEmptyView(emptyText);

        mtagdialogadapter = new TagDialogAdapter(getActivity(), mtags);
        dialogList.setAdapter(mtagdialogadapter);

        dialogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tag mSelectedtag = mtags.get(position);
                if (mSelectedtag != null){
                    mtagselectedlistener.onTagSelected(mSelectedtag);
                }
            }
        });

        return builder.create();
    }



}
