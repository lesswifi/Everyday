package compsci290.edu.duke.myeveryday;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.mikepenz.materialdrawer.util.KeyboardUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import compsci290.edu.duke.myeveryday.Authentication.AuthUiActivity;
import compsci290.edu.duke.myeveryday.Journal.AddJournalActivity;
import compsci290.edu.duke.myeveryday.Journal.JournalListFragment;
import compsci290.edu.duke.myeveryday.Models.JournalEntry;
import compsci290.edu.duke.myeveryday.Models.SampleData;
import compsci290.edu.duke.myeveryday.Models.Tag;
import compsci290.edu.duke.myeveryday.Tag.TagListActivity;
import compsci290.edu.duke.myeveryday.util.Constants;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mdatabase;
    private DatabaseReference mcloudReference;
    private DatabaseReference mTagCloudReference;
    private DatabaseReference mdefaultdatareference;
    private DatabaseReference muserdatareference;
    private FloatingActionButton mFab;
    private Activity mActivity;
    private List<JournalEntry> journals;

    private String CURRENT_ACTIVITY = "Current Activity";
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private int SelectedItem;

    @BindView(android.R.id.content)
    View mRootView;
    @BindView(R.id.tool_bar_item)
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar_item);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();
        SelectedItem = sp.getInt(CURRENT_ACTIVITY, -1);
        editor.putInt(CURRENT_ACTIVITY, 1).commit();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            startActivity(new Intent(this, AuthUiActivity.class));
            finish();
            return;
        }


        //Get the database reference
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mcloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        mTagCloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);

        muserdatareference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid());
        muserdatareference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("add"))
                    addInitialTagToFirebase();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mActivity = this;
        journals = new ArrayList<>();

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start the addjournalactivity
                startActivity(new Intent(mActivity, AddJournalActivity.class));
            }
        });


        mcloudReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot journalsnapshot : dataSnapshot.getChildren()) {
                    JournalEntry journal = journalsnapshot.getValue(JournalEntry.class);
                    journals.add(journal);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        openToolBar(new ToolBarFragment(), "Journals");
        openFragment(new JournalListFragment(), "Journals");

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void openFragment(Fragment fragment, String screenTitle) {
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.container, fragment)
                .addToBackStack(screenTitle)
                .commit();
    }

    private void openToolBar(Fragment fragment, String screenTitle) {
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.tool_bar, fragment)
                .addToBackStack(screenTitle)
                .commit();
    }


    //Add some inital tags to firebase
    private void addInitialTagToFirebase() {

        List<String> categoryNames = SampleData.getSampleCategories();
        for (String categoryName : categoryNames) {
            Tag category = new Tag();
            category.setmTagName(categoryName);
            category.setmTagID(mTagCloudReference.push().getKey());
            mTagCloudReference.child(category.getmTagID()).setValue(category);
        }
        mdefaultdatareference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + "/add");
        mdefaultdatareference.push().setValue("Yes");

    }

    @Override
    public void onBackPressed() {
        int fragments = getSupportFragmentManager().getBackStackEntryCount();
        if (fragments == 1) {
            finish();
        } else {
            if (getFragmentManager().getBackStackEntryCount() > 1) {
                getFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }
}
