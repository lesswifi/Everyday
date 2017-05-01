package compsci290.edu.duke.myeveryday.Tag;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import compsci290.edu.duke.myeveryday.Models.Tag;
import compsci290.edu.duke.myeveryday.R;
import compsci290.edu.duke.myeveryday.util.Constants;

/**
 * This fragment is called by
 */
public class AddTagFragment extends DialogFragment {
    private EditText mTagEditText;
    private boolean mInEditMode = false;
    private Tag mtag;
    private DatabaseReference mdatabase;
    private DatabaseReference mTagCloudReference;
    private FirebaseAuth mFirebasAuth;
    private FirebaseUser mFirebaseUser;
    private FloatingActionButton mFab;


    public AddTagFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mFirebasAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebasAuth.getCurrentUser();
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mTagCloudReference = mdatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);


    }

    public static AddTagFragment newInstatnce(String content)
    {
        AddTagFragment dialogFragment = new AddTagFragment();
        Bundle args = new Bundle();

        if (!content.isEmpty()){
            args.putString(Constants.SERIALIZED_CATEGORY, content);
            dialogFragment.setArguments(args);
        }

        return dialogFragment;


    }

    public void getCurrentTag(){
        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.SERIALZED_CATEGORY)){
            String serializedCategory = args.getString(Constants.SERIALIZED_CATEGORY, "");
            if (!TextUtils.isEmpty(serializedCategory)){
                Gson gson = new Gson();
                mtag = gson.fromJson(serializedCategory, new TypeToken<Tag>(){}.getType());
                if (mtag != null & !TextUtils.isEmpty(mtag.getmTagID())){
                    mInEditMode = true;
                }
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final AlertDialog.Builder addtagdialog = new AlertDialog.Builder(getActivity());

        if(savedInstanceState == null)
        {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.fragment_add_tag, null);
            addtagdialog.setView(view);
            getCurrentTag();

            View titleView = (View)inflater.inflate(R.layout.dialog_title, null);
            TextView titleText = (TextView)titleView.findViewById(R.id.text_view_dialog_title);
            titleText.setText(mInEditMode == true ? getString(R.string.edit_category) : getString(R.string.add_category));
            addtagdialog.setCustomTitle(titleView);

            mTagEditText = (EditText)view.findViewById(R.id.edit_text_add_category);

            addtagdialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            addtagdialog.setPositiveButton(mInEditMode == true ? "Update" : "Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            if(mInEditMode){
                populatefields(mtag);
                addtagdialog.setTitle(mtag.getmTagName());
            }


        }

            return addtagdialog.create();
        }

    private void populatefields(Tag mtag) {
        mTagEditText.setText(mtag.getmTagName());
    }

    private boolean requiredFieldCompleted()
    {
        if(mTagEditText.getText().toString().isEmpty())
        {
            mTagEditText.setError("The name of the rag is required");
            mTagEditText.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if(dialog != null)
        {
            Button postiveButton = (Button) dialog.getButton(Dialog.BUTTON_POSITIVE);
            postiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean toclosedialog = false;
                    if(requiredFieldCompleted())
                    {
                        saveTag();
                        toclosedialog = true;
                    }
                    if(toclosedialog)
                    {
                        dismiss();
                    }

                }
            });
        }
    }

    public void saveTag()
    {
            if(mtag != null)
            {
                mtag.setmTagName(mTagEditText.getText().toString().trim());
                //update values in firebase
                mTagCloudReference.child(mtag.getmTagID()).setValue(mtag);
            }
            else
            {
                Tag tag = new Tag();
                tag.setmTagName(mTagEditText.getText().toString().trim());
                tag.setmTagID(mTagCloudReference.push().getKey());
                mTagCloudReference.child(tag.getmTagID()).setValue(tag);
            }

    }


}
