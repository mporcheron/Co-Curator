package uk.porcheron.cocurator;

import android.app.Application;

import uk.porcheron.ace.AceFramework;
import uk.porcheron.ace.AceImplementation;
import uk.porcheron.ace.ble.BleImplementation;
import uk.porcheron.ace.log.Log;
import uk.porcheron.ace.log.LogCat;

/**
 * Shared utilities for use throughout the application.
 */
public class CoCurator extends Application {

    /** Logging. */
    public static final String TAG = "CoCurator";

    /** Instance of the implementation for the Android Collocated Experiences framework. */
    private AceImplementation mAceImplementation;

    @Override
    public void onCreate() {
        super.onCreate();

        // Setup Logging
        Log.addLogNode(LogCat.getInstance());

        // Setup ACE
        mAceImplementation = new BleImplementation(this, null);

        Log.v(TAG, "Created CoCurator Application");
    }

    /**
     * @return the Android Collocated Experiences implementation.
     */
    public AceImplementation getAce() {
        return mAceImplementation;
    }

}
