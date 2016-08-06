package uk.porcheron.ace.ble;

import android.app.IntentService;

import uk.porcheron.ace.AceBackgroundService;
import uk.porcheron.ace.log.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class BleBackgroundService extends AceBackgroundService {

    /** Go forth and perform the device discovery */
    protected void discoverDevices() {
        Log.e(TAG, "Device discovery not implemented!");
    }
}
