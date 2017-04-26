package compsci290.edu.duke.myeveryday.notes;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import compsci290.edu.duke.myeveryday.MainActivity;
import compsci290.edu.duke.myeveryday.Models.JournalEntry;
import compsci290.edu.duke.myeveryday.R;
import compsci290.edu.duke.myeveryday.Services.LatLng;
import compsci290.edu.duke.myeveryday.Services.WeatherService;
import compsci290.edu.duke.myeveryday.util.Constants;
import compsci290.edu.duke.myeveryday.util.NaturalLanguageTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class JournalEditorFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    @BindView(R.id.edit_text_tag)
    EditText mTag;

    @BindView(R.id.edit_text_title)
    EditText mTitle;

    @BindView(R.id.edit_text_journal)
    EditText mContent;

    private View mRootView;
    private JournalEntry currentJournal = null;
    private boolean isInEditMode = false;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mdatabase;
    private DatabaseReference mcloudReference;
    private DatabaseReference mTagCloudReference;
    private GoogleApiClient mGoogleApiClient;
    private Activity mActivity;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private BreakIterator mLatitudeText;
    private BreakIterator mLongitudeText;
    private String mAddress;
    private LatLng mLatLng;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private int MY_PERMISSIONS_REQUEST = 31 * 5;

    public JournalEditorFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mActivity = getActivity();


        //create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public static JournalEditorFragment newInstance(String content){
        JournalEditorFragment fragment = new JournalEditorFragment();
        if (!TextUtils.isEmpty(content)){
            Bundle args = new Bundle();
            args.putString(Constants.SERIALIZED_NOTE, content);
            fragment.setArguments(args);
        }

        return fragment;
    }

    public void getCurrentNode(){
        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.SERIALIZED_NOTE)){
            String serializedJournal = args.getString(Constants.SERIALIZED_NOTE, "");
            if (!serializedJournal.isEmpty()){
                Gson gson = new Gson();
                currentJournal = gson.fromJson(serializedJournal, JournalEntry.class);
                if (currentJournal != null & !TextUtils.isEmpty(currentJournal.getmID())){
                    isInEditMode = true;
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_journal_editor, container, false);
        ButterKnife.bind(this, mRootView);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mcloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        mTagCloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);

        getCurrentNode();
        return mRootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (isInEditMode){
            populateNode(currentJournal);
        }
    }

    private void populateNode(JournalEntry journal) {
        mTitle.setText(journal.getmTitle());
        mTitle.setHint(getString(R.string.placeholder_note_title));
        mContent.setText(journal.getmContent());
        mContent.setHint(getString(R.string.placeholder_note_text));
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_note_editor, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_save:
                validateAndSaveContent();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void validateAndSaveContent() {
        String title = mTitle.getText().toString();

        if (TextUtils.isEmpty(title)){
            mTitle.setError(getString(R.string.title_is_required));
            return;
        }


        String content = mContent.getText().toString();
        if (TextUtils.isEmpty(content)){
            mContent.setError(getString(R.string.note_is_required));
            return;
        }

        addNotetoFirebase();

    }

    private void addNotetoFirebase()  {

        if (currentJournal == null){
            currentJournal = new JournalEntry();
            String key = mcloudReference.push().getKey();
            currentJournal.setmID(key);

        }
        Log.d("Journal editor", mTitle.getText().toString());
        currentJournal.setmTitle(mTitle.getText().toString());
        String contentText = mContent.getText().toString();
        currentJournal.setmContent(contentText);
        currentJournal.setmDateCreated(System.currentTimeMillis());
        currentJournal.setmDateModified(System.currentTimeMillis());
        currentJournal.setmLocation(mAddress);
        currentJournal.setmLatLng(mLatLng);
        Log.d("JournalEditorFragment",mAddress);
        WeatherService ws = new WeatherService(mLastLocation);
        String weather = null;
        List<Double> nlpResult = null;
        try {
            weather = ws.getWeather();
            nlpResult = getNLP(contentText);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        currentJournal.setmWeather(weather);
        Log.d("JournalEditorFragment",weather);
        System.out.println("NLP");
        System.out.println(nlpResult);

        mcloudReference.child(currentJournal.getmID()).setValue(currentJournal);

        String result = isInEditMode ? "Note updated" : "Note added";
        makeToast(result);
        startActivity(new Intent(getActivity(), MainActivity.class));

    }

    private List<Double> getNLP(String content) throws InterruptedException, ExecutionException{
        NaturalLanguageTask rd = new NaturalLanguageTask(content);
        rd.execute();
        return rd.get();
    }


    private void makeToast(String message){
        Snackbar snackbar = Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG);

        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary));
        TextView tv = (TextView)snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Begin polling for new location updates.
        // Request location updates
        if (ContextCompat.checkSelfPermission(mActivity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // we can request the permission.
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST);
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

            Geocoder geocoder = new Geocoder(mActivity, Locale.getDefault());
            try {
                List<Address> address = geocoder.getFromLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude(),1);
                mAddress = address.get(0).getThoroughfare().toString();
                Log.d("JournalEditorFragment", mAddress);
                mLatLng = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }


    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        com.google.android.gms.maps.model.LatLng latLng = new com.google.android.gms.maps.model.LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            //Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            //Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
