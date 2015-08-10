package uk.porcheron.co_curator.line;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.View;

import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Style;

/**
 * Created by map on 09/08/15.
 */
public class Notch extends View {
    private static final String TAG = "CC:Notch";

    private User mUser;
    private RectF mBounds;

    public Notch(Context context, User user, boolean above) {
        super(context);

        mUser = user;

        mBounds = new RectF();
        mBounds.left = (Style.itemWidth / 2) - Style.lineWidth;
        mBounds.right = mBounds.left + Style.lineWidth;

        if(above) {
            mBounds.top = 0;
            mBounds.bottom = (Style.layoutCentreHeight / 2) + user.offset;
        } else {
            mBounds.top = (Style.layoutCentreHeight / 2) + user.offset;
            mBounds.bottom = Style.layoutCentreHeight;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(Style.itemFullWidth / 2, Style.layoutCentreHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawRect(mBounds.left, mBounds.top, mBounds.right, mBounds.bottom, mUser.paint);
    }
}
