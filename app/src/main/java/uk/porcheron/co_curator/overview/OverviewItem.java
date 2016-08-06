package uk.porcheron.co_curator.overview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created by map on 31/08/15.
 */
public class OverviewItem extends FrameLayout {

    public OverviewItem(Context context) {
        super(context);
    }

    public OverviewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OverviewItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OverviewItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Set a square layout.
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
