package compsci290.edu.duke.myeveryday;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.androidplot.Region;
import com.androidplot.ui.Size;
import com.androidplot.ui.SizeMode;
import com.androidplot.ui.TextOrientation;
import com.androidplot.ui.widget.TextLabelWidget;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
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
import compsci290.edu.duke.myeveryday.Journals.AddJournalActivity;
import compsci290.edu.duke.myeveryday.Journals.NoteViewHolder;
import compsci290.edu.duke.myeveryday.Models.JournalEntry;
import compsci290.edu.duke.myeveryday.Tag.TagListActivity;
import compsci290.edu.duke.myeveryday.util.CameraHelper;
import compsci290.edu.duke.myeveryday.util.Constants;
import compsci290.edu.duke.myeveryday.util.MyXYSeries;


/**
 * Created by Divya on 4/27/17.
 */
public class AnalyticsActivity extends AppCompatActivity {

    private XYPlot plot;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mdatabase;
    private DatabaseReference mcloudReference;
    private static ArrayList<JournalEntry> mJournals;
    private FloatingActionButton mFab;

    private TextLabelWidget selectionWidget;

    private Pair<Integer, JournalEntry> selection;
    private MyXYSeries series;


    private String mUsername;
    private String mPhotoURL;
    private String memailaddress;

    private AccountHeader mHeader = null;
    private Drawer mDrawer = null;
    private Activity mActivity;

    private static final String ANONYMOUS = "Anonymous";
    public static final String ANONYMOUS_PHOTO_URL = "https://dl.dropboxusercontent.com/u/15447938/notepadapp/anon_user_48dp.png";
    public static final String ANONYMOUS_EMAIL = "anonymous@noemail.com";

    @BindView(android.R.id.content) View mRootView;
    @BindView(R.id.toolbar3)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mcloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        setContentView(R.layout.analytics_plot);

        plotSetup();

