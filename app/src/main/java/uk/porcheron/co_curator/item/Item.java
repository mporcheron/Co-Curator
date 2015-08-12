package uk.porcheron.co_curator.item;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.val.Style;

/**
 * A timeline item. Only the _actual_ content is drawn (i.e. no borders etc).
 */
public abstract class Item extends View implements View.OnTouchListener {
    private static final String TAG = "CC:Item";

    private static Random mRandom = new Random();

    private User mUser;
    private int mItemId;

    private RectF mSlotBounds;
    private RectF mOuterBounds;
    private RectF mInnerBounds;
    private RectF mStemBounds;

    private float mRandomPadRight;
    private float mRandomPadRightHalf;

    public Item(Context context) { super(context); }

    public Item(Context context, AttributeSet attrs) { super(context, attrs); }

    public Item(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    public Item(User user, int itemId) {
        super(TimelineActivity.getInstance());

        mUser = user;
        mItemId = itemId;

        mRandomPadRight = Style.itemXGapMin + mRandom.nextInt((int) Style.itemXGapOffset);
        mRandomPadRightHalf = mRandomPadRight / 2;

        setOnTouchListener(this);
    }

    @Override
    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) mSlotBounds.width(), (int) mSlotBounds.height());
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawRoundRect(mOuterBounds,
                Style.itemRoundedCorners,
                Style.itemRoundedCorners,
                getUser().bgPaint);

        canvas.drawRect(mStemBounds,
                getUser().bgPaint);
    }

    protected final RectF getInnerBounds() {
        return mInnerBounds;
    }

    protected final RectF setBounds(float width, float height, float padding) {
        float top;
        if (mUser.above) {
            top = Style.itemFullHeight + mUser.offset - height;
        } else {
            top = Style.layoutCentreHeight + mUser.offset;
        }

        mOuterBounds = new RectF(mRandomPadRightHalf, top, mRandomPadRightHalf + width, top + height);

        mInnerBounds = new RectF(mOuterBounds.left + padding,
                mOuterBounds.top + padding,
                mOuterBounds.right - padding,
                mOuterBounds.bottom - padding);

        mSlotBounds = new RectF(0, 0, mOuterBounds.width() + mRandomPadRight, Style.itemFullHeight + Style.layoutCentreHeight);

        float offset = mRandomPadRightHalf + Style.itemStemNarrowBy +
                mRandom.nextInt((int) (mInnerBounds.width() - (2 * Style.itemStemNarrowBy)));

        if (mUser.above) {
            mStemBounds = new RectF(offset, mOuterBounds.bottom, offset + Style.lineWidth, Style.layoutHalfPadding + mUser.centrelineOffset);
        } else {
            mStemBounds = new RectF(offset, mUser.centrelineOffset + Style.lineWidth - 1, offset + Style.lineWidth, mOuterBounds.top);
        }

        return mInnerBounds;
    }

    protected final User getUser() {
        return mUser;
    }

    protected final int getItemId() {
        return mItemId;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "Touched");
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "ACTION_DOWN");
            if (mOuterBounds.contains(event.getX(), event.getY())) {
                Log.d(TAG, "Within Outer Bounds");
                onClick(v);
                return true;
            }
        }

        return false;
    }

    protected abstract void onClick(View v);
}
