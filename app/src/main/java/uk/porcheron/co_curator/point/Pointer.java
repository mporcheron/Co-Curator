package uk.porcheron.co_curator.point;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.val.Style;

/**
 * Created by map on 01/09/15.
 */
public class Pointer extends View {

    private Paint mOuterPaint;
    private Paint mInnerPaint;

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

    public Pointer(User user) {
        super(TimelineActivity.getInstance());

        int colorInt = Style.userMeBgColors[user.userId];
        mInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerPaint.setStyle(Paint.Style.FILL);
        mInnerPaint.setColor(colorInt);

        colorInt = Style.pointerBorderColor;
        mOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOuterPaint.setStyle(Paint.Style.FILL);
        mOuterPaint.setColor(colorInt);
    }

    @Override
    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) Style.pointerSize, (int) Style.pointerSize);
    }

    public boolean mDecreasing = true;
    public int mReduceBorderStep = 0;
    public final int STEP_PULSATE = 20;

    @Override
    public void onDraw(Canvas canvas) {
        float outerR = Style.pointerSize/2f;
        canvas.drawCircle(outerR, outerR, outerR, mOuterPaint);

        float innerR = Style.pointerCentre/2f;

        if(mDecreasing) {
            if(mReduceBorderStep < Style.pointerCentreReduceSteps) {
                innerR += (mReduceBorderStep++ * Style.pointerCentreReduceStepBy);
            } else {
                innerR += (mReduceBorderStep-- * Style.pointerCentreReduceStepBy);
                mDecreasing = false;
            }
        } else {
            if(mReduceBorderStep > 0) {
                innerR += (mReduceBorderStep-- * Style.pointerCentreReduceStepBy);
            } else {
                innerR += (mReduceBorderStep++ * Style.pointerCentreReduceStepBy);
                mDecreasing = true;
            }
        }

        canvas.drawCircle(outerR, outerR, innerR, mInnerPaint);

        this.postInvalidateDelayed(40 + (mReduceBorderStep * STEP_PULSATE));
    }

}
