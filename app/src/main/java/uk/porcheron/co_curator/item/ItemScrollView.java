package uk.porcheron.co_curator.item;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.HorizontalScrollView;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.porcheron.co_curator.val.Instance;

/**
 * ScrollView for items.
 */
public class ItemScrollView extends HorizontalScrollView {

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

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub
    }

    protected void cancelLongPress(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Instance.items.cancelLongPress();
    }

}
