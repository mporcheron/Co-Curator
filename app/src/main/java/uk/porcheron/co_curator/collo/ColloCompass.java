package uk.porcheron.co_curator.collo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.util.Log;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.Queue;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.util.CCLog;
import uk.porcheron.co_curator.util.Event;
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

    private static final boolean mLandscapeRotate = true;

    private double mPitch;
    private Queue<Double> mPreviousRotateValues = new CircularFifoQueue<>(10);
    private Queue<Double> mOrientations = new CircularFifoQueue<>(5);

    private long mNextFirePossibleAfter = 0L;
    private long mDoBindBefore = 0L;
    private long mReceivedBindAt = -1;
    private int mReceivedBindFromGlobalUserId = -1;

    private static double DIFFERENCE_TO_TRIGGER = 165;
    private static long TIME_TILL_NEXT_FIRE = 3000L;
    private static long TIME_GAP_FOR_BIND = 2000L;

    private static long VIBRATE_REQUEST_BIND = 150;
    private static long[] VIBRATE_DO_BIND = {50,100,50,150,50,100};

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
        ColloManager.ResponseManager.registerHandler(ColloDict.ACTION_DO_GROUP_BIND, mInstance);
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

                if(mLandscapeRotate) {
                    mOrientations.add(Math.toDegrees(orientation[0])); // azimut, pitch and roll
                } else {
                    mOrientations.add(Math.toDegrees(orientation[2])); // azimut, pitch and roll
                }
                float sum = 0;
                for(double f : mOrientations) {
                    sum += f;
                }
                mPitch = sum / mOrientations.size();

                if(mNextFirePossibleAfter > now) {
                    //mPreviousRotateValues.add(mPitch);
                    return;
                }

                boolean add = false;
                for(double v : mPreviousRotateValues) {
                    double diff = Math.abs(v - mPitch);
                    if(diff > DIFFERENCE_TO_TRIGGER) {

                        Log.d(TAG, "Rotated " + DIFFERENCE_TO_TRIGGER + "degrees => BIND");
                        CCLog.write(Event.COLLO_ROTATE, "{diff=" + diff + "}");

                        if(mReceivedBindAt > 0 && mReceivedBindAt + TIME_GAP_FOR_BIND > now) {
                            Log.v(TAG, "Binding close enough to previous bind request");
                            doBind(mReceivedBindFromGlobalUserId, true);
                        } else {
                            requestBind();
                        }

                        return;
                    }
                }

                // if we request or do bind, we don't save this value
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

                if(mDoBindBefore > now) {
                    Log.d(TAG, "Received bind from " + globalUserId + ", will bind to them");
                    doBind(globalUserId, true);
                }
                break;

            case ColloDict.ACTION_DO_BIND:
                try {
                    int otherGlobalUserId = Integer.parseInt(data[0]);
                    Log.e(TAG, "Received doBind from " + globalUserId);

                    if (otherGlobalUserId == Instance.globalUserId) {
                        doBind(globalUserId, false);
                    }

                    if(ColloManager.isBoundTo(globalUserId)) {
                        Log.e(TAG, "We're bound to " + globalUserId + " so, " + otherGlobalUserId + " bind with us too");
                        doBind(globalUserId, true);
                    }

                    if(ColloManager.isBoundTo(otherGlobalUserId)) {
                        Log.e(TAG, "We're bound to " + otherGlobalUserId + " so " + globalUserId + "  bind with us too");
                        doBind(globalUserId, true);
                    }
                } catch(NumberFormatException e) {
                    Log.e(TAG, ColloDict.ACTION_DO_BIND + " did not come with otherGlobalUserId");
                }
                break;


        }

        return false;
    }

    private void requestBind() {
        Log.v(TAG, "Request bind");

        CCLog.write(Event.COLLO_BIND_REQUEST);

        long now = System.currentTimeMillis();

        mNextFirePossibleAfter = now + TIME_TILL_NEXT_FIRE;
        mDoBindBefore = now + TIME_GAP_FOR_BIND;

        Vibrator v = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(VIBRATE_REQUEST_BIND);

        ColloManager.broadcast(ColloDict.ACTION_BIND);
    }

    private void doBind(int globalUserId, boolean broadcast) {
        if(ColloManager.isBoundTo(globalUserId)) {
            Log.e(TAG, "Can't bind to " + globalUserId + ", already bound to them!");
            return;
        }

        CCLog.write(Event.COLLO_DO_BIND, "{globalUserId=" + globalUserId + ",broadcast=" + broadcast + "}");

        if(broadcast) {
            ColloManager.broadcast(ColloDict.ACTION_DO_BIND, globalUserId);
        }

        Vibrator v = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(VIBRATE_DO_BIND, -1);

        ColloManager.bindToUser(globalUserId);
    }
}
