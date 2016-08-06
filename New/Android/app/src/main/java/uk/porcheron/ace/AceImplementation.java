package uk.porcheron.ace;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import uk.porcheron.ace.ble.BleBackgroundService;
import uk.porcheron.ace.log.Log;

/**
 * Service that can be used for discovering and connecting to other devices.
 */
public abstract class AceImplementation {

    /** Logging */
    protected static final String TAG = AceFramework.TAG + ":Implementation";

    /** The UI Activity Context */
    private final Context mContext;

    /** Handler for returning messages to the UI thread */
    private final Handler mUiHandler;

    /** Handler for firing discovery intents. */
    private Handler mDiscoveryHandler;

    /** How often we should begin the discovery task */
    private long mDiscoveryFrequency = 15000;

    /**
     * Constructor for the specific implementation.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public AceImplementation(Context context, Handler handler) {
        mContext = context;
        mUiHandler = handler;

        Log.v(TAG, "AceImplementation created");
    }

    /**
     * Is discovery possible at this moment? Maybe the implementation can try and make
     * discovery possible, although this is optional.
     *
     * @param activity Current Activity with focus.
     * @return {@code true} if discovery is possible.
     */
    public abstract boolean discoveryPossible(Activity activity);

    /**
     * How frequent devices should be discovered. Too high and we won't discover people quick
     * enough, to low and we'll flatten phones.
     *
     * @param frequency Frequency in seconds
     */
    protected final void setDiscoveryFrequency(long frequency) {
        mDiscoveryFrequency = frequency;
    }

    /**
     * Triggered when we should discover devices.
     *
     * @param context Current Context.
     * */
    protected final void discoverDevices(Context context) {
        Intent intent = new Intent(context, getBackgroundService());
        intent.setAction(AceAction.DISCOVER.ident);
        context.startService(intent);
    }

    /**
     * @return a Class that extends the {@link AceBackgroundService} and implements the required actions.
     */
    protected abstract Class<? extends AceBackgroundService> getBackgroundService();

    /**
     * Start automatic discovery.
     */
    public final void start() {
        Log.d(TAG, "Start automatic device discovery");

        if(mDiscoveryFrequency > 0) {
            mDiscoveryHandler = new Handler();
            mDiscoveryHandler.postDelayed(mDiscoveryRunnable, 0);
        }
    }

    /**
     * Runnable that triggers device discovery.
     */
    private Runnable mDiscoveryRunnable = new Runnable() {
        @Override
        public void run() {
            Log.v(TAG, "Discover Devices");

            discoverDevices(mContext);
            mDiscoveryHandler.postDelayed(mDiscoveryRunnable, mDiscoveryFrequency);
        }
    };

    /**
     * Immediately halt and shutdown all held resources for the implementation. You should also call
     * the super shutdown function to clean up any automated loops.
     */
    protected void shutdown() {
        Log.v(TAG, "Shutdown Ace");

        mDiscoveryHandler.removeCallbacks(mDiscoveryRunnable);
    }

}
