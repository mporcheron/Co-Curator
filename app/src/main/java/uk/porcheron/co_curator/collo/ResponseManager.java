package uk.porcheron.co_curator.collo;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by map on 15/08/15.
 */
public class ResponseManager {
    private static final String TAG = "CC:ResponseManager";

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
