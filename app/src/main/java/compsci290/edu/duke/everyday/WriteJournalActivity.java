package compsci290.edu.duke.everyday;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.BreakIterator;
import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * Created by Divya on 4/10/17.
 */


public class WriteJournalActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private static final String TAG = "NewJournalEntryActivity";
    private static final String REQUIRED = "Required";
    // Get the database reference
    private DatabaseReference mDatabase;

    private EditText mTitleField;
    private EditText mTagField;
    private EditText mContentField;
    private FloatingActionButton mSubmitButton;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private BreakIterator mLatitudeText;
    private BreakIterator mLongitudeText;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private int MY_PERMISSIONS_REQUEST = 31*5;

    private String weatherRequestURL = "api.openweathermap.org/data/2.5/weather?lat={";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_quiz);

        //create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        // Initialize the DB Reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mTitleField = (EditText) findViewById(R.id.edit_title);
        mTagField = (EditText) findViewById(R.id.edit_tag);
        mContentField = (EditText) findViewById(R.id.edit_content);
        mSubmitButton = (FloatingActionButton) findViewById(R.id.fab_submit_quiz);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = mTitleField.getText().toString();
                final String tag = mTagField.getText().toString();
                final String content = mContentField.getText().toString();

                if(TextUtils.isEmpty(title)) {
                    mTitleField.setError(REQUIRED);
                    return;
                } else if (TextUtils.isEmpty(tag)) {
                    mTagField.setError(REQUIRED);
                    return;
                }
                setEditingEnabled(false);
                Snackbar.make(v, "Uploading entry...", Snackbar.LENGTH_SHORT).show();

                Calendar calendar = GregorianCalendar.getInstance();
                long date = calendar.getTimeInMillis();
                submitEntry(title, content, tag, date);

                //System.out.println(mLastLocation.toString());
/**                mDatabase.child("journalentries").addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Date date = new Date();
                                submitEntry(title, content, tag, date);
                                setEditingEnabled(true);
                                finish();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(TAG, "onCancelled", databaseError.toException());
                                setEditingEnabled(true);
                            }
                        }
                );  **/
            }
        });


    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    private void setEditingEnabled(boolean value) {
        mTitleField.setEnabled(value);
        mTagField.setEnabled(value);
        mContentField.setEnabled(value);
        if (value) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    private void submitEntry(String title, String content, String tag, long date) {

        // journal entries key
        String key = mDatabase.child("journalentries").push().getKey();
        JournalEntry journalEntry = new JournalEntry(title, content, tag, date);

        mDatabase.child(key).setValue(journalEntry);
        journalEntry.setJournalId(key);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Begin polling for new location updates.
        // Request location updates
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST);
            }
        }
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);


        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        // Get last known recent location.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        // Note that this can be NULL if last location isn't already known.
        if (mLastLocation != null) {
            // Print current location if not null
            Log.d("DEBUG", "current location: " + mLastLocation.toString());
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            weatherRequestURL = weatherRequestURL + mLastLocation.getLatitude() + "}&lon={" +  mLastLocation.getLongitude()+ "}";
        }

    }




    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }
    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
