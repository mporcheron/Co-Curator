package uk.porcheron.cocurator.timeline;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.TextView;

import uk.porcheron.cocurator.CoCurator;
import uk.porcheron.ace.log.Log;

/**
 * Timeline that recycles views not visible.
 */
public class TimelineLayout extends FrameLayout {

    /** Logging. */
    public static final String TAG = CoCurator.TAG + ":TLLayout";

    /** SurfaceView that draws the trunks of the timeline. */
    private SurfaceView mSurfaceView;

    /** Display nearby devices. */
    private TextView mNearbyDevices;

    public TimelineLayout(Context context) {
        super(context);

        mSurfaceView = new SurfaceView(context);
        mNearbyDevices = new TextView(context);
    }

    public TimelineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSurfaceView = new SurfaceView(context, attrs);
        mNearbyDevices = new TextView(context, attrs);

        completeInit();
    }

    public TimelineLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mSurfaceView = new SurfaceView(context, attrs, defStyle);
        mNearbyDevices = new TextView(context, attrs, defStyle);

        completeInit();
    }

    /**
     * Complete the layout initialisation.
     */
    private void completeInit() {
        // Style the created Views
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mNearbyDevices.setLayoutParams(layoutParams);

        // Add all views to the FrameLayout
        addView(mSurfaceView);
        addView(mNearbyDevices);

        Log.v(TAG, "Created Timeline Layout");
    }
}
