package compsci290.edu.duke.myeveryday;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
    private String mUsername;
    private String mPhotoURL;
    private String memailaddress;
    private List<JournalEntry> journals;


    private AccountHeader mHeader = null;
    private Drawer mDrawer = null;

    private static final String ANONYMOUS = "Anonymous";
    public static final String ANONYMOUS_PHOTO_URL = "https://dl.dropboxusercontent.com/u/15447938/notepadapp/anon_user_48dp.png";
    public static final String ANONYMOUS_EMAIL = "anonymous@noemail.com";

    @BindView(android.R.id.content)
    View mRootView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            startActivity(new Intent(this, AuthUiActivity.class));
            finish();
            return;
        } else {
            //user is logged in
            mUsername = mFirebaseUser.getDisplayName();
            //if photourl is not loaded
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoURL = mFirebaseUser.getPhotoUrl().toString();
            }
            memailaddress = mFirebaseUser.getEmail();

        }

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

        setnavigationdrawer(savedInstanceState);
        openFragment(new JournalListFragment(), "Journals");


    }


    public void setnavigationdrawer(Bundle savedInstanceState) {

        mUsername = TextUtils.isEmpty(mUsername) ? ANONYMOUS : mUsername;
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
                        new PrimaryDrawerItem().withName("Journals").withIcon(GoogleMaterial.Icon.gmd_view_list).withIdentifier(Constants.JOURNALS),
                        new PrimaryDrawerItem().withName("Tags").withIcon(GoogleMaterial.Icon.gmd_folder).withIdentifier(Constants.TAGS),
                        new PrimaryDrawerItem().withName("Analytics").withIcon(GoogleMaterial.Icon.gmd_arrow_forward).withIdentifier(Constants.ANALYTICS),
                        new PrimaryDrawerItem().withName("Atlas").withIcon(GoogleMaterial.Icon.gmd_map).withIdentifier(Constants.ATLAS),
                        new PrimaryDrawerItem().withName("Logout").withIcon(GoogleMaterial.Icon.gmd_lock).withIdentifier(Constants.LOGOUT)

                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null && drawerItem instanceof Nameable) {
                            //String name = ((Nameable) drawerItem).getName().getText(mActivity);
                            toolbar.setTitle("Journals");
                        }

                        if (drawerItem != null) {
                            //handle on navigation drawer item
                            onTouchDrawer((int) drawerItem.getIdentifier());
                        }

                        return false;
                    }
                })
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
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
        mDrawer.setSelection(Constants.JOURNALS, false);
    }

    public void onTouchDrawer(int position) {
        switch (position) {
            case Constants.JOURNALS:

                break;
            case Constants.TAGS:
                startActivity(new Intent(MainActivity.this, TagListActivity.class));
                break;
            case Constants.ANALYTICS:
                startActivity(new Intent(MainActivity.this, AnalyticsActivity.class));
                break;
            case Constants.ATLAS:
                startActivity(new Intent(MainActivity.this, AtlasActivity.class));
                break;
            case Constants.LOGOUT:
                logout();
                break;
            case Constants.DELETE:
                deleteAccountClicked();
                break;
        }
    }

    public void logout() {
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
        getSupportActionBar().setTitle(screenTitle);
    }


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
