package uk.porcheron.co_curator.item;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.val.Style;

/**
 * A timeline item. Only the _actual_ content is drawn (i.e. no borders etc).
 */
public abstract class Item extends View {
    private static final String TAG = "CC:Item";

    private static Random mRandom = new Random();

    private float mSetWidth;
    private float mSetHeight;
    private float mSetPadding;

    private User mUser;
    private int mItemId;
    private int mDateTime;

    private RectF mSlotBounds;
    private RectF mOuterBounds;
    private RectF mInnerBounds;
    private RectF mStemBounds;

    private float mRandomPadRight;
    private float mRandomPadRightHalf;

    private boolean mDrawn = false;
    private boolean mDeleted = false;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    public Item(Context context) { super(context); }

    public Item(Context context, AttributeSet attrs) { super(context, attrs); }

    public Item(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    public Item(User user, int itemId, int dateTime) {
        super(TimelineActivity.getInstance());

        mUser = user;
        mItemId = itemId;
        mDateTime = dateTime;

        mRandomPadRight = Style.itemXGapMin + mRandom.nextInt((int) Style.itemXGapOffset);
        mRandomPadRightHalf = mRandomPadRight / 2;

        final GestureDetector gD  = new GestureDetector(TimelineActivity.getInstance(), new GestureListener());
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                gD.onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) mSlotBounds.width(), (int) mSlotBounds.height());
    }

    public final void setDrawn(boolean newState) {
        mDrawn = newState;
    }

    public final boolean isDrawn() {
        return mDrawn;
    }

    public final void setDeleted(boolean newState) {
        mDeleted = newState;
    }

    public final boolean isDeleted() {
        return mDeleted;
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
        mSetWidth = width;
        mSetHeight = height;
        mSetPadding = padding;

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

    public final void reassessBounds() {
        setBounds(mSetWidth, mSetHeight, mSetPadding);
    }

    protected final User getUser() {
        return mUser;
    }

    protected final int getItemId() {
        return mItemId;
    }

    protected final String getUniqueItemId() { return mUser.globalUserId + "-" + mItemId; }

    protected final int getDateTime() { return mDateTime; }

    protected abstract boolean onTap();

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(TAG, "SingleTapConfirmed");
            return onTap();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "Fling");

            if(mUser.globalUserId != Instance.globalUserId) {
                return false;
            }

            Instance.items.remove(mUser.globalUserId, mItemId, true, true, true);
            return true;
//
//            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                return false; // Right to left
//            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                return false; // Left to right
//            }
//
//            if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                return false; // Bottom to top
//            }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                return false; // Top to bottom
//            }
        }

        @Override
        public void onLongPress(MotionEvent event) {
            super.onLongPress(event);
            Log.d(TAG, "onLongPress");
            TimelineActivity.getInstance().onLongClick(Item.this);
        }
    }
}
