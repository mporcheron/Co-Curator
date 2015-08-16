package uk.porcheron.co_curator.collo;

import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.val.Collo;
import uk.porcheron.co_curator.val.Instance;

/**
 * Manage connections to other users.
 */
public class ClientManager {
    private static final String TAG = "CC:ColloClientManager";

    private static SparseArray<Client> mClients = new SparseArray<>();

    private static String mPreviousMessage;

    ClientManager() {
    }

    public static void postMessage(String action, Object... components) {
        StringBuilder mb = new StringBuilder(action + ColloDict.SEP + Instance.globalUserId);
        for(Object component : components) {
            mb.append(ColloDict.SEP);
            mb.append(component.toString());
        }
        String message = mb.toString();

        for(User user : Instance.users) {
            if (user.globalUserId == Instance.globalUserId) {
                continue;
            }

            postMessage(user.globalUserId, message);
        }
    }

    private static void postMessage(int recipientGlobalUserId, String message) {
        Log.d(TAG, "Post `" + message + "` to User[" + recipientGlobalUserId + "]");
        User user = Instance.users.getByGlobalUserId(recipientGlobalUserId);
        if(user.ip.isEmpty()) {
            Log.v(TAG, "Could not send message('" + message + "'): No connection details for User[" + user.globalUserId + "]");
            return;
        }

        mPreviousMessage = message;

        Client c = mClients.get(user.globalUserId);
        if(c != null) {
            while (c.getStatus() == AsyncTask.Status.RUNNING) {
                Log.e(TAG, "Waiting for previous message (" + mPreviousMessage + ") to end");
                c.cancel(true);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Log.d(TAG, "User[" + Instance.globalUserId + "] Connect to User[" + user.globalUserId + "] at " + user.ip + ":" + Collo.cPort(user.globalUserId));
        c = new Client(user.ip, Collo.cPort(user.globalUserId));
        mClients.put(user.globalUserId, c);

        c.execute(message);
    }

}
