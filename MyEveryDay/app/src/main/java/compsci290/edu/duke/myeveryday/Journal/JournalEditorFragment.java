package compsci290.edu.duke.myeveryday.Journal;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import compsci290.edu.duke.myeveryday.Listeners.TagSelectedListener;
import compsci290.edu.duke.myeveryday.MainActivity;
import compsci290.edu.duke.myeveryday.Models.JournalEntry;
import compsci290.edu.duke.myeveryday.Models.Tag;
import compsci290.edu.duke.myeveryday.R;
import compsci290.edu.duke.myeveryday.Services.LatLng;
import compsci290.edu.duke.myeveryday.Services.WeatherService;
import compsci290.edu.duke.myeveryday.Tag.SelectTagFragment;
import compsci290.edu.duke.myeveryday.util.AudioHelper;
import compsci290.edu.duke.myeveryday.util.CameraHelper;
import compsci290.edu.duke.myeveryday.util.Constants;
import compsci290.edu.duke.myeveryday.util.NaturalLanguageTask;
import compsci290.edu.duke.myeveryday.util.OrientationUtils;
import compsci290.edu.duke.myeveryday.util.TimeUtils;
import io.fabric.sdk.android.services.concurrency.AsyncTask;


/**
 * Created by FD on 4/20/17.
 * This is the JournalEditor Fragment. It's called by AddJournalActivity and it opens the
 * EditView for users to write journal. If the intent was created by clicking on the existing journal,
 * the journal information would be passed in, and the inflater would inflate the EditView automatically.
 * If the intent is created by clicking on adding new journal button, it would allow user to create
 * a new journal and save. It also includes Audio, Photo and delete function.
 *
 */

