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
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import compsci290.edu.duke.myeveryday.Tag.TagListActivity;
import compsci290.edu.duke.myeveryday.util.Constants;


/**
 * A simple {@link Fragment} subclass.
 */
public class ToolBarFragment extends Fragment{

    private String mUsername;
    private String mPhotoURL;
    private String memailaddress;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private AccountHeader mHeader = null;
    private Drawer mDrawer = null;
    private Activity mActivity;
    private AppCompatActivity mAppCompatActivity;

    private static final String ANONYMOUS = "Anonymous";
    public static final String ANONYMOUS_PHOTO_URL = "https://dl.dropboxusercontent.com/u/15447938/notepadapp/anon_user_48dp.png";
    public static final String ANONYMOUS_EMAIL = "anonymous@noemail.com";
    private View mRootView;

    private String CURRENT_ACTIVITY = "Current Activity";
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private int SelectedItem;


    @BindView(R.id.tool_bar_item)
    Toolbar toolbar;


    public ToolBarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup ToolBar,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mRootView = inflater.inflate(R.layout.fragment_tool_bar, ToolBar, false);
        ButterKnife.bind(this, mRootView);
        Log.d("toolbar", "oh no");

        mActivity = getActivity();

        mAppCompatActivity = (AppCompatActivity)mActivity;

        //Toolbar toolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);
        //mAppCompatActivity.setSupportActionBar(toolbar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        // If user didn't log in, go back to AuthenticationAcitivity
        //user is logged in and get username

        mUsername = mFirebaseUser.getDisplayName();
        //if photourl is not loaded
        if (mFirebaseUser.getPhotoUrl() != null) {
            mPhotoURL = mFirebaseUser.getPhotoUrl().toString();
        }
        memailaddress = mFirebaseUser.getEmail();

        mAppCompatActivity.setSupportActionBar(toolbar);

        sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
        editor = sp.edit();
        SelectedItem = sp.getInt(CURRENT_ACTIVITY, -1);
        mAppCompatActivity.getSupportActionBar().setTitle(Constants.CURRENT_NAME.get(SelectedItem));

        setnavigationdrawer(savedInstanceState);


        return mRootView;
    }

    public void setnavigationdrawer(Bundle savedInstanceState) {

        mUsername = TextUtils.isEmpty(mUsername) ? ANONYMOUS : mUsername;
        memailaddress = TextUtils.isEmpty(memailaddress) ? ANONYMOUS_EMAIL : memailaddress;
        mPhotoURL = TextUtils.isEmpty(mPhotoURL) ? ANONYMOUS_PHOTO_URL : mPhotoURL;

        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("Journals").withIcon(GoogleMaterial.Icon.gmd_view_list).withIdentifier(Constants.JOURNALS);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withName("Tags").withIcon(GoogleMaterial.Icon.gmd_folder).withIdentifier(Constants.TAGS);
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withName("Analytics").withIcon(GoogleMaterial.Icon.gmd_arrow_forward).withIdentifier(Constants.ANALYTICS);
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withName("Atlas").withIcon(GoogleMaterial.Icon.gmd_map).withIdentifier(Constants.ATLAS);
        PrimaryDrawerItem item5 = new PrimaryDrawerItem().withName("Logout").withIcon(GoogleMaterial.Icon.gmd_lock).withIdentifier(Constants.LOGOUT);

        IProfile profile = new ProfileDrawerItem()
                .withName(mUsername)
                .withEmail(memailaddress)
                .withIcon(mPhotoURL)
                .withIdentifier(102);

        mHeader = new AccountHeaderBuilder()
                .withActivity(mActivity)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(profile)
                .build();

        mDrawer = new DrawerBuilder()
                .withAccountHeader(mHeader)
                .withActivity(mActivity)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withSelectedItem(-1)
                .addDrawerItems(
                        item1,
                        item2,
                        item3,
                        item4,
                        item5
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null && drawerItem instanceof Nameable) {
                            editor.putInt(CURRENT_ACTIVITY, (int) drawerItem.getIdentifier()).commit();
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
                        KeyboardUtil.hideKeyboard(mActivity);

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
        mDrawer.setSelection(SelectedItem, false);

    }

    public void onTouchDrawer(int position) {
        switch (position) {
            case Constants.JOURNALS:
                mActivity.startActivity(new Intent(mActivity, MainActivity.class));
                break;
            case Constants.TAGS:
                mActivity.startActivity(new Intent(mActivity, TagListActivity.class));
                break;
            case Constants.ANALYTICS:
                mActivity.startActivity(new Intent(mActivity, AnalyticsActivity.class));
                break;
            case Constants.ATLAS:
                mActivity.startActivity(new Intent(mActivity, AtlasActivity.class));
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
                .signOut(mActivity)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mActivity.startActivity(new Intent(mActivity, MainActivity.class));
                            mActivity.finish();
                        } else {
                            showSnackbar(R.string.sign_out_failed);
                        }
                    }
                });


    }

    public void deleteAccountClicked() {

        AlertDialog dialog = new AlertDialog.Builder(mActivity)
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
                .delete(mActivity)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mActivity.startActivity(new Intent(mActivity, MainActivity.class));
                            mActivity.finish();
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


}
