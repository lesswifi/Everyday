package compsci290.edu.duke.myeveryday;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import compsci290.edu.duke.myeveryday.Journal.AddJournalActivity;
import compsci290.edu.duke.myeveryday.Models.JournalEntry;
import compsci290.edu.duke.myeveryday.util.Constants;

public class AtlasActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mdatabase;
    private DatabaseReference mcloudReference;
    private static ArrayList<JournalEntry> mJournals;
    private HashMap<String, JournalEntry> markerMap = new HashMap<String, JournalEntry>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mcloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);

        mJournals = new ArrayList<>();
        mcloudReference.addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot journalsnapshot : dataSnapshot.getChildren()) {
                    JournalEntry journal = journalsnapshot.getValue(JournalEntry.class);
                    if(journal.getmLatLng() != null) {
                        mJournals.add(journal);
                    }

                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setContentView(R.layout.activity_atlas);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(JournalEntry journal : mJournals) {
            // get the marker Id as String
            String id =  mMap.addMarker(getMarkerForObject(journal)).getId();
            //add the marker ID to Map this way you are not holding on to  GoogleMap object
            markerMap.put(id, journal);
            builder.include(getLatLng(journal));
        }
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        mMap.animateCamera(cu);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Gson gson = new Gson();
                System.out.println(markerMap.get(marker.getId()));
                String serializedJournal = gson.toJson(markerMap.get(marker.getId()));
                Intent editIntent = new Intent(getApplicationContext(), AddJournalActivity.class);
                editIntent.putExtra(Constants.SERIALIZED_NOTE, serializedJournal);
                startActivity(editIntent);


            }
        });


    }


    public MarkerOptions getMarkerForObject(JournalEntry journal) {
        MarkerOptions marker = new MarkerOptions().position(getLatLng(journal)).title(journal.getmTitle());
        return marker;
    }

    public com.google.android.gms.maps.model.LatLng getLatLng(JournalEntry journal) {
        com.google.android.gms.maps.model.LatLng location = new com.google.android.gms.maps.model.LatLng(journal.getmLatLng().getLatitude(), journal.getmLatLng().getLongitude());
        return location;
    }



}
