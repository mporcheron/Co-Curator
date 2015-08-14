package uk.porcheron.co_curator.val;

import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;

/**
 * Data relating to this instance of the running application connecting to other instances.
 */
public class Collo {
    private static final int mBaseServerPort = 54500;

    public static int[] sPorts(int globalUserId) {
        int[] arr = new int[3];
        int i = 0;
        for(User user : Instance.users) {
            if(user.globalUserId == globalUserId) {
                continue;
            }

            arr[i++] = sPort(user.globalUserId);
        }
        return arr;
    }

    public static int[] cPorts(int globalUserId) {
        int[] arr = new int[3];
        int i = 0;
        for(User user : Instance.users) {
            if(user.globalUserId == globalUserId) {
                continue;
            }

            arr[i++] = cPort(user.globalUserId);
        }
        return arr;
    }

    public static int sPort(int globalUserId) {
        return mBaseServerPort + (10 * ((Instance.globalUserId - 1) % 4)) + ((globalUserId - 1) % 4);
    }

    public static int cPort(int globalUserId) {
        return mBaseServerPort + (10 * ((globalUserId - 1) % 4)) + ((Instance.globalUserId - 1) % 4);
    }
}