        mJournals = new ArrayList<>();
        mcloudReference.addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot journalsnapshot : dataSnapshot.getChildren()) {
                    JournalEntry journal = journalsnapshot.getValue(JournalEntry.class);
                    Log.d("order in analytics", String.valueOf(journal.getmDateCreated()));

                    if(journal.getmSentimentScore() != null) {
                        mJournals.add(journal);
                    }


                }

                showGraph(mJournals);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        mActivity = this;

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

        setnavigationdrawer(savedInstanceState);

    }

    private void setnavigationdrawer(Bundle savedInstanceState) {

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
                        new PrimaryDrawerItem().withName("Analytics").withIcon(GoogleMaterial.Icon.gmd_caret_up_circle).withIdentifier(Constants.ANALYTICS),
                        new PrimaryDrawerItem().withName("Journals").withIcon(GoogleMaterial.Icon.gmd_view_list).withIdentifier(Constants.NOTES),
                        new PrimaryDrawerItem().withName("Tags").withIcon(GoogleMaterial.Icon.gmd_folder).withIdentifier(Constants.CATEGORIES),
                        new PrimaryDrawerItem().withName("Atlas").withIcon(GoogleMaterial.Icon.gmd_map).withIdentifier(Constants.ATLAS),
                        new PrimaryDrawerItem().withName("Logout").withIcon(GoogleMaterial.Icon.gmd_lock).withIdentifier(Constants.LOGOUT)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null && drawerItem instanceof Nameable){
                            //String name = ((Nameable) drawerItem).getName().getText(mActivity);
                            toolbar.setTitle("Analytics");
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
                        KeyboardUtil.hideKeyboard(AnalyticsActivity.this);

                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {

                    }
                })
                //.withFireOnInitialOnClick(true)
                //.withSavedInstance(savedInstanceState)
                .build();
        mDrawer.addStickyFooterItem(new PrimaryDrawerItem().withName("Delete Account!").withIcon(GoogleMaterial.Icon.gmd_delete).withIdentifier(Constants.DELETE));
        mDrawer.setSelection(Constants.ANALYTICS);
    }

    public void onTouchDrawer(int position)
    {
        switch (position){
            case Constants.NOTES:
                //Do Nothing, we are already on Notes
                startActivity(new Intent(AnalyticsActivity.this, MainActivity.class));
                break;
            case Constants.CATEGORIES:
                startActivity(new Intent(AnalyticsActivity.this, TagListActivity.class));
                break;
            case Constants.ANALYTICS:
                break;
            case Constants.LOGOUT:
                logout();
                break;
            case Constants.ATLAS:
                startActivity(new Intent(AnalyticsActivity.this, AtlasActivity.class));
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



    public void plotSetup() {
        plot = (XYPlot) findViewById(R.id.plot);
        PanZoom.attach(plot);
        plot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    onPlotClicked(new PointF(motionEvent.getX(), motionEvent.getY()));
                }
                return true;
            }
        });
        selectionWidget = new TextLabelWidget(plot.getLayoutManager(),
                new Size(
                        PixelUtils.dpToPix(100), SizeMode.ABSOLUTE,
                        PixelUtils.dpToPix(100), SizeMode.ABSOLUTE),
                TextOrientation.HORIZONTAL);

        selectionWidget.getLabelPaint().setTextSize(PixelUtils.dpToPix(16));
    }

    public void showGraph(ArrayList<JournalEntry> journals){

        System.out.println(mJournals);
        System.out.println(journals);
        // initialize our XYPlot reference:

        // create a couple arrays of y-values to plot:
        //final Number[] domainLabels = {1};
        List<Integer> domainLabels = new ArrayList<Integer>();
        for(int i = 1; i <= journals.size(); i++) {
            domainLabels.add(i);
        }
        //Number[] series1Numbers = {1, 4, 2, 8, 4, 16, 8, 32, 16, 64};


        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        series = new MyXYSeries(journals, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Sentiment Analytics");


        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format =
                new LineAndPointFormatter(this, R.xml.line_point_formatter_with_labels);



        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // add a new series' to the xyplot:
        plot.addSeries(series, series1Format);


    }

    private void onPlotClicked(PointF point) {

        // make sure the point lies within the graph area.  we use gridrect
        // because it accounts for margins and padding as well.
        if (plot.getGraph().getGridRect().contains(point.x, point.y)) {
            Number x = plot.getXVal(point.x);
            Number y = plot.getYVal(point.y);
            System.out.println(x);
            System.out.println(y);


            selection = null;
            double xDistance = 0;
            double yDistance = 0;

            // find the closest value to the selection:

            for (int i = 0; i < series.size(); i++) {
                Number thisX = series.getX(i);
                Number thisY = series.getY(i);
                JournalEntry thisJournal = series.getJournal(i);
                if (thisX != null && thisY != null) {
                    double thisXDistance =
                            Region.measure(x, thisX).doubleValue();
                    double thisYDistance =
                            Region.measure(y, thisY).doubleValue();
                    if (selection == null) {
                        selection = new Pair<Integer, JournalEntry>(i, thisJournal);
                        xDistance = thisXDistance;
                        yDistance = thisYDistance;
                    } else if (thisXDistance < xDistance) {
                        selection = new Pair<Integer, JournalEntry>(i, thisJournal);
                        xDistance = thisXDistance;
                        yDistance = thisYDistance;
                    } else if (thisXDistance == xDistance &&
                            thisYDistance < yDistance &&
                            thisY.doubleValue() >= y.doubleValue()) {
                        selection = new Pair<Integer, JournalEntry>(i, thisJournal);
                        xDistance = thisXDistance;
                        yDistance = thisYDistance;
                    }
                }
            }


        } else {
            // if the press was outside the graph area, deselect:
            selection = null;
        }

        if(selection == null) {
            selectionWidget.setText("no selection");
            System.out.println("no selection");
        } else {

            selectionWidget.setText("Selected: " + selection.second.getmTitle() + " value: " + selection.second.getmSentimentScore() );
            System.out.println("Selected: " + selection.second.getmTitle() + " Value: " + selection.second.getmSentimentScore());
            displayJournalPreview(selection.second);


        }
        //selection.second.getTitle()
        //selection.second.getY(selection.first)
        plot.redraw();
    }

    public void displayJournalPreview(final JournalEntry journal) {
        NoteViewHolder holder = new NoteViewHolder(this.findViewById(android.R.id.content));

        holder.title.setText(journal.getmTitle());
        holder.content.setText(journal.getmContent());

        String weatherIconUrl = null;
        if (journal.getmWeather() != null && journal.getmWeather().startsWith("http")) {
            weatherIconUrl = journal.getmWeather();
        }
        CameraHelper.displayImageInView(getApplicationContext(), weatherIconUrl, holder.weather_icon);
        holder.location_weather.setText(journal.getmLocation());

        String imageUrl = null;
        if (!journal.getmImagePaths().isEmpty()) {
            imageUrl = journal.getmImagePaths().get(0);
        }
        CameraHelper.displayImageInView(getApplicationContext(), imageUrl, holder.photo);
        holder.photo.setMaxHeight(400);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setVisibility(View.VISIBLE);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gson gson = new Gson();
                String serializedJournal = gson.toJson(journal);
                Intent editIntent = new Intent(getApplicationContext(), AddJournalActivity.class);
                editIntent.putExtra(Constants.SERIALIZED_NOTE, serializedJournal);
                startActivity(editIntent);
            }
        });


    }

}