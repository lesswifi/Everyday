package compsci290.edu.duke.myeveryday;

import android.app.Activity;
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
import android.util.Log;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import compsci290.edu.duke.myeveryday.Authentication.AuthUiActivity;
import compsci290.edu.duke.myeveryday.Models.JournalEntry;
import compsci290.edu.duke.myeveryday.Models.SampleData;
import compsci290.edu.duke.myeveryday.Models.Tag;
import compsci290.edu.duke.myeveryday.Tag.TagList;
import compsci290.edu.duke.myeveryday.notes.AddJournalActivity;
import compsci290.edu.duke.myeveryday.notes.NoteListFragment;
import compsci290.edu.duke.myeveryday.util.Constants;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mdatabase;
    private DatabaseReference mcloudReference;
    private DatabaseReference mTagCloudReference;
    private DatabaseReference mdefaultdatareference;
    private FloatingActionButton mFab;
    private Activity mActivity;
    private String mUsername;
    private String mPhotoURL;
    private String memailaddress;
    private List<JournalEntry> journals;

    private SharedPreferences msharedPreferences;
    SharedPreferences.Editor meditor;
    private String storestringdmap;
    private AccountHeader mHeader = null;
    private Drawer mDrawer = null;
    HashMap<String, String> mmap= new HashMap<>();

    private static final String ANONYMOUS = "Anonymous";
    public static final String ANONYMOUS_PHOTO_URL = "https://dl.dropboxusercontent.com/u/15447938/notepadapp/anon_user_48dp.png";
    public static final String ANONYMOUS_EMAIL = "anonymous@noemail.com";

    @BindView(android.R.id.content) View mRootView;
    @BindView(R.id.toolbar) Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if(mFirebaseUser == null)
        {
            startActivity(new Intent(this, AuthUiActivity.class));
            finish();
            return;
        }
        else
        {
            //user is logged in
            mUsername = mFirebaseUser.getDisplayName();
            //if photourl is not loaded
            if(mFirebaseUser.getPhotoUrl() != null)
            {
            mPhotoURL = mFirebaseUser.getPhotoUrl().toString();
            }
            memailaddress = mFirebaseUser.getEmail();

            //String uid = mFirebaseUser.getUid();
        }

        mdatabase = FirebaseDatabase.getInstance().getReference();
        mcloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        mTagCloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);
        //mdefaultdatareference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + "DefaultTagAdded");
        //mdefaultdatareference.child(mdefaultdatareference.push().getKey()).setValue("Something");
        mActivity = this;
        journals = new ArrayList<>();

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mActivity, AddJournalActivity.class));
            }
        });


        mcloudReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot journalsnapshot: dataSnapshot.getChildren())
                {
                    JournalEntry journal = journalsnapshot.getValue(JournalEntry.class);
                    journals.add(journal);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setnavigationdrawer(savedInstanceState);


        SharedPreferences msp = PreferenceManager.getDefaultSharedPreferences(this);
        storestringdmap = msp.getString("Map", null);
        if(storestringdmap!=null) {
            Gson mg = new Gson();
            mmap = mg.fromJson(storestringdmap, new TypeToken<HashMap<String, String>>(){}.getType());
            if(mmap.get(mFirebaseUser.getUid()) == null )
            addDefaultTag();
        }
        else
        {
            addDefaultTag();
        }

    }





    public void setnavigationdrawer(Bundle savedInstanceState)
    {

        mUsername = TextUtils.isEmpty(mUsername)? ANONYMOUS:mUsername;
        memailaddress = TextUtils.isEmpty(memailaddress) ? ANONYMOUS_EMAIL : memailaddress;
        mPhotoURL = TextUtils.isEmpty(mPhotoURL) ? ANONYMOUS_PHOTO_URL : mPhotoURL;

        IProfile profile = new ProfileDrawerItem()
                .withName(mUsername)
                .withEmail(memailaddress)
                .withIcon(mPhotoURL)
                .withIdentifier(102);

        mHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(profile)
                .build();

        mDrawer = new DrawerBuilder()
                .withAccountHeader(mHeader)
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Journals").withIcon(GoogleMaterial.Icon.gmd_view_list).withIdentifier(Constants.NOTES),
                        new PrimaryDrawerItem().withName("Tags").withIcon(GoogleMaterial.Icon.gmd_folder).withIdentifier(Constants.CATEGORIES),
                        new PrimaryDrawerItem().withName("Analytics").withIcon(GoogleMaterial.Icon.gmd_caret_up_circle).withIdentifier(Constants.ANALYTICS),
                        new PrimaryDrawerItem().withName("Logout").withIcon(GoogleMaterial.Icon.gmd_lock).withIdentifier(Constants.LOGOUT)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null && drawerItem instanceof Nameable){
                            String name = ((Nameable) drawerItem).getName().getText(mActivity);
                            toolbar.setTitle(name);
                        }

                        if (drawerItem != null){
                            //handle on navigation drawer item
                            onTouchDrawer((int) drawerItem.getIdentifier());
                        }

                        return false;
                    }
                })
                .withOnDrawerListener(new Drawer.OnDrawerListener()
                {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        KeyboardUtil.hideKeyboard(MainActivity.this);

                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {

                    }
                })
                .withFireOnInitialOnClick(true)
                .withSavedInstance(savedInstanceState)
                .build();
        mDrawer.addStickyFooterItem(new PrimaryDrawerItem().withName("Delete Account!").withIcon(GoogleMaterial.Icon.gmd_delete).withIdentifier(Constants.DELETE));
        openFragment(new NoteListFragment(), "Journals");

        }

    public void onTouchDrawer(int position)
    {
        switch (position){
            case Constants.NOTES:
                //Do Nothing, we are already on Notes
                break;
            case Constants.CATEGORIES:
                startActivity(new Intent(MainActivity.this, TagList.class));
                break;
            case Constants.ANALYTICS:
                startActivity(new Intent(MainActivity.this, AnalyticsActivity.class));
                break;
            case Constants.LOGOUT:
                logout();
                break;
            case Constants.DELETE:
                deleteAccountClicked();
                break;
        }
    }

    public void logout()
    {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(mActivity, MainActivity.class));
                            finish();
                        } else {
                            showSnackbar(R.string.sign_out_failed);
                        }
                    }
                });


    }

    public void deleteAccountClicked() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes, delete it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteAccount();
                    }
                })
                .setNegativeButton("No", null)
                .create();

        dialog.show();
    }

    private void deleteAccount() {
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(mActivity, MainActivity.class));
                            finish();
                        } else {
                            showSnackbar(R.string.delete_account_failed);
                        }
                    }
                });
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    private void openFragment(Fragment fragment, String screenTitle){
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.container, fragment)
                .addToBackStack(screenTitle)
                .commit();
        getSupportActionBar().setTitle(screenTitle);
    }

//Starts here is for testing the funcationality of the app
    public void addDefaultTag()
    {
        msharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        meditor = msharedPreferences.edit();

        //if (msharedPreferences.getBoolean(Constants.FIRST_RUN, true)) {
            //meditor.putBoolean(Constants.FIRST_RUN, false).commit();
        mmap.put(mFirebaseUser.getUid(), "Yeah");
        Gson gson = new Gson();
        String storemap = gson.toJson(mmap);
        meditor.putString("Map",storemap);
        meditor.commit();
        addInitialTagToFirebase();

    }

    private void addInitialTagToFirebase() {

        List<String> categoryNames = SampleData.getSampleCategories();
        for (String categoryName: categoryNames){
            Tag category = new Tag();
            category.setmTagName(categoryName);
            category.setmTagID(mTagCloudReference.push().getKey());
            mTagCloudReference.child(category.getmTagID()).setValue(category);
        }
    }

    /*public void addinitialdatatofirebase()
    {
        List<JournalEntry> sampleNotes = SampleData.getSampleNotes();
        for (JournalEntry note: sampleNotes){
            String key = mcloudReference.push().getKey();
            note.setmID(key);
            mcloudReference.child(key).setValue(note);
        }


    }*/
}
