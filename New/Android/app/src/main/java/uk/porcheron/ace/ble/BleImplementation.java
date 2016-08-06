package uk.porcheron.ace.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import uk.porcheron.ace.AceAction;
import uk.porcheron.ace.AceBackgroundService;
import uk.porcheron.ace.AceImplementation;

/**
 * Discover and connect to users over Bluetooth Low Energy and Wifi Direct.
 */
public class BleImplementation extends AceImplementation {

    /** Local Bluetooth adapter. */
    private final BluetoothAdapter mBluetoothAdapter;

    /** Intent request code for enabling Bluetooth */
    public static final int REQUEST_ENABLE_BT = 0;

    /** Intent request code for secure device connections */
    public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;

    /** String buffer for outgoing messages. */
    private StringBuffer mOutStringBuffer;

    /**
     * Constructor for the BLE implementation.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BleImplementation(Context context, Handler handler) {
        super(context, handler);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Is discovery possible at this moment?
     *
     * @param activity Current Activity with focus.
     * @return {@code true} if discovery is possible.
     */
    @Override
    public boolean discoveryPossible(Activity activity) {
        if(mBluetoothAdapter != null){
            if(mBluetoothAdapter.isEnabled()) {
                return true;
            }

            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        return false;
    }

    /**
     * @return a Class that extends the {@link AceBackgroundService} and implements the required actions.
     */
    @Override
    protected Class<BleBackgroundService> getBackgroundService() {
        return BleBackgroundService.class;
    }

}
