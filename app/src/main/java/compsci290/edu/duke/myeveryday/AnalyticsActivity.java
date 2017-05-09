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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.androidplot.xy.BoundaryMode;
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
import compsci290.edu.duke.myeveryday.Journal.AddJournalActivity;
import compsci290.edu.duke.myeveryday.Journal.JournalViewHolder;
import compsci290.edu.duke.myeveryday.Models.JournalEntry;
import compsci290.edu.duke.myeveryday.Tag.TagListActivity;
import compsci290.edu.duke.myeveryday.util.CameraHelper;
import compsci290.edu.duke.myeveryday.util.Constants;
import compsci290.edu.duke.myeveryday.util.MyXYSeries;


/**
 * Created by Divya on 4/27/17.
 *
 * AnalyticsActivity displays the sentiment analysis data received from IBM Watson's Natural Language Understanding,
 * and allows users to view the corresponding journal entries from a graph.
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

    @BindView(android.R.id.content)
    View mRootView;

    @BindView(R.id.tool_bar_item)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mcloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        setContentView(R.layout.analytics_plot);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar_item);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

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

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        openToolBar(new ToolBarFragment(), "Analytics");

    }


    @Override
    protected void onResume()
    {
        super.onResume();
    }

    private void openToolBar(Fragment fragment, String screenTitle) {
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.tool_bar, fragment)
                .addToBackStack(screenTitle)
                .commit();
    }



    public void plotSetup() {
        plot = (XYPlot) findViewById(R.id.plot);
        plot.setRangeBoundaries(-1, 1, BoundaryMode.FIXED);

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

        List<Integer> domainLabels = new ArrayList<Integer>();
        for(int i = 1; i <= journals.size(); i++) {
            domainLabels.add(i);
        }

        // Turn the above arrays into XYSeries':
        series = new MyXYSeries(journals, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Sentiment Analytics");

        // Create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format =
                new LineAndPointFormatter(this, R.xml.line_point_formatter_with_labels);

        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // Add a new series' to the xyplot:
        plot.addSeries(series, series1Format);


    }

    private void onPlotClicked(PointF point) {

        // Make sure the point lies within the graph area.  we use gridrect
        // because it accounts for margins and padding as well.
        if (plot.getGraph().getGridRect().contains(point.x, point.y)) {
            Number x = plot.getXVal(point.x);
            Number y = plot.getYVal(point.y);
            System.out.println(x);
            System.out.println(y);


            selection = null;
            double xDistance = 0;
            double yDistance = 0;

            // Find the closest value to the selection:

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
            // If the press was outside the graph area, deselect:
            selection = null;
        }

        if(selection != null) {
            displayJournalPreview(selection.second);
        }

        plot.redraw();
    }

    public void displayJournalPreview(final JournalEntry journal) {
        JournalViewHolder holder = new JournalViewHolder(this.findViewById(android.R.id.content));

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