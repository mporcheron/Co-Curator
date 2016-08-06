package uk.porcheron.ace.p2p;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.widget.Toast;

import uk.porcheron.ace.AceBackgroundService;
import uk.porcheron.ace.AceImplementation;
import uk.porcheron.ace.ble.BleBackgroundService;

/**
 * Discover and connect to users over Wifi P2P.
 */
public class P2pImplementation extends AceImplementation {

    /** Intent request code for enabling Bluetooth */
    public static final int REQUEST_ENABLE_BT = 0;

    /** Intent request code for secure device connections */
    public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;

    /** String buffer for outgoing messages. */
    private StringBuffer mOutStringBuffer;

    /** Intent Filter for updates from the Wifi P2P framework */
    private final IntentFilter mIntentFilter = new IntentFilter();

    /** Wifi P2P Manager */
    private final WifiP2pManager mManager;

    /** Wifi P2P Channel */
    private final WifiP2pManager.Channel mChannel;

    /**
     * Constructor for the BLE implementation.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public P2pImplementation(Context context, Handler handler) {
        super(context, handler);

        //  Indicates a change in the Wi-Fi P2P status.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // Setup Wifi P2P
        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, context.getMainLooper(), null);
    }

    /**
     * Is discovery possible at this moment?
     *
     * @param activity Current Activity with focus.
     * @return {@code true} if discovery is possible.
     */
    @Override
    public boolean discoveryPossible(Activity activity) {
        return true;
    }

    /**
     * @return a Class that extends the {@link AceBackgroundService} and implements the required actions.
     */
    @Override
    protected Class<? extends AceBackgroundService> getBackgroundService() {
        return null;
    }

}
