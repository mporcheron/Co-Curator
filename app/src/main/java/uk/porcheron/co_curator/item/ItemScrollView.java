package uk.porcheron.co_curator.item;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.HorizontalScrollView;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.val.Instance;

/**
 * ScrollView for items.
 */
public class ItemScrollView extends HorizontalScrollView {
    private static final String TAG = "CC:ItemScrollView";

    public ItemScrollView(Context context) {
        super(context);
    }

    public ItemScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ItemScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        TimelineActivity.getInstance().testPointers(getScrollX(), getScrollX() + this.getWidth());
        return true;
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        TimelineActivity.getInstance().testPointers(getScrollX(), getScrollX() + this.getWidth());
    }

//    protected void cancelLongPress(int l, int t, int oldl, int oldt) {
//        super.onScrollChanged(l, t, oldl, oldt);
//        Instance.items.cancelLongPress();
//    }

}
