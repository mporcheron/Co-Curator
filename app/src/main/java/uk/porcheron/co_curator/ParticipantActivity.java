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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import uk.porcheron.co_curator.util.IData;

/**
 * A login screen that offers login via email/password.
 */
public class ParticipantActivity extends Activity {

    private UserLoginTask mAuthTask = null;

    private SharedPreferences mSharedPreferences;

    private EditText mGlobalUserIdField;
    private EditText mUserIdField;
    private Button mButtonSignIn;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
        int globalUserId = mSharedPreferences.getInt(getString(R.string.pref_globalUserId), -1);
        int userId = mSharedPreferences.getInt(getString(R.string.pref_userId), -1);
        if(globalUserId >= 0 && userId >= 0) {
            IData.globalUserId = globalUserId;
            IData.userId = userId;

            Intent intent = new Intent(ParticipantActivity.this, TimelineActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_participant);

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
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final int mGlobalUserId;
        private final int mUserId;

        UserLoginTask(int globalUserId, int userId) {
            mGlobalUserId = globalUserId;
            mUserId = userId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mEmail)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }

            // TODO: register the new account here.

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                IData.globalUserId = mGlobalUserId;
                IData.userId = mUserId;

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(getString(R.string.pref_globalUserId), mGlobalUserId);
                editor.putInt(getString(R.string.pref_userId), mUserId);
                editor.commit();

                Intent intent = new Intent(ParticipantActivity.this, TimelineActivity.class);
                startActivity(intent);
                finish();
            } else {
                mGlobalUserIdField.setError(getString(R.string.error_global_id));
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

