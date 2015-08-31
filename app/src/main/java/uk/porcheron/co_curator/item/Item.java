package uk.porcheron.co_curator.item;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import java.util.Random;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.CCLog;
import uk.porcheron.co_curator.util.Event;
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
    private int mItemIndex;
    private long mDateTime;

    private float mX;
    private float mLongPressX;

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
            public boolean onTouch(final View view, final MotionEvent e) {
                if(mOuterBounds.contains(e.getX(), e.getY())) {
                    if(e.getAction() == MotionEvent.ACTION_DOWN) {
                        handler.postDelayed(mLongPressed, LONG_PRESS_DELAY);
                        mLongPressX = e.getX();
                        mCancelLongPress = false;
                        return true;
                    }

                    if(mCancelLongPress || e.getAction() == MotionEvent.ACTION_MOVE
                            || e.getAction() == MotionEvent.ACTION_UP
                            || e.getAction() == MotionEvent.ACTION_SCROLL) {
                        handler.removeCallbacks(mLongPressed);
                    }

                    gD.onTouchEvent(e);
                    return true;
                }
                return false;
            }
        });
    }

    public boolean dataChanged(String data) {
        return !getData().equals(data);
    }

    public abstract String getData();

    abstract String setData(String data);

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

    final void setDeleted(boolean newState) {
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

    protected final void setDrawnX(float x) {
        mX = x;
    }

    protected final float getDrawnX() {
        return mX;
    }

    protected final RectF getSlotBounds() {
        return mSlotBounds;
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

    protected final int getItemIndex() {
        return mItemIndex;
    }

    protected final void setItemIndex(int itemIndex) {
        mItemIndex = itemIndex;
    }

    public final String getUniqueItemId() { return mUser.globalUserId + "-" + mItemId; }

    protected final void setDateTime(long dateTime) { mDateTime = dateTime; }

    public final long getDateTime() { return mDateTime; }

    protected abstract boolean onTap();

    protected abstract boolean onLongPress();

    private boolean mCancelLongPress = false;

    public final void cancelLongPress() {
        mCancelLongPress = true;
    }

    private static long LONG_PRESS_DELAY = 750;

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(TAG, "Single tap on Item[" + getUniqueItemId() + "]");
            CCLog.write(Event.ITEM_SINGLE_TAP, "{uniqueItemId=" + getUniqueItemId() + "}");

            return onTap();
        }
//
//        @Override
//        public boolean onTouchEvent(MotionEvent e) {
//            if(mCancelLongPress) {
//                mCancelLongPress = false;
//                return false;
//            }
//
//            if(e.getAction() == MotionEvent.ACTION_DOWN) {
//                handler.postDelayed(mLongPressed, LONG_PRESS_DELAY);
//                return true;
//            }
//
//            if(e.getAction() == MotionEvent.ACTION_MOVE
//                    || e.getAction() == MotionEvent.ACTION_UP
//                    || e.getAction() == MotionEvent.ACTION_SCROLL) {
//                handler.removeCallbacks(mLongPressed);
//            }
//            return false;
//        }
    }


    final Handler handler = new Handler();
    Runnable mLongPressed = new Runnable() {
        public void run() {
            if(getUser().globalUserId == Instance.globalUserId) {
                Log.d(TAG, "Long press on Item[" + getUniqueItemId() + "]");
                CCLog.write(Event.ITEM_LONG_PRESS, "{uniqueItemId=" + getUniqueItemId() + "}");
                Item.this.onLongPress();
            } else {
                TimelineActivity.getInstance().promptAdd(getDrawnX() + mLongPressX);
            }
        }
    };

    protected abstract void onSelect(boolean editable, boolean deletable);
}
