package uk.porcheron.cocurator.activity;

import uk.porcheron.ace.AceImplementation;
import uk.porcheron.cocurator.CoCurator;

import android.app.Activity;
import android.os.Bundle;

import uk.porcheron.cocurator.R;
import uk.porcheron.ace.log.Log;

/**
 * Display one or more timelines to the user.
 */
public class TimelineActivity extends Activity {

    /** Logging. */
    public static final String TAG = CoCurator.TAG + ":TLActivity";

    /** Application instance. */
    private CoCurator mApplication;

    /** Android Collocated Experiences instance. */
    private AceImplementation mAce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timeline);

        mApplication = (CoCurator) getApplication();
        mAce = mApplication.getAce();

        Log.v(TAG, "Created Timeline Activity");
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start Ace discovery
        if(mAce.discoveryPossible(this)) {
            mAce.start();
        } else {
            Log.e(TAG, "Device discovery not possible!");
        }
    }

}
