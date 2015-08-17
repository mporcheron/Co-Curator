package uk.porcheron.co_curator.collo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.Queue;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.val.Instance;

/**
 * Binding mechanism by rotating the phone a certain amount of degrees.
 */
public class ColloCompass implements SensorEventListener, ColloManager.ResponseHandler {
    private static final String TAG = "CC:ColloGesture";

    private TimelineActivity mActivity;
    private static ColloCompass mInstance;

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] mGravity;
    private float[] mGeomagnetic;

    private double mPitch;
    private Queue<Double> mPreviousRotateValues = new CircularFifoQueue<>(10);
    private Queue<Double> mOrientations = new CircularFifoQueue<>(5);

    private long mNextFirePossibleAfter = 0L;
    private long mDoBindBefore = 0L;
    private long mReceivedBindAt = -1;
    private int mReceivedBindFromGlobalUserId = -1;

    private double DIFFERENCE_TO_TRIGGER = 170;
    private long TIME_TILL_NEXT_FIRE = 2000L;
    private long TIME_GAP_FOR_BIND = 3000L;

    public static ColloCompass getInstance() {
        if(mInstance == null) {
            mInstance = new ColloCompass();
        }
        return mInstance;
    }

    ColloCompass() {
        mActivity = TimelineActivity.getInstance();
        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void resumeListening() {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);

        ColloManager.ResponseManager.registerHandler(ColloDict.ACTION_BIND, mInstance);
        ColloManager.ResponseManager.registerHandler(ColloDict.ACTION_DO_BIND, mInstance);
    }

    public void pauseListening() {
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    public void onSensorChanged(SensorEvent event) {
        long now = System.currentTimeMillis();

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values.clone();

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values.clone();

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                mOrientations.add(Math.toDegrees(orientation[2])); // azimut, pitch and roll
                float sum = 0;
                for(double f : mOrientations) {
                    sum += f;
                }
                mPitch = sum / mOrientations.size();

                if(mNextFirePossibleAfter > now) {
                    return;
                }

                for(double v : mPreviousRotateValues) {
                    if(Math.abs(v - mPitch) > DIFFERENCE_TO_TRIGGER) {
                        Log.d(TAG, "Rotated " + DIFFERENCE_TO_TRIGGER + "degrees => BIND");

                        mNextFirePossibleAfter = now + TIME_TILL_NEXT_FIRE;
                        mDoBindBefore = now + TIME_GAP_FOR_BIND;

                        if(mReceivedBindAt + TIME_GAP_FOR_BIND > now) {
                            doBind(mReceivedBindFromGlobalUserId, true);
                        } else {
                            requestBind();
                        }
                        break;
                    }
                }
                mPreviousRotateValues.add(mPitch);
            }
        }
    }

    @Override
    public boolean respond(String action, int globalUserId, String... data) {
        long now = System.currentTimeMillis();
        switch(action) {
            case ColloDict.ACTION_BIND:
                mReceivedBindAt = now;
                mReceivedBindFromGlobalUserId = globalUserId;

                if(mDoBindBefore <= now) {
                    doBind(globalUserId, true);
                }
                break;

            case ColloDict.ACTION_DO_BIND:
                try {
                    int otherGlobalUserId = Integer.parseInt(data[0]);
                    if (otherGlobalUserId == Instance.globalUserId) {
                        doBind(globalUserId, false);
                    }
                } catch(NumberFormatException e) {
                    Log.e(TAG, ColloDict.ACTION_DO_BIND + " did not come with otherGlobalUserId");
                }
                break;

        }

//        try {
//            if(cg.havePossibleBinder(Float.parseFloat(data[0]), Float.parseFloat(data[1]), data[2])) {
//                Instance.users.drawUser(globalUserId);
//                ColloManager.broadcast(ColloDict.ACTION_DO_BIND, globalUserId);
//
//                TimelineActivity.getInstance().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Instance.items.retestDrawing();
//                        TimelineActivity.getInstance().redrawCentrelines();
//                    }
//                });
//            }
//            return true;
//        } catch (NumberFormatException e) {
//            Log.e(TAG, "Invalid co-ordinates received");
//        }

        return false;
    }

    private void requestBind() {
        ColloManager.broadcast(ColloDict.ACTION_BIND);
    }

    private void doBind(int globalUserId, boolean broadcast) {
        if(broadcast) {
            ColloManager.broadcast(ColloDict.ACTION_DO_BIND, globalUserId);
        }

        Instance.users.drawUser(globalUserId);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Instance.items.retestDrawing();
                TimelineActivity.getInstance().redrawCentrelines();
            }
        });
    }
}
