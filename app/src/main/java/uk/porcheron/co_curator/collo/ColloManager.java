package uk.porcheron.co_curator.collo;

import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.val.Collo;
import uk.porcheron.co_curator.val.Instance;

/**
 * Manage connections to other users.
 */
public class ColloManager {
    private static final String TAG = "CC:ColloManager";

    private static SparseArray<Client> mClients = new SparseArray<>();
    private static SparseArray<BlockedIp> mClientBlacklist = new SparseArray<>();

    private static String mPreviousMessage;

    ColloManager() {
    }

    public static void broadcast(String action, Object... components) {
        StringBuilder mb = new StringBuilder(action + ColloDict.SEP + Instance.globalUserId);
        for(Object component : components) {
            mb.append(ColloDict.SEP);
            mb.append(component.toString());
        }
        String message = mb.toString();

        if(Instance.users == null) {
            return;
        }

        for(User user : Instance.users) {
            if (user.globalUserId == Instance.globalUserId) {
                continue;
            }

            broadcast(user.globalUserId, message);
        }
    }

    private static void broadcast(int recipientGlobalUserId, String message) {

        User user = Instance.users.getByGlobalUserId(recipientGlobalUserId);
        if(user.ip.isEmpty()) {
            return;
        }

        mPreviousMessage = message;

        Client c = mClients.get(user.globalUserId);
        if(c != null) {
            while (c.getStatus() == AsyncTask.Status.RUNNING) {
                Log.e(TAG, "Waiting for previous message (" + mPreviousMessage + ") to end");
                c.cancel(true);
                c = null;
                break;
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }

        c = new Client(user.globalUserId, user.ip, Collo.cPort(user.globalUserId));
        mClients.put(user.globalUserId, c);

        c.execute(message);
    }

    /**
     * Object that handles responses to messages received.
     */
    public static interface ResponseHandler {

        public boolean respond(String action, int globalUserId, String... data);

    }

    /**
     * Process and send out responses
     */
    public static class ResponseManager {
        private static final String TAG = "CC:ColloResponseManager";

        private static Map<String,ResponseHandler> mHandlers = new HashMap<>();

        public static void registerHandler(String action, ResponseHandler handler) {
            mHandlers.put(action, handler);
        }

        public static boolean respond(String action, int globalUserId, String... data) {
            ResponseHandler handler = mHandlers.get(action);
            if(handler == null) {
                Log.e(TAG, "No ResponseHandler for Action[" + action + "]");;
                return false;
            }

            return handler.respond(action, globalUserId, data);
        }
    }

    /**
     * Created by map on 14/08/15.
     */
    public static class Client extends AsyncTask<String, String, Void> {
        private static final String TAG = "CC:ColloClient";

        private int mGlobalUserId;
        private String mDestinationIp;
        private int mDestinationPort;

        private static final int TIMEOUT = 1000;

        private String mWait = "WAIT";

        Client(int globalUserId, String ip, int port) {
            mGlobalUserId = globalUserId;
            mDestinationIp = ip;
            mDestinationPort = port;
        }

        @Override
        protected Void doInBackground(String... message) {
//            BlockedIp blockedIp = mClientBlacklist.get(mGlobalUserId);
//            if(blockedIp != null && blockedIp.attempts > 0 && mDestinationIp.equals(blockedIp.ip)) {
//                Log.v(TAG, "User[" + Instance.globalUserId + "] Temporarily paused messages to User[" + mGlobalUserId + "] at " + mDestinationIp + ":" + mDestinationPort);
//                blockedIp.attempts--;
//                return null;
//            }

            Log.v(TAG, "User[" + Instance.globalUserId + "] Send message[" + message[0] + "] to User[" + mGlobalUserId + "] at " + mDestinationIp + ":" + mDestinationPort);
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(mDestinationIp, mDestinationPort), TIMEOUT);
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(message[0]);
                publishProgress(null);
            } catch (IOException e) {
                publishProgress("Error: " + e.toString());
                mClientBlacklist.put(mGlobalUserId, new BlockedIp(mDestinationIp));
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

    static class BlockedIp {
        public final String ip;
        public int attempts = 3;
        BlockedIp(String ip) {
            this.ip = ip;
        }
    }
}
