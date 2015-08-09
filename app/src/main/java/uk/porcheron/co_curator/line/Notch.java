package uk.porcheron.co_curator.line;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Style;

/**
 * Created by map on 09/08/15.
 */
public class Notch extends View {
    private static final String TAG = "CC:Notch";

    protected User mUser;

    protected float mNotchX1;
    protected float mNotchY1;
    protected float mNotchX2;
    protected float mNotchY2;

    public Notch(Context context, User user, boolean above) {
        super(context);

        mUser = user;

        mNotchX1 = (Style.itemWidth / 2) - (Style.lineWidth / 2);
        mNotchX2 = mNotchX1 + Style.lineWidth;

        if(above) {
            mNotchY1 = 0;
            mNotchY2 = (Style.layoutCentreHeight / 2) + user.offset;
        } else {
            mNotchY1 = (Style.layoutCentreHeight / 2) + user.offset;
            mNotchY2 = Style.layoutCentreHeight;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(Style.itemFullWidth / 2, Style.layoutCentreHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawRect(mNotchX1, mNotchY1, mNotchX2, mNotchY2, mUser.paint);
    }
}
