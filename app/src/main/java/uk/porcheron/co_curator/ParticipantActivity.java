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
    private EditText mUserIdField;
    private Button mButtonSignIn;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant);

        mSharedPreferences = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
        mGlobalUserIdField = (EditText) findViewById(R.id.globalUserId);
        mUserIdField = (EditText) findViewById(R.id.localUserId);

        mUserIdField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

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
        mUserIdField.setError(null);

        // Store values at the time of the login attempt.
        String globalUserIdS = mGlobalUserIdField.getText().toString();
        String userIdS = mUserIdField.getText().toString();

        // Validate data
        boolean cancel = false;
        View focusView = null;

        int globalUserId = -1, userId = -1;

        try {
            globalUserId = Integer.parseInt(globalUserIdS);
        } catch(Exception e) {
            mGlobalUserIdField.setError(getString(R.string.error_global_id));
            focusView = mGlobalUserIdField;
            cancel = true;
        }

        try {
            userId = Integer.parseInt(userIdS);
        } catch(Exception e) {
            mUserIdField.setError(getString(R.string.error_local_id));
            focusView = mUserIdField;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(globalUserId, userId);
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
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final int mGlobalUserId;
        private final int mUserId;
        private String mErrorMessage;

        UserLoginTask(int globalUserId, int userId) {
            mGlobalUserId = globalUserId;
            mUserId = userId;
            mErrorMessage = getString(R.string.error_login);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("globalUserId", "" + mGlobalUserId));
            nameValuePairs.add(new BasicNameValuePair("userId", "" + mUserId));

            JSONObject response = Web.requestObj(Web.LOGIN, nameValuePairs);
            if(response != null && response.has("success")) {
                try {
                    return response.getInt("groupId");
                } catch (JSONException e) {
                    Log.e(TAG, "Login failed: Did not receive groupId");
                    e.printStackTrace();
                    return -1;
                }
            } else if(response.has("error")) {
                try {
                    mErrorMessage = response.getString("error");
                    Log.e(TAG, "Login failed: " + response.getString("error"));
                } catch (JSONException e) {
                    Log.e(TAG, "Login failed: Did not receive error response!");
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Login failed: Unknown Login Error!");
            }

            return -1;
        }

        @Override
        protected void onPostExecute(final Integer groupId) {
            mAuthTask = null;
            showProgress(false);

            if (groupId >= 0) {
                Instance.globalUserId = mGlobalUserId;
                Instance.userId = mUserId;
                Instance.groupId = groupId;

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(getString(R.string.pref_globalUserId), mGlobalUserId);
                editor.putInt(getString(R.string.pref_userId), mUserId);
                editor.putInt(getString(R.string.pref_groupId), groupId);
                editor.commit();

                Intent intent = new Intent(ParticipantActivity.this, TimelineActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(ParticipantActivity.this, mErrorMessage, Toast.LENGTH_SHORT).show();
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

