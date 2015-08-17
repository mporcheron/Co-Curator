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

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.db.WebLoader;
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

    public static final int BEAT_EVERY = 10000;
    private static final int UPDATE_USERS_EVERY = 4;
    private static final float HEARTBEAT_WAIT = 1.5f;

    private static SparseArray<Long> mHeardFromAt = new SparseArray<>();
    private static SparseArray<Boolean> mUsersBoundTo = new SparseArray<>();
    private static int mBeatsTillNextUpdate = 0;

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
    public interface ResponseHandler {

        boolean respond(String action, int globalUserId, String... data);

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
            if(action.equals(ColloDict.ACTION_HEARTBEAT)) {
                mHeardFromAt.put(globalUserId, System.currentTimeMillis());
                return true;
            }

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

                Boolean boundTo = mUsersBoundTo.get(mGlobalUserId);
                if(boundTo != null && boundTo) {
                    unBindFromUser(mGlobalUserId);
                }
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

    public static void beat(boolean unbindAll) {
        new HeartbeatTask().execute(unbindAll);
    }

    static class HeartbeatTask extends AsyncTask<Boolean,Void,Void> {

        @Override
        protected Void doInBackground(Boolean... params) {
            if(mBeatsTillNextUpdate == 0) {
                WebLoader.loadUsersFromWeb();
                ServerManager.update();
                mBeatsTillNextUpdate = UPDATE_USERS_EVERY;
            } else {
                mBeatsTillNextUpdate--;
            }

            if(params[0]) {
                ColloManager.broadcast(ColloDict.ACTION_UNBIND);
            } else {
                ColloManager.broadcast(ColloDict.ACTION_HEARTBEAT);
            }

            long earliest = System.currentTimeMillis() - (int) (HEARTBEAT_WAIT * BEAT_EVERY);
            for(User u : Instance.users) {
                Boolean boundTo = mUsersBoundTo.get(u.globalUserId);
                if(boundTo != null && boundTo) {
                    Long heardFrom = mHeardFromAt.get(u.globalUserId);
                    if(heardFrom != null && heardFrom < earliest) {
                        unBindFromUser(u.globalUserId);
                    }
                }
            }
            return null;
        }
    }

    public static void bindToUser(int globalUserId) {
        mUsersBoundTo.put(globalUserId, true);
        Instance.users.drawUser(globalUserId);
        TimelineActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Instance.items.retestDrawing();
                TimelineActivity.getInstance().redrawCentrelines();
            }
        });
    }

    public static void unBindFromUser(int globalUserId) {
        mUsersBoundTo.put(globalUserId, false);
        Instance.users.unDrawUser(globalUserId);
        TimelineActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Instance.items.retestDrawing();
                TimelineActivity.getInstance().redrawCentrelines();
            }
        });
    }
}
