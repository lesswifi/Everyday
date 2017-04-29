package compsci290.edu.duke.myeveryday;


import android.app.Activity;
import android.graphics.PointF;
import android.os.Bundle;
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
import compsci290.edu.duke.myeveryday.util.Constants;
import compsci290.edu.duke.myeveryday.util.MyXYSeries;


/**
 * Created by Divya on 4/27/17.
 */
public class AnalyticsActivity extends Activity{

    private XYPlot plot;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mdatabase;
    private DatabaseReference mcloudReference;
    private static ArrayList<JournalEntry> mJournals;


    private TextLabelWidget selectionWidget;

    private Pair<Integer, JournalEntry> selection;
    private MyXYSeries series;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mcloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        setContentView(R.layout.analytics_plot);
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
            System.out.println("Selected: " + selection.second.getmTitle() + "value: " + selection.second.getmSentimentScore());
        }
        //selection.second.getTitle()
        //selection.second.getY(selection.first)
        plot.redraw();
    }

}