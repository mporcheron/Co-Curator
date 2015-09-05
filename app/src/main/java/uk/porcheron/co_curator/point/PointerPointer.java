package uk.porcheron.co_curator.point;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.val.Style;

/**
 * Created by map on 01/09/15.
 */
public class PointerPointer extends View {

    private Paint mFillPaint;
    private Paint mLinePaint;
    private boolean mPointRight;
    private float mYPosition;

    public PointerPointer(Context context) {
        super(context);
    }

    public PointerPointer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PointerPointer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PointerPointer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PointerPointer(User user, float yPosition, boolean pointRight) {
        super(TimelineActivity.getInstance());

        int colorInt = Style.userMeBgColors[user.userId];
        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setColor(colorInt);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(Style.pointerPointerArrowLineWidth);
        mLinePaint.setColor(colorInt);

        mYPosition = yPosition;
        mPointRight = pointRight;
    }

    public float getYPosition() {
        return mYPosition;
    }

    public boolean getPointRight() {
        return mPointRight;
    }

    @Override
    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) (Style.pointerPointerArrowLength + Style.pointerPointerCircleSize);
        setMeasuredDimension(width, (int) Style.pointerPointerCircleSize);
    }

    @Override
    public void onDraw(Canvas canvas) {
        float circleR = Style.pointerPointerCircleSize / 2f;
        float xOffset = mPointRight ? 0 : Style.pointerPointerArrowLength;
        canvas.drawCircle(xOffset + circleR, circleR, circleR, mFillPaint);

        float halfLineHeight = Style.pointerPointerArrowLineWidth / 2;
        float x1, x2;

        if(mPointRight) {
            x1 = halfLineHeight;
            x2 = getMeasuredWidth() - halfLineHeight;
        } else {
            x1 = halfLineHeight;
            x2 = x1 + Style.pointerPointerCircleSize + Style.pointerPointerArrowLength - halfLineHeight;
        }
        float midY = circleR;
        float y1 = midY - halfLineHeight;
        float y2 = midY + halfLineHeight;
        canvas.drawRect(x1, y1, x2, y2, mFillPaint);

        if(Style.pointerPointerArrowHeadDepth != 0 && Style.pointerPointerArrowHeadHeight != 0) {
            Path p = new Path();
            if (mPointRight) {
                float backBy = x2 - Style.pointerPointerArrowHeadDepth;

                p.moveTo(backBy, midY - Style.pointerPointerArrowHeadHeight);
                p.lineTo(x2, midY);
                p.lineTo(backBy, midY + Style.pointerPointerArrowHeadHeight);
            } else {
                float backBy = x1 + Style.pointerPointerArrowHeadDepth;

                p.moveTo(backBy, midY - Style.pointerPointerArrowHeadHeight);
                p.lineTo(x1, midY);
                p.lineTo(backBy, midY + Style.pointerPointerArrowHeadHeight);
            }

            canvas.drawPath(p, mLinePaint);
        }
    }

}