public class JournalEditorFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    @BindView(R.id.action_save)
    FloatingActionButton mSaveButton;

    @BindView(R.id.date_created)
    TextView mDate;

    @BindView(R.id.edit_text_tag)
    EditText mTag;

    @BindView(R.id.edit_text_title)
    EditText mTitle;

    @BindView(R.id.edit_text_journal)
    EditText mContent;

    @Nullable @BindView(R.id.photo_gallery)
    LinearLayout mPhotoGallery;

    @BindView(R.id.audio_playback)
    Button mAudioPlayback;

    private View mRootView;
    private JournalEntry currentJournal = null;
    private Tag currenttag = null;
    private boolean isInEditMode = false;
    private List<Tag> mtags = new ArrayList<>();
    private SelectTagFragment selecttagdialog;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private StorageReference mStorageReference;
    private StorageReference mPhotoReference;
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

    private ArrayList<String> mPhotoPathList = new ArrayList<String>();
    private ArrayList<String> mCloudPhotoPathList = new ArrayList<String>();
    private String mAudioPath;
    private String mPlaybackAudioPath;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private Animation mAnimation;

    static final int EXTERNAL_PERMISSION_REQUEST = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_AUDIO_RECORD = 2;
    static final int ACCESS_COARSE_LOCATION = 3;
    static final int ACCESS_FINE_LOCATION = 4;



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

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSaveContent();
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mcloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        mTagCloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);

        mTagCloudReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot tagsnapshot: dataSnapshot.getChildren())
                {
                    Tag mtag = tagsnapshot.getValue(Tag.class);
                    mtags.add(mtag);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mStorageReference = FirebaseStorage.getInstance().getReference();

        getCurrentNode();

        if (currentJournal != null) {
            mDate.setText(TimeUtils.getReadableModifiedDate(currentJournal.getmDateCreated()));

            mTag.setText(currentJournal.getmTagName());

            mCloudPhotoPathList = (ArrayList<String>) currentJournal.getmImagePaths();
            for (int i = 0; i < mCloudPhotoPathList.size(); i++) {
                populateImage(mCloudPhotoPathList.get(i), true);
            }
            mPlaybackAudioPath = currentJournal.getmAudioPath();
            if (mPlaybackAudioPath != null) {
                AudioHelper.displayDuration(mPlaybackAudioPath, mAudioPlayback);
                mAudioPlayback.setVisibility(View.VISIBLE);
            }
        } else {
            mDate.setText(TimeUtils.getReadableModifiedDate(System.currentTimeMillis()));
        }

        createFlashingButtonAnimation();
        mAudioPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer == null) {
                    mPlayer = new MediaPlayer();
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            OrientationUtils.unlockOrientation(getActivity());
                            AudioHelper.stopPlayback(mPlayer);
                            mPlayer = null;
                            mAudioPlayback.clearAnimation();
                            mAudioPlayback.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_play), null, null, null);
                        }
                    });
                    OrientationUtils.lockOrientation(getActivity());
                    mAudioPlayback.setAnimation(mAnimation);
                    mAudioPlayback.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause), null, null, null);
                    AudioHelper.startPlayback(mPlayer, mPlaybackAudioPath);
                } else {
                    OrientationUtils.unlockOrientation(getActivity());
                    AudioHelper.stopPlayback(mPlayer);
                    mPlayer = null;
                    mAudioPlayback.clearAnimation();
                    mAudioPlayback.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause), null, null, null);
                }
            }
        });
        mAudioPlayback.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(getContext());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View titleView = (View)inflater.inflate(R.layout.dialog_title, null);
                TextView titleText = (TextView)titleView.findViewById(R.id.text_view_dialog_title);
                titleText.setText(getString(R.string.are_you_sure));
                alertDialog.setCustomTitle(titleView);

                alertDialog.setMessage("Delete this audio?");
                alertDialog.setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPlaybackAudioPath = null;
                        mAudioPlayback.setVisibility(View.GONE);
                    }
                });
                alertDialog.setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
                return true;
            }
        });


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

    @OnClick(R.id.edit_text_tag)
    public void showSelectedTag()
    {
        showchosendialog(mtags);
    }

    private void showchosendialog(List<Tag> mtags) {
        selecttagdialog = SelectTagFragment.newInstance();
        selecttagdialog.settags(mtags);

        selecttagdialog.settagselectedlistner(new TagSelectedListener() {
            @Override
            public void onTagSelected(Tag TagSelected) {
                selecttagdialog.dismiss();
                mTag.setText(TagSelected.getmTagName());
                currenttag = TagSelected;
            }

            @Override
            public void onEditButtionClicked(Tag TagSelected) {

            }

            @Override
            public void onDeleteButtonClicked(Tag TagSelected) {

            }
        });

        selecttagdialog.show(getFragmentManager(), "Dialog");

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_note_editor, menu);
        MenuItem delete_item = menu.findItem(R.id.action_delete);

        if (currentJournal == null){
            delete_item.setVisible(false);
        }

        super.onCreateOptionsMenu(menu, inflater);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        PackageManager packageManager = getActivity().getPackageManager();
        switch (item.getItemId()){
            case R.id.action_delete:
                promptForDelete(currentJournal);
                break;
            case R.id.action_camera:
                if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    if (isStoragePermissionGranted()) {
                        launchCamera();
                    }
                }
                break;
            case R.id.action_record:
                if (packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
                    if (isStoragePermissionGranted()) {
                        if (isRecordPermissionGranted()) {
                            launchRecorder();
                        }
                    }
                } else {
                    makeToast("You do not have a microphone");
                }

        }

        return super.onOptionsItemSelected(item);
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        File photoFile;
        try {
            photoFile = CameraHelper.createImageFile(getActivity());
            mPhotoPathList.add(photoFile.getAbsolutePath());
        } catch (IOException e) {
            makeToast("There was a problem saving the picture.");
            return;
        }

        if (photoFile != null) {
            Uri photoUri = FileProvider.getUriForFile(getActivity(),
                    "compsci290.edu.duke.myeveryday.fileprovider",
                    photoFile);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void launchRecorder() {
        OrientationUtils.lockOrientation(getActivity());
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View titleView = (View) inflater.inflate(R.layout.dialog_title, null);
        TextView titleText = (TextView) titleView.findViewById(R.id.text_view_dialog_title);
        titleText.setText("Record Audio");
        alertDialog.setCustomTitle(titleView);

        alertDialog.setPositiveButton("Start", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mRecorder == null) {
                    try {
                        File audioFile = AudioHelper.createAudioFile();
                        mAudioPath = audioFile.getAbsolutePath();
                        mRecorder = new MediaRecorder();
                        AudioHelper.startRecording(mRecorder, mAudioPath);
                        promptToStopRecording();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                OrientationUtils.unlockOrientation(getActivity());
                dialog.dismiss();
            }
        });

        alertDialog.show();


    }

    private void promptToStopRecording() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        alertDialog.setCancelable(false);
        View titleView = (View) inflater.inflate(R.layout.dialog_title, null);
        TextView titleText = (TextView) titleView.findViewById(R.id.text_view_dialog_title);
        titleText.setText("Recording...");
        alertDialog.setCustomTitle(titleView);

        alertDialog.setPositiveButton("Stop", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AudioHelper.stopRecording(mRecorder);
                mRecorder = null;
                mPlaybackAudioPath = mAudioPath;
                AudioHelper.displayDuration(mPlaybackAudioPath, mAudioPlayback);
                mAudioPlayback.setVisibility(View.VISIBLE);
                OrientationUtils.unlockOrientation(getActivity());
                dialog.dismiss();
            }
        });

        alertDialog.show();


    }

    private void createFlashingButtonAnimation() {
        mAnimation = new AlphaAnimation(1, 0);
        mAnimation.setDuration(500);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    String photoPath = mPhotoPathList.get(mPhotoPathList.size() - 1);
                    populateImage(photoPath, false);
                    break;
                case REQUEST_AUDIO_RECORD:
                    launchRecorder();
                    break;

            }
        }
    }

    private void populateImage(final String imagePath, final boolean isCloudImage) {
        final ImageView image = new ImageView(getContext());
        image.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 1000));
        mPhotoGallery.addView(image);
        CameraHelper.displayImageInView(getContext(), imagePath, image);

        // Add delete listener
        image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(getContext());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View titleView = (View)inflater.inflate(R.layout.dialog_title, null);
                TextView titleText = (TextView)titleView.findViewById(R.id.text_view_dialog_title);
                titleText.setText(getString(R.string.are_you_sure));
                alertDialog.setCustomTitle(titleView);

                alertDialog.setMessage("Delete this photo? This cannot be undone.");
                alertDialog.setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPhotoGallery.removeView(image);
                        if (isCloudImage) {
                            mCloudPhotoPathList.remove(imagePath);
                        } else {
                            mPhotoPathList.remove(imagePath);
                        }
                    }
                });
                alertDialog.setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
                return true;
            }
        });
    }




    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_PERMISSION_REQUEST);
                return false;
            }
        }
        else {
            return true;
        }
    }

    private boolean isRecordPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_RECORD);
                return false;
            }
        }
        else {
            return true;
        }
    }

    private void requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

            } else {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_COARSE_LOCATION);

            }
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
            } else {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
            }
        }

    }


    private void promptForDelete(final JournalEntry journal){

        String title = journal.getmTitle();
        String message = "Delete " + title;

        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View titleView = (View)inflater.inflate(R.layout.dialog_title, null);
        TextView titleText = (TextView)titleView.findViewById(R.id.text_view_dialog_title);
        titleText.setText(getString(R.string.are_you_sure));
        alertDialog.setCustomTitle(titleView);

        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TextUtils.isEmpty(journal.getmID())){
                    Task<Void> voidTask = mcloudReference.child(journal.getmID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //if (mNoteFirebaseAdapter.getItemCount() < 1) {
                              //  showEmptyText();
                            //}
                            startActivity(new Intent(getActivity(), MainActivity.class));
                        }
                    });
                }
            }
        });
        alertDialog.setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
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

        AsyncTask saveNote = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                String result = isInEditMode ? "Updating note..." : "Adding note...";
                makeToast(result);
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    addNotetoFirebase();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            };

            @Override
            protected void onPostExecute(Object o) {
                Log.d("validateAndSave", currentJournal.getmImagePaths().toString());
                mcloudReference.child(currentJournal.getmID()).setValue(currentJournal);
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        }.execute();


    }




    private void addNotetoFirebase() throws ExecutionException, InterruptedException {

        if (currentJournal == null){
            currentJournal = new JournalEntry();
            Log.d("addNotetoFirebase", currentJournal.toString());
            String key = mcloudReference.push().getKey();
            currentJournal.setmID(key);
            currentJournal.setmDateCreated(System.currentTimeMillis());
        }

        addImagesToFirebase();
        addAudioToFirebase();

        //Add Tags to Firebase
        if(currenttag != null)
        {
            currentJournal.setmTagID(currenttag.getmTagID());
            currentJournal.setmTagName(currenttag.getmTagName());
        }

        Log.d("Journal editor", mTitle.getText().toString());
        currentJournal.setmTitle(mTitle.getText().toString());

        String contentText = mContent.getText().toString();
        currentJournal.setmContent(contentText);

        currentJournal.setmDateModified(System.currentTimeMillis());
        currentJournal.setmLocation(mAddress);
        currentJournal.setmLatLng(mLatLng);


        WeatherService ws = new WeatherService(mLastLocation);
        String weatherIconUrl = null;
        Double nlpResult = 0.0;
        try {
            weatherIconUrl = ws.getWeatherIcon();
            nlpResult = getNLP(contentText);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        currentJournal.setmWeather(weatherIconUrl);
        currentJournal.setmSentimentScore(nlpResult);
        System.out.println("NLP");
        System.out.println(currentJournal.getmSentimentScore());
    }



    public void addImagesToFirebase() {
        Log.d("addImagesToFirebase", "IN THIS FUNCTION");
        // Updates cloud photos to remove the URLs deleted during editing
        currentJournal.setmImagePaths(mCloudPhotoPathList);

        // Adds local images
        ArrayList<UploadTask> taskList = new ArrayList<UploadTask>();
        for (int i = 0; i < mPhotoPathList.size(); i++) {
            String localPhotoPath = mPhotoPathList.get(i);
            Uri file = Uri.fromFile(new File(localPhotoPath));
            StorageReference imageRef = mStorageReference.child("images/" + file.getLastPathSegment());
            UploadTask uploadTask = imageRef.putFile(file);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    makeToast("There was a problem uploading your photos.");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String uploadedImagePath = taskSnapshot.getDownloadUrl().toString();
                    currentJournal.addmImagePath(uploadedImagePath);
                    Log.d("onSuccess", uploadedImagePath);
                }
            });
            taskList.add(uploadTask);
        }

        try {
            Tasks.await(Tasks.whenAll(taskList));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void addAudioToFirebase() {
        if (mAudioPath == null) {
            if (mPlaybackAudioPath == null) {
                currentJournal.setmAudioPath(null);
            }
            return;
        }

        Uri file = Uri.fromFile(new File(mAudioPath));
        StorageReference audioRef = mStorageReference.child("audio/" + file.getLastPathSegment());
        UploadTask uploadTask = audioRef.putFile(file);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                makeToast("There was a problem uploading your recording.");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String uploadedAudioPath = taskSnapshot.getDownloadUrl().toString();
                currentJournal.setmAudioPath(uploadedAudioPath);
                Log.d("onSuccess", uploadedAudioPath);
            }
        });

        try {
            Tasks.await(uploadTask);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private Double getNLP(String content) throws InterruptedException, ExecutionException{
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
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(mActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            requestLocationPermissions();
        }

        // we can request the permission.
        ActivityCompat.requestPermissions(mActivity,
                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST);

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
        // Create a LatLng Object for use with maps
        com.google.android.gms.maps.model.LatLng latLng = new com.google.android.gms.maps.model.LatLng(location.getLatitude(), location.getLongitude());
    }


    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            makeToast("Disconnected. Please re-connect.");
        } else if (i == CAUSE_NETWORK_LOST) {
            makeToast("Network lost. Please re-connect.");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
