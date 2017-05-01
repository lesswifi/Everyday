package compsci290.edu.duke.myeveryday.Tag;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import compsci290.edu.duke.myeveryday.Models.JournalEntry;
import compsci290.edu.duke.myeveryday.Models.Tag;
import compsci290.edu.duke.myeveryday.util.Constants;

/**
 * Created by yx78 on 4/20/17.
 */
public class DeleteTagIntentService extends IntentService {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mdatabase;
    private DatabaseReference mjournalreference;
    private DatabaseReference mtagcloudreferece;
    private List<Tag> mtags;


    public DeleteTagIntentService() {
        super("DeleteTagIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mtags = new ArrayList<>();

        mjournalreference =  mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        mtagcloudreferece =  mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);

        mtagcloudreferece.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot tagsnapshot: dataSnapshot.getChildren())
                {
                    Tag mtag = null;
                    try{
                        mtag = tagsnapshot.getValue(Tag.class);
                        mtags.add(mtag);
                    } catch(Exception e)
                    {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Bundle args = intent.getExtras();

        if (args != null && args.containsKey(Constants.SELECTED_CATEGORY_ID)) {
            final String tagid = args.getString(Constants.SELECTED_CATEGORY_ID);
            if (!TextUtils.isEmpty(tagid)) {
                mjournalreference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                            JournalEntry mjournal = noteSnapshot.getValue(JournalEntry.class);
                            if (!TextUtils.isEmpty(mjournal.getmTagID()) && mjournal.getmTagID().equals(tagid)) {
                                String defaultTagId = getDefaultTagId();
                                mjournal.setmTagID(defaultTagId);
                                updateJournalBackToFirebase(mjournal);
                            }
                        }
                        mtagcloudreferece.child(tagid).removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        }

    }

    private void updateJournalBackToFirebase(JournalEntry mjournal) {
        mjournalreference.child(mjournal.getmID()).setValue(mjournal);
    }

    private String getDefaultTagId() {
        Tag mtag = null;
        for(Tag tag: mtags)
        {
            if(tag.getmTagName().equals(Constants.DEFAULT_CATEGORY))
            {
                mtag = tag;
                break;
            }
        }
        if(mtag == null)
        {
            return addTagtoFirebase(Constants.DEFAULT_CATEGORY);
        }
        else
        {
            return mtag.getmTagID();
        }

    }

    private String addTagtoFirebase(String tag) {
        Tag mtag = new Tag();
        mtag.setmTagName(tag);
        String key = mtagcloudreferece.push().getKey();
        mtag.setmTagID(key);
        mtagcloudreferece.child(key).setValue(mtag);
        return mtag.getmTagID();
    }

}
