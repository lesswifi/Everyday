package compsci290.edu.duke.myeveryday.notes;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import compsci290.edu.duke.myeveryday.Models.JournalEntry;
import compsci290.edu.duke.myeveryday.R;
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

    private FirebaseRecyclerAdapter<JournalEntry, NoteViewHolder> mNoteFirebaseAdapter;

    private View mRootView;

    @BindView(R.id.note_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.empty_text)
    TextView eEmptyText;


    public NoteListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_journal_list, container, false);

        ButterKnife.bind(this, mRootView);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mcloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        mTagCloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);
        // Inflate the layout for this fragment

        mNoteFirebaseAdapter = new FirebaseRecyclerAdapter<JournalEntry, NoteViewHolder>(
                JournalEntry.class,
                R.layout.row_note_list,
                NoteViewHolder.class,
                mcloudReference) {

            @Override
            protected JournalEntry parseSnapshot (DataSnapshot snapshot){
                JournalEntry journal = super.parseSnapshot(snapshot);
                if (journal != null){
                    journal.setmID(snapshot.getKey());
                }

                return journal;
            }

            @Override
            protected void populateViewHolder(NoteViewHolder holder, JournalEntry model, int position) {
                holder.title.setText(model.getmTitle());
                /**holder.title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openNoteDetails(model);
                    }
                });**/

                holder.noteDate.setText(TimeUtils.getDueDate(model.getmDateModified()));
                /**holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        promptForDelete(model);
                    }
                });**/

                try {
                    if (model.getmJourneyType().equals(Constants.NOTE_TYPE_AUDIO)){
                        Glide.with(getContext()).load(R.drawable.headphone_button).into(holder.noteCircleIcon);
                    }else if (model.getmJourneyType().equals(Constants.NOTE_TYPE_REMINDER)){
                        Glide.with(getContext()).load(R.drawable.appointment_reminder).into(holder.noteCircleIcon);
                    } else if (model.getmJourneyType().equals(Constants.NOTE_TYPE_IMAGE)){
                        //Show the image
                    }else {                   //Show TextView Image

                        String firstLetter = model.getmTitle().substring(0, 1);
                        ColorGenerator generator = ColorGenerator.MATERIAL;
                        int color = generator.getRandomColor();

                        holder.noteCircleIcon.setVisibility(View.GONE);
                        holder.noteIcon.setVisibility(View.VISIBLE);

                        TextDrawable drawable = TextDrawable.builder()
                                .buildRound(firstLetter, color);
                        holder.noteIcon.setImageDrawable(drawable);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mNoteFirebaseAdapter);
        return mRootView;
    }

}
