package compsci290.edu.duke.myeveryday.Journals;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import compsci290.edu.duke.myeveryday.Models.JournalEntry;
import compsci290.edu.duke.myeveryday.R;
import compsci290.edu.duke.myeveryday.util.CameraHelper;
import compsci290.edu.duke.myeveryday.util.Constants;
import compsci290.edu.duke.myeveryday.util.TimeUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoteListFragment extends Fragment {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mdatabase;
    private DatabaseReference mcloudReference;
    private DatabaseReference mTagCloudReference;
    private String mselectedtagid;

    private FirebaseRecyclerAdapter<JournalEntry, NoteViewHolder> mNoteFirebaseAdapter;

    private View mRootView;

    @BindView(R.id.note_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.empty_text)
    TextView mEmptyText;
    

    public NoteListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_journal_list, container, false);
        Bundle mbundle = this.getArguments();
        if(mbundle != null) {
            mselectedtagid = mbundle.getString("Selected");
        }


        ButterKnife.bind(this, mRootView);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mcloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        mTagCloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);
        // Inflate the layout for this fragment

        Query journalquery ;
        if(mselectedtagid != null)
        {
            journalquery = mcloudReference.orderByChild("mTagID").equalTo(mselectedtagid);
        }

        else
        {
            journalquery = mcloudReference.orderByChild("mDateCreated");
        }

        mNoteFirebaseAdapter = new FirebaseRecyclerAdapter<JournalEntry, NoteViewHolder>(
                JournalEntry.class,
                R.layout.row_note_list,
                NoteViewHolder.class,
                journalquery) {

            @Override
            protected JournalEntry parseSnapshot (DataSnapshot snapshot){
                JournalEntry journal = super.parseSnapshot(snapshot);
                if (journal != null){
                    journal.setmID(snapshot.getKey());
                }

                return journal;
            }

            @Override
            protected void populateViewHolder(NoteViewHolder holder, final JournalEntry model, int position) {
                holder.title.setText(model.getmTitle());
                holder.content.setText(model.getmContent());

                String weatherIconUrl = null;
                if (model.getmWeather() != null && model.getmWeather().startsWith("http")) {
                    weatherIconUrl = model.getmWeather();
                }
                CameraHelper.displayImageInView(getActivity(), weatherIconUrl, holder.weather_icon);
                holder.location_weather.setText(model.getmLocation());

                String imageUrl = null;
                if (!model.getmImagePaths().isEmpty()) {
                    imageUrl = model.getmImagePaths().get(0);
                }
                CameraHelper.displayImageInView(getActivity(), imageUrl, holder.photo);
                holder.photo.setMaxHeight(400);

                holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Gson gson = new Gson();
                        String serializedJournal = gson.toJson(model);
                        Intent editIntent = new Intent(getActivity(), AddJournalActivity.class);
                        editIntent.putExtra(Constants.SERIALIZED_NOTE, serializedJournal);
                        startActivity(editIntent);
                    }
                });

                holder.noteTime.setText(TimeUtils.getReadableModifiedShortDate(model.getmDateCreated()) + "\n" + TimeUtils.getReadableModifiedTime(model.getmDateCreated()));

            }
        };

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mNoteFirebaseAdapter);
        return mRootView;
    }


    public void showEmptyText() {
        mRecyclerView.setVisibility(View.GONE);
        mEmptyText.setVisibility(View.VISIBLE);
    }

}
