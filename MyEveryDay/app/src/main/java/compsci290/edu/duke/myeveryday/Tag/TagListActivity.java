package compsci290.edu.duke.myeveryday.Tag;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import compsci290.edu.duke.myeveryday.AnalyticsActivity;
import compsci290.edu.duke.myeveryday.AtlasActivity;
import compsci290.edu.duke.myeveryday.Authentication.AuthUiActivity;
import compsci290.edu.duke.myeveryday.MainActivity;
import compsci290.edu.duke.myeveryday.R;
import compsci290.edu.duke.myeveryday.util.Constants;

/**
 * Created by wangerxiao on 4/23/17.
 */

public class TagListActivity extends AppCompatActivity {

    private String mUsername;
    private String mPhotoURL;
    private String memailaddress;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private AccountHeader mHeader = null;
    private Drawer mDrawer = null;
    private Activity mActivity;

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
        setContentView(R.layout.activity_tag);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        mActivity = this;

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

            //String uid = mFirebaseUser.getUid();
        }


        setnavigationdrawer(savedInstanceState);

    }

    private void setnavigationdrawer(Bundle savedInstanceState) {

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
                        new PrimaryDrawerItem().withName("Tags").withIcon(GoogleMaterial.Icon.gmd_folder).withIdentifier(Constants.CATEGORIES),
                        new PrimaryDrawerItem().withName("Journals").withIcon(GoogleMaterial.Icon.gmd_view_list).withIdentifier(Constants.NOTES),
                        new PrimaryDrawerItem().withName("Analytics").withIcon(GoogleMaterial.Icon.gmd_caret_up_circle).withIdentifier(Constants.ANALYTICS),
                        new PrimaryDrawerItem().withName("Atlas").withIcon(GoogleMaterial.Icon.gmd_map).withIdentifier(Constants.ATLAS),
                        new PrimaryDrawerItem().withName("Logout").withIcon(GoogleMaterial.Icon.gmd_lock).withIdentifier(Constants.LOGOUT)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null && drawerItem instanceof Nameable) {
                            //String name = ((Nameable) drawerItem).getName().getText(mActivity);
                            toolbar.setTitle("Tags");
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
                        KeyboardUtil.hideKeyboard(TagListActivity.this);

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

        openFragment(new TagListFragment(), "Tags");
    }

    public void onTouchDrawer(int position) {
        switch (position) {
            case Constants.NOTES:
                //Do Nothing, we are already on Notes
                startActivity(new Intent(TagListActivity.this, MainActivity.class));
                break;
            case Constants.CATEGORIES:
                break;
            case Constants.ANALYTICS:
                startActivity(new Intent(TagListActivity.this, AnalyticsActivity.class));
                break;
            case Constants.ATLAS:
                startActivity(new Intent(TagListActivity.this, AtlasActivity.class));
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
