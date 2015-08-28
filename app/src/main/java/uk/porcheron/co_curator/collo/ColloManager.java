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
import uk.porcheron.co_curator.util.CCLog;
import uk.porcheron.co_curator.util.Event;
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

    public static final int BEAT_EVERY = 15000;
    private static final int UPDATE_USERS_EVERY = 2;
    private static final float HEARTBEAT_WAIT = 5f;

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

            broadcast(user.globalUserId, message, true);
        }
    }

    private static void broadcast(int recipientGlobalUserId, String action, Object... components) {
        StringBuilder mb = new StringBuilder(action + ColloDict.SEP + Instance.globalUserId);
        for(Object component : components) {
            mb.append(ColloDict.SEP);
            mb.append(component.toString());
        }
        String message = mb.toString();

        broadcast(recipientGlobalUserId, message, true);
    }

    private static void broadcast(int recipientGlobalUserId, String message, boolean send) {
        if(recipientGlobalUserId == Instance.globalUserId) {
            Log.e(TAG, "Can't talk to yourself");
            return;
        }

        User user = Instance.users.getByGlobalUserId(recipientGlobalUserId);
        if(user == null || user.ip == null || user.ip.isEmpty()) {
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
                if(!isBoundTo(globalUserId)) {
                    bindToUser(globalUserId);
                }
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
    public static class Client extends AsyncTask<String, Void, Void> {
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

            int attempts = 3;
            while(attempts-- > 0) {
                Log.v(TAG, "User[" + Instance.globalUserId + "] Send message[" + message[0] + "] to User[" + mGlobalUserId + "] at " + mDestinationIp + ":" + mDestinationPort);

                CCLog.write(Event.COLLO_SEND, "{globalUserId=" + mGlobalUserId + ",message=" + message + "}");

                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(mDestinationIp, mDestinationPort), TIMEOUT);
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.writeUTF(message[0]);
                    break;
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    CCLog.write(Event.COLLO_SEND_FAIL, "{globalUserId=" + mGlobalUserId + ",message=" + message + "}");


//                Boolean boundTo = mUsersBoundTo.get(mGlobalUserId);
//                if(boundTo != null && boundTo) {
//                    unBindFromUser(mGlobalUserId);
//                }
                }
            }

            return null;
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
            CCLog.write(Event.APP_BEAT, "{unbindAll=" + params[0] + "}");

            if(mBeatsTillNextUpdate == 0) {
                WebLoader.loadUsersFromWeb();
                ServerManager.update();
                mBeatsTillNextUpdate = UPDATE_USERS_EVERY;
            } else {
                mBeatsTillNextUpdate--;
            }

            if(params[0]) {
                ColloManager.broadcast(ColloDict.ACTION_UNBIND);
                for(User u : Instance.users) {
                    unBindFromUser(u.globalUserId);
                }
            }

            long earliest = System.currentTimeMillis() - (int) (HEARTBEAT_WAIT * BEAT_EVERY);
            for(User u : Instance.users) {
                if(isBoundTo(u.globalUserId)) {
                    ColloManager.broadcast(u.globalUserId, ColloDict.ACTION_HEARTBEAT);

                    Long heardFrom = mHeardFromAt.get(u.globalUserId);
                    if(heardFrom != null && heardFrom > 0 && heardFrom < earliest) {
                        Log.e(TAG, "Haven't heard from " + u.globalUserId + " for a while");
                        //unBindFromUser(u.globalUserId);
                    }
                }
            }
            return null;
        }
    }

    public static void bindToUser(int globalUserId) {
        if(globalUserId == Instance.globalUserId) {
            Log.e(TAG, "Can't bind to yourself");
            return;
        }

        mUsersBoundTo.put(globalUserId, true);
        mHeardFromAt.put(globalUserId, -1L);
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
        CCLog.write(Event.COLLO_DO_UNBIND, "{globalUserId=" + globalUserId + "}");

        if(globalUserId == Instance.globalUserId) {
            return;
        }

        mUsersBoundTo.remove(globalUserId);
        mHeardFromAt.put(globalUserId, -1L);
        Instance.users.unDrawUser(globalUserId);
        TimelineActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Instance.items.retestDrawing();
                TimelineActivity.getInstance().redrawCentrelines();
            }
        });
    }

    public static boolean isBoundTo(int globalUserId) {
        if(globalUserId == Instance.globalUserId) {
            return false;
        }

        Boolean boundTo = mUsersBoundTo.get(globalUserId);
        return boundTo != null && boundTo.booleanValue() && Instance.users.getByGlobalUserId(globalUserId).draw();
    }
}
