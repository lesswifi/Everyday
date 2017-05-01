package compsci290.edu.duke.myeveryday.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig;
import com.firebase.ui.auth.ui.ResultCodes;

import butterknife.ButterKnife;
import butterknife.OnClick;
import compsci290.edu.duke.myeveryday.MainActivity;
import compsci290.edu.duke.myeveryday.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
/**
 * Created by yx78 on 4/17/17.
 * This is the signin activity. We use FirebaseUI libaray for firebase authentication.
 * We allow users to log in and sign up through Google accounts
 */

public class AuthUiActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;

    // Use ButterKnife API to bind view
    @BindView(android.R.id.content)
    View mRootView;

    @BindView(R.id.sign_in)
    Button mSignIn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check if user has already logged in
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            //if already logged in, go to mainactivity
            startActivity(new Intent(AuthUiActivity.this, MainActivity.class));
            finish();
        }
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.sign_in)
    public void signIn(View view) {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setTheme(R.style.AppTheme)
                        .setProviders(getSelectedProviders())
                        .build(),
                RC_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            return;
        }

        showSnackbar(R.string.unknown_response);
    }

    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //if login is successful, go to mainactivity
            startActivity(new Intent(AuthUiActivity.this, MainActivity.class));
            finish();
            return;
        }

        if (resultCode == RESULT_CANCELED) {
            //if login failed, display this message
            showSnackbar(R.string.sign_in_cancelled);
            return;
        }

        if (resultCode == ResultCodes.RESULT_NO_NETWORK) {
            // if there's no internet connection, display the message below
            showSnackbar(R.string.no_internet_connection);
            return;
        }

        showSnackbar(R.string.unknown_sign_in_response);
    }




    @MainThread
    private List<IdpConfig> getSelectedProviders() {
        List<IdpConfig> selectedProviders = new ArrayList<>();
        // set configuration providers to google
        // and allow users to sign in through google
        selectedProviders.add(
                new IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                        .setPermissions(getGooglePermissions())
                        .build());

        return selectedProviders;
    }


    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    @MainThread
    private List<String> getGooglePermissions() {
        List<String> result = new ArrayList<>();
        return result;
    }

}