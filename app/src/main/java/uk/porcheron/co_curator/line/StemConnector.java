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
public class StemConnector extends View {
    private static final String TAG = "CC:StemConnector";

    private User mUser;
    private RectF mBounds;

    public StemConnector(Context context, User user, RectF bounds) {
        super(context);

        mUser = user;
        mBounds = bounds;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) Style.noteWidth / 2, (int) Style.layoutCentreHeight);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawRect(mBounds, mUser.bgPaint);
    }
}
