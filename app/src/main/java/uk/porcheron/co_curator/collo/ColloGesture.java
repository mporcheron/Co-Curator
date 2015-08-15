package uk.porcheron.co_curator.collo;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.val.Phone;

/**
 * Detect gestures used for connecting collocated devices.
 */
public class ColloGesture extends GestureDetector.SimpleOnGestureListener implements ResponseHandler {
    private static final String TAG = "CC:ColloGesture";

    public static final String GESTURE_UP = "up";
    public static final String GESTURE_DOWN = "down";

    private static final int X_LEEWAY = 25;
    private static final int Y_LEEWAY = 500;
    private static final float Y_MIN_DISTANCE = .1f;
    private static final long WAIT_BEFORE_NEXT = 4000L;
    private static final long CONNECT_GAP = 10000L;

    private final float mYDistanceTravelled;
    private final float mYLeeway;

    private long mNextTrigger = 0;
    private long mWindow = 0;

    private float mBindX = 0;
    private float mBindY = 0;
    private String mBindDir = "";

    private static ColloGesture mInstance = null;

    public static ColloGesture getInstance() {
        if(mInstance == null) {
            mInstance = new ColloGesture();
            ResponseManager.registerHandler(ColloDict.ACTION_BIND, mInstance);
            ResponseManager.registerHandler(ColloDict.ACTION_DO_BIND, mInstance);
        }
        return mInstance;
    }

    ColloGesture() {
        mYDistanceTravelled = Phone.screenHeight * Y_MIN_DISTANCE;
        mYLeeway = Phone.screenHeight - Y_LEEWAY;
    }

    public boolean havePossibleBinder(float x, float y, String dir) {
        // too late?
        if(System.currentTimeMillis() > mNextTrigger) {
            Log.d(TAG, "Too late");
            return false;
        }

//        // same direction?
//        if(dir.equals(mBindDir)) {
//            Log.d(TAG, "Wrong direction");
//            return false;
//        }

        // close enough?
        if(mBindX - x < X_LEEWAY && mBindY - y < Y_LEEWAY) {
            Log.e(TAG, "BIND BIND BIND");
            return true;
        }

        Log.e(TAG, "We say: (" + mBindX + "," + mBindY + "); they say (" + x + "," + y + ")");

        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // Too soon?
        if(System.currentTimeMillis() < mNextTrigger) {
            Log.e(TAG, "Too soon");
            return false;
        }

        // End at the edge?
        if(e2.getY() >= Y_LEEWAY && e2.getY() < mYLeeway) {
            Log.v(TAG, "Not at edge of screen (y=" + e2.getY() + ")");
            return false;
        }

        // Measure the distance
        float x = Math.abs(e1.getX() - e2.getX());
        float y = Math.abs(e1.getY() - e2.getY());

        if(x > X_LEEWAY) {
            Log.v(TAG, "Too much X (used " + x + "/" + X_LEEWAY + ")");
            return false;
        }

        if(y < mYDistanceTravelled) {
            Log.v(TAG, "Need to use more Y (used " + y + "/" + mYDistanceTravelled + ")");
            return false;
        }

        mNextTrigger = System.currentTimeMillis() + WAIT_BEFORE_NEXT;
        mWindow = System.currentTimeMillis() + CONNECT_GAP;

        Log.v(TAG, "Looks like we're trying to bind");

        mBindX = x;
        mBindY = y;
        mBindDir = e2.getY() < Y_LEEWAY ? GESTURE_UP : GESTURE_DOWN;

        ClientManager.postMessage(ColloDict.ACTION_BIND, x, y, mBindDir);


        return true;
    }

    @Override
    public boolean respond(String action, int globalUserId, String... data) {
        if(action.equals(ColloDict.ACTION_DO_BIND)) {
            try {
                int otherGlobalUserId = Integer.parseInt(data[0]);
                if(otherGlobalUserId == Instance.globalUserId) {
                    User u = Instance.users.getByGlobalUserId(globalUserId);
                    u.draw = true;

                    TimelineActivity.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Instance.items.retestDrawing();
                            TimelineActivity.getInstance().redrawCentrelines();
                        }
                    });
                    return true;
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid other global user ID received");
            }
            return false;
        }

        ColloGesture cg = ColloGesture.getInstance();
        try {
            if(cg.havePossibleBinder(Float.parseFloat(data[0]), Float.parseFloat(data[1]), data[2])) {
                User u = Instance.users.getByGlobalUserId(globalUserId);
                u.draw = true;
                ClientManager.postMessage(ColloDict.ACTION_DO_BIND, globalUserId);

                TimelineActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Instance.items.retestDrawing();
                        TimelineActivity.getInstance().redrawCentrelines();
                    }
                });
            }
            return true;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid co-ordinates received");
        }

        return false;
    }
}
