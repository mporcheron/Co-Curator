package uk.porcheron.ace;

import android.app.IntentService;
import android.content.Intent;

import uk.porcheron.ace.log.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public abstract class AceBackgroundService extends IntentService {

    /** Logging */
    protected static final String TAG = AceFramework.TAG + ":IntentService";

    public AceBackgroundService() {
        super("BleBackgroundService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (AceAction.DISCOVER.equals(action)) {
                discoverDevices();
            }
        }
    }

    /** Go forth and perform the device discovery */
    protected abstract void discoverDevices();
}
