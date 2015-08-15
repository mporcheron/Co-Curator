package uk.porcheron.co_curator.collo;

import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.porcheron.co_curator.db.WebLoader;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.SoUtils;
import uk.porcheron.co_curator.val.Collo;
import uk.porcheron.co_curator.val.Instance;

/**
 * All other users to connect to us.
 */
public class ServerManager {
    private static final String TAG = "CC:ColloServerManager";

    private static SparseArray<Server> mServers = new SparseArray<>();

    ServerManager() {

    }


    public static void update() {
        Log.v(TAG, "User[" + Instance.globalUserId + "] Update Servers");

        for(User user : Instance.users) {
            if(user.globalUserId == Instance.globalUserId) {
                continue;
            }

            Server s = mServers.get(user.globalUserId);
            if(s != null) {
                if (s.getPort() == Collo.sPort(user.globalUserId)) {
                    continue;
                } else {
                    s.interrupt();
                }
            }

            Log.v(TAG, "User[" + Instance.globalUserId + "] Start listening at " + SoUtils.getIPAddress(true) + ":" + Collo.sPort(user.globalUserId));
            s = new Server(Collo.sPort(user.globalUserId));
            mServers.put(user.globalUserId, s);
            s.start();
        }
    }


}
