package compsci290.edu.duke.myeveryday.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;


import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig;
import com.firebase.ui.auth.ui.ResultCodes;

import compsci290.edu.duke.myeveryday.MainActivity;
import compsci290.edu.duke.myeveryday.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
/**
 * Created by wangerxiao on 4/17/17.
 */

public class AuthUiActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;


    @BindView(android.R.id.content)
    View mRootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(AuthUiActivity.this, MainActivity.class));
            finish();
        }

        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        //.setTheme(AuthUI.getDefaultTheme())
                        //.setLogo(getSelectedLogo())
                        .setProviders(getSelectedProviders())
                        //.setTosUrl(getSelectedTosUrl())
                        //.setIsSmartLockEnabled(mEnableSmartLock.isChecked())
                        .build(),
                RC_SIGN_IN);

        Log.d("selected provider", getSelectedProviders()+"");
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
            startActivity(new Intent(AuthUiActivity.this, MainActivity.class));
            //startActivity(SignedInActivity.createIntent(this, IdpResponse.fromResultIntent(data)));
            finish();
            return;
        }

        if (resultCode == RESULT_CANCELED) {
            showSnackbar(R.string.sign_in_cancelled);
            return;
        }

        if (resultCode == ResultCodes.RESULT_NO_NETWORK) {
            showSnackbar(R.string.no_internet_connection);
            return;
        }

        showSnackbar(R.string.unknown_sign_in_response);
    }




    @MainThread
    private List<IdpConfig> getSelectedProviders() {
        List<IdpConfig> selectedProviders = new ArrayList<>();

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