package uk.porcheron.co_curator.collo;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import uk.porcheron.co_curator.val.Instance;

/**
 * Created by map on 14/08/15.
 */
public class Client extends AsyncTask<String, String, Void> {
    private static final String TAG = "CC:ColloClient";

    private String mDestinationIp;
    private int mDestinationPort;

    private String mWait = "WAIT";

    Client(String ip, int port) {
        mDestinationIp = ip;
        mDestinationPort = port;
    }

    public String getIp() {
        return mDestinationIp;
    }

    public int getPort() {
        return mDestinationPort;
    }

    @Override
    protected Void doInBackground(String... arg0) {
        Log.v(TAG, "User[" + Instance.globalUserId + "] Send message to " + mDestinationIp + ":" + mDestinationPort);

            try (
                    Socket socket = new Socket(mDestinationIp, mDestinationPort);
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {

                dataOutputStream.writeUTF(arg0[0]);
                publishProgress(null);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
                publishProgress("Error: Unknown Host - " + e.toString());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
                publishProgress("Error: IO - " + e.toString());
            }

        return null;
    }

    @Override
    protected void onProgressUpdate(String... response) {
        if(response != null) {
            Log.e(TAG, response[0]);
        }
    }

}

