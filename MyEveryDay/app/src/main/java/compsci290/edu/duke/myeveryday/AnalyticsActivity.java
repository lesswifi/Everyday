package compsci290.edu.duke.myeveryday;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
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


/**
 * Created by Divya on 4/27/17.
 */
public class AnalyticsActivity extends Activity {

    private XYPlot plot;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mdatabase;
    private DatabaseReference mcloudReference;
    private static ArrayList<Double> scores;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mcloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        scores = new ArrayList<>();
        mcloudReference.addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot journalsnapshot : dataSnapshot.getChildren()) {
                    JournalEntry journal = journalsnapshot.getValue(JournalEntry.class);
                    Log.d("order in analytics", String.valueOf(journal.getmDateCreated()));
                    scores.add(journal.getmSentimentScore());

                }

                showGraph(scores);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        System.out.println("here");
        System.out.println(scores);
   

    }

    public void showGraph(ArrayList<Double> sentimentScores){
        setContentView(R.layout.analytics_plot);
        System.out.println("mScores");
        System.out.println(sentimentScores);

        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.plot);

        // create a couple arrays of y-values to plot:
        //final Number[] domainLabels = {1};
        List<Integer> domainLabels = new ArrayList<Integer>();
        for(int i = 1; i <= sentimentScores.size(); i++) {
            domainLabels.add(i);
        }
        //Number[] series1Numbers = {1, 4, 2, 8, 4, 16, 8, 32, 16, 64};


        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(
                sentimentScores, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");


        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format =
                new LineAndPointFormatter(this, R.xml.line_point_formatter_with_labels);



        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));


        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);

//        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
//            @Override
//            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
//                int i = Math.round(((Number) obj).floatValue());
//                return toAppendTo.append(domainLabels[i]);
//            }
//            @Override
//            public Object parseObject(String source, ParsePosition pos) {
//                return null;
//            }
//        });
    }

}