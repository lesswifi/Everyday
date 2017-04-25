package compsci290.edu.duke.myeveryday.notes;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.PathInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import compsci290.edu.duke.myeveryday.MainActivity;
import compsci290.edu.duke.myeveryday.Models.JournalEntry;
import compsci290.edu.duke.myeveryday.R;
import compsci290.edu.duke.myeveryday.util.AudioHelper;
import compsci290.edu.duke.myeveryday.util.CameraHelper;
import compsci290.edu.duke.myeveryday.util.Constants;
import compsci290.edu.duke.myeveryday.util.FileUtils;
import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class JournalEditorFragment extends Fragment {

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
    private boolean isInEditMode = false;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private StorageReference mStorageReference;
    private StorageReference mPhotoReference;
    private DatabaseReference mdatabase;
    private DatabaseReference mcloudReference;
    private DatabaseReference mTagCloudReference;

    private ArrayList<String> mPhotoPathList = new ArrayList<String>();
    private ArrayList<String> mCloudPhotoPathList = new ArrayList<String>();
    private String mAudioPath;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private Animation mAnimation;

    static final int EXTERNAL_PERMISSION_REQUEST = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_AUDIO_RECORD = 2;


    public JournalEditorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        mStorageReference = FirebaseStorage.getInstance().getReference();

        getCurrentNode();

        if (currentJournal != null) {
            mCloudPhotoPathList = (ArrayList<String>) currentJournal.getmImagePaths();
            for (int i = 0; i < mCloudPhotoPathList.size(); i++) {
                populateImage(mCloudPhotoPathList.get(i), true);
            }
            if (currentJournal.getmAudioPath() != null) {
                mAudioPlayback.setVisibility(View.VISIBLE);
            }
        }

        mAudioPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    mAudioPlayback.setText("Stop");
                    AudioHelper.startPlayback(mPlayer, mAudioPath);
                } else {
                    AudioHelper.stopPlayback(mPlayer);
                    mPlayer = null;
                    mAudioPlayback.setText("Play");
                }
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


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_note_editor, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        PackageManager packageManager = getActivity().getPackageManager();
        switch (item.getItemId()){
            case R.id.action_save:
                validateAndSaveContent();
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
                dialog.dismiss();
            }
        });

        alertDialog.show();


    }

    private void promptToStopRecording() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View titleView = (View) inflater.inflate(R.layout.dialog_title, null);
        TextView titleText = (TextView) titleView.findViewById(R.id.text_view_dialog_title);
        titleText.setText("Audio Recorder");
        alertDialog.setCustomTitle(titleView);

        alertDialog.setPositiveButton("Stop", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AudioHelper.stopRecording(mRecorder);
                mRecorder = null;
                mAudioPlayback.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });

        alertDialog.show();


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

    private void populateImage(String imagePath, boolean isCloudImage) {
        ImageView image = new ImageView(getContext());
        mPhotoGallery.addView(image);
        CameraHelper.displayImageInView(getContext(), imagePath, image);
        if (isCloudImage) {
            // delete button removes image view
            // and removes file path from mCloudPhotoPathList
        } else {
            // delete button removes image view
        }
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

        try {
            addNotetoFirebase();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void addNotetoFirebase() throws ExecutionException, InterruptedException {

        if (currentJournal == null){
            currentJournal = new JournalEntry();
            Log.d("addNotetoFirebase", currentJournal.toString());
            String key = mcloudReference.push().getKey();
            currentJournal.setmID(key);
        }

        currentJournal.setmTitle(mTitle.getText().toString());
        currentJournal.setmContent(mContent.getText().toString());
        currentJournal.setmDateCreated(System.currentTimeMillis());
        currentJournal.setmDateModified(System.currentTimeMillis());

        AsyncTask addMedia = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                addImagesToFirebase();
                addAudioToFirebase();
                return null;
            };

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                mcloudReference.child(currentJournal.getmID()).setValue(currentJournal);
                String result = isInEditMode ? "Note updated" : "Note added";
                makeToast(result);
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        }.execute();

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

    private void makeToast(String message){
        Snackbar snackbar = Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG);

        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary));
        TextView tv = (TextView)snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();
    }


}
