package uk.porcheron.co_curator.point;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.val.Style;

/**
 * Created by map on 01/09/15.
 */
public class Pointer extends View {

    private float mTriggeredX;
    private float mTriggeredY;

    private User mUser;
    private Paint mPaint;

    public Pointer(Context context) {
        super(context);
    }

    public Pointer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Pointer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Pointer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public Pointer(User user, float x, float y) {
        super(TimelineActivity.getInstance());

        mUser = user;
        mTriggeredX = x;
        mTriggeredY = y;

        int colorInt = Style.userMeBgColors[user.userId];
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(colorInt);
    }

    public User getUser() {
        return mUser;
    }

    public float getTriggeredX() {
        return mTriggeredX;
    }

    public float getTriggeredY() {
        return mTriggeredY;
    }

    @Override
    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) Style.pointerMaxSize, (int) Style.pointerMaxSize);
    }

    public boolean mDecreasing = true;
    public int mPulseStep = 0;
    public final int STEP_PULSATE = 20;

    @Override
    public void onDraw(Canvas canvas) {
        float maxR = Style.pointerMaxSize /2f;
        float minR = Style.pointerMinSize /2f;

        if(mDecreasing) {
            if(mPulseStep < Style.pointerPulseSteps) {
                minR += (mPulseStep++ * Style.pointerPulseStepIncrement);
            } else {
                minR += (mPulseStep-- * Style.pointerPulseStepIncrement);
                mDecreasing = false;
            }
        } else {
            if(mPulseStep > 0) {
                minR += (mPulseStep-- * Style.pointerPulseStepIncrement);
            } else {
                minR += (mPulseStep++ * Style.pointerPulseStepIncrement);
                mDecreasing = true;
            }
        }

        canvas.drawCircle(maxR, maxR, minR, mPaint);

        this.postInvalidateDelayed(40 + (mPulseStep * STEP_PULSATE));
    }

}
