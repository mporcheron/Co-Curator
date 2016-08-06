package uk.porcheron.co_curator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.porcheron.co_curator.util.SoUtils;
import uk.porcheron.co_curator.val.Collo;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.util.Web;

/**
 * A login screen that offers login via email/password.
 */
public class ParticipantActivity extends Activity {
    private static final String TAG = "CC:ParticipantActivity";

    private SharedPreferences mSharedPreferences;

    private UserLoginTask mAuthTask = null;

    private EditText mGlobalUserIdField;
    private EditText mGroupIdField;
    private EditText mServerAddressField;
    private Button mButtonSignIn;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant);

        mSharedPreferences = getSharedPreferences(getString(R.string.prefFile), Context.MODE_PRIVATE);
        mGlobalUserIdField = (EditText) findViewById(R.id.globalUserId);
        //mGroupIdField = (EditText) findViewById(R.id.groupId);
        mServerAddressField = (EditText) findViewById(R.id.serverAddress);

        mGlobalUserIdField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.prefFile), Context.MODE_PRIVATE);
        int globalUserId = sharedPrefs.getInt(getString(R.string.prefGlobalUserId), -1);

        if(globalUserId >= 0) {
            mGlobalUserIdField.setText("" + globalUserId);
        }

        mGlobalUserIdField.requestFocus();

        mButtonSignIn = (Button) findViewById(R.id.buttonSignIn);
        mButtonSignIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mGlobalUserIdField.setError(null);
        //mGroupIdField.setError(null);

        // Store values at the time of the login attempt.
        String globalUserIdS = mGlobalUserIdField.getText().toString();
        //String groupIdS = mGroupIdField.getText().toString();
        String serverAddress = mServerAddressField.getText().toString();
        Log.v(TAG, serverAddress);

        // Validate data
        boolean cancel = false;
        View focusView = null;

        int globalUserId = -1, groupId = -1;

        try {
            globalUserId = Integer.parseInt(globalUserIdS);
        } catch(Exception e) {
            mGlobalUserIdField.setError(getString(R.string.errorGlobalUserid));
            focusView = mGlobalUserIdField;
            cancel = true;
        }

        groupId=1;
//        try {
//            groupId = Integer.parseInt(groupIdS);
//        } catch(Exception e) {
//            mGroupIdField.setError(getString(R.string.errorGroupId));
//            focusView = mGroupIdField;
//            cancel = true;
//        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(globalUserId, groupId, serverAddress);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final int mGlobalUserId;
        private final int mGroupId;
        private String mErrorMessage;
        private int mUserId = -1;
        private final String mServerAddress;


        UserLoginTask(int globalUserId, int groupId, String serverAddress) {
            mGlobalUserId = globalUserId;
            mGroupId = groupId;
            mErrorMessage = getString(R.string.errorLogin);
            mServerAddress = serverAddress;

            Instance.serverAddress = mServerAddress;
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.d(TAG, "Begin login to " + Web.LOGIN);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("globalUserId", "" + mGlobalUserId));
            nameValuePairs.add(new BasicNameValuePair("groupId", "" + mGroupId));
            nameValuePairs.add(new BasicNameValuePair("ip", SoUtils.getIPAddress(true)));

            String mesg = getString(R.string.errorLogin);

            JSONObject response = Web.requestObj(Web.LOGIN, nameValuePairs);
            if(response != null && response.has("success")) {
                try {
                    mUserId = response.getInt("userId");
                } catch (JSONException e) {
                    mesg += ": Did not receive user ID";
                    Log.e(TAG, "Login Failed: " + mesg);
                    return mesg;
                }
                return null;
            } else if(response != null && response.has("error")) {
                try {
                    mesg += ": " + response.getString("error");
                    Log.e(TAG, "Login Failed: " + mesg);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Login Failed: Did not receive error response!");
                }
            } else {
                Log.e(TAG, "Login Failed: Unknown Login Error!");
            }

            return mesg;
        }

        @Override
        protected void onPostExecute(final String response) {
            Log.d(TAG, "Login response processed");

            mAuthTask = null;
            showProgress(false);

            if (response == null) {
                Instance.globalUserId = mGlobalUserId;
                Instance.groupId = mGroupId;
                Instance.userId = mUserId;
                Instance.serverAddress = mServerAddress;


                SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.prefFile), Context.MODE_PRIVATE);
                int globalUserId = sharedPrefs.getInt(getString(R.string.prefGlobalUserId), -1);

                if(globalUserId >= 0 && globalUserId != mGlobalUserId) {
                    // flush DB
                }

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(getString(R.string.prefGlobalUserId), mGlobalUserId);
                editor.putInt(getString(R.string.prefGroupId), mGroupId);
                editor.putInt(getString(R.string.prefUserId), mUserId);
                editor.putString(getString(R.string.prefServerAddress), mServerAddress);
                editor.commit();

                Log.d(TAG, "You are user " + mUserId);

                Intent intent = new Intent(ParticipantActivity.this, TimelineActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(ParticipantActivity.this, response, Toast.LENGTH_SHORT).show();
                mGlobalUserIdField.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

