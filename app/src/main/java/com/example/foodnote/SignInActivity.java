package com.example.foodnote;

import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.foodnote.backend.models.appEngineUserApi.AppEngineUserApi;
import com.example.foodnote.backend.models.appEngineUserApi.model.AppEngineUser;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = SignInActivity.class.getSimpleName();

    /*
     *  Request codes
     */
    private static final int REQUEST_ACCOUNT_PICKER = 1;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 2;

    /*
     *  Google Account credentials manager
     */
    private static GoogleAccountCredential credential;

    /*
     *  Shared preference to save and load user name
     */
    SharedPreferences sharedPreferences;

    /*
     *  Progress Dialog that indicates progress for this activity
     */
    ProgressDialog mProgressDialog;

    /*
     *  If user already signed in, return to MainActivity
     *  Otherwise, show options for signing in
     */
    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Please wait a moment..");

        if (isSignedIn()) {
            finish();
        }

        com.google.android.gms.common.SignInButton signInButton =
                (com.google.android.gms.common.SignInButton)findViewById(R.id.gmail_sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGmailSignInButtonClicked();
            }
        });
    }

    public static GoogleAccountCredential getCredential() {
        return credential;
    }

    /*
     *  Check if the user is signed in by trying to retrieve information
     *  from shared preferences
     */
    private boolean isSignedIn() {
        credential = GoogleAccountCredential.usingAudience(this, Constants.ANDROID_AUDIENCE_ID);
        String accountName = sharedPreferences.getString(Constants.ACCOUNT_NAME_SETTINGS_NAME, null);
        credential.setSelectedAccountName(accountName);
        return credential.getSelectedAccount() != null;
    }

    /*
     *  Handle the request for clicking Sign in as Gmail button
     *
     *  Prompt for Gmail user authentication
     */
    public void onGmailSignInButtonClicked() {
        new GmailAccountPickerTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ACCOUNT_PICKER) {
            if (resultCode == RESULT_OK) {
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                sharedPreferences.edit()
                        .putString(Constants.ACCOUNT_NAME_SETTINGS_NAME, accountName)
                        .apply();
                credential.setSelectedAccountName(accountName);

                new AppEngineUserTask().execute();
            }
        }
    }

    /*
     *  A task used to start Gmail account login activity in background process
     *  and show progress dailog while starting the activity
     */
    private class GmailAccountPickerTask extends AsyncTask<Void, Void, Void> {

        public void onPreExecute() {
            mProgressDialog.show();
        }

        public Void doInBackground(Void... param) {
            // Uncomment the below code to test the progress dialog
            /*
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Log.i(TAG, "Interrupted!!!!");
            }*/
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            return null;
        }

        public void onPostExecute(Void param) {
            mProgressDialog.dismiss();
        }
    }

    /*
     *  A task used to set up user as App Engine User
     */
    private class AppEngineUserTask extends AsyncTask<Void, Void, Void>  {

        public void onPreExecute() {
            mProgressDialog.show();
        }

        public Void doInBackground(Void... param) {
            AppEngineUserApi appEngineUserApi = CloudEndpointBuilderHelper.getAppEngineUserEndpoints();
            try {
                com.example.foodnote.backend.models.appEngineUserApi.model.AppEngineUser appEngineUser = appEngineUserApi.getUser().execute();
                ActionStateSingleton.getInstance().setAppEngineUserId(appEngineUser.getUser().getUserId());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(Void param) {
            mProgressDialog.dismiss();
            sharedPreferences.edit()
                    .putString(Constants.APP_ENGINE_USER_ID,
                            ActionStateSingleton.getInstance().getAppEngineUserId())
                    .apply();
            finish();
        }
    }
}
