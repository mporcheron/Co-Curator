package uk.porcheron.cocurator.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import uk.porcheron.ace.AceImplementation;
import uk.porcheron.ace.ble.BleImplementation;
import uk.porcheron.ace.log.Log;
import uk.porcheron.ace.log.LogNode;
import uk.porcheron.cocurator.CoCurator;
import uk.porcheron.cocurator.R;

public class BleActivity extends AppCompatActivity {

    /** Logging. */
    public static final String TAG = CoCurator.TAG + ":BleActivity";

    /** Application instance. */
    private CoCurator mApplication;

    /** Android Collocated Experiences instance. */
    private AceImplementation mAce;

    /** EditText for displaying information */
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ble);

        mApplication = (CoCurator) getApplication();
        mAce = mApplication.getAce();

        mEditText = (EditText) findViewById(R.id.editText);

        Log.addLogNode(new LogNode() {
            @Override
            public void println(int priority, final String tag, final String msg, Throwable tr) {
                BleActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEditText.getText().insert(0, tag + "\t" + msg + "\n");
                    }
                });
            }
        });

        Log.v(TAG, "Created Timeline Activity");
    }

    @Override
    public void onResume() {
        super.onResume();
        startDiscovery();
    }

    /** Start device discovery */
    private void startDiscovery() {
        if(mAce.discoveryPossible(this)) {
            mAce.start();
        } else {
            Log.e(TAG, "Device discovery not possible!");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BleImplementation.REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    //connectDevice(data, true);
                }
                break;

            case BleImplementation.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    startDiscovery();
                } else {
                    Log.e(TAG, "Bluetooth not enabled");
                    Toast.makeText(this, R.string.errorBtNotEnabled, Toast.LENGTH_SHORT).show();
                }
        }
    }
}
