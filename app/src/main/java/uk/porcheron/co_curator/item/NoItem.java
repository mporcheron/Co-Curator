package uk.porcheron.co_curator.item;

import android.content.Context;
import android.util.AttributeSet;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;

/**
 * Created by map on 17/08/15.
 */
public class NoItem extends Item {

    private static NoItem mInstance;

    public static NoItem getInstance() {
        if(mInstance == null) {
            mInstance = new NoItem(TimelineActivity.getInstance());
        }

        return mInstance;
    }

    private NoItem(Context context) {
        super(context);
        setBounds(0, 0, 0);
    }

    private NoItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBounds(0, 0, 0);
    }

    private NoItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setBounds(0, 0, 0);
    }

    private NoItem(User user, int itemId, int dateTime) {
        super(user, itemId, dateTime);
        setBounds(0, 0, 0);
    }

    @Override
    protected boolean onTap() {
        return false;
    }
}
