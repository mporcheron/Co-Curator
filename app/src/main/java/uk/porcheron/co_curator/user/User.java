package uk.porcheron.co_curator.user;

import android.graphics.Paint;

import uk.porcheron.co_curator.util.Style;

/**
 * Created by map on 08/08/15.
 */
public class User {
    private static final String TAG = "CC:User";

    public int userId = 0;
    public int color = 0;
    public float offset = 0;
    public Paint paint;

    public User(int userId) {
        this.userId = userId;
        this.offset = (float) Math.ceil(userId % 2 == 0 ? -userId/2f : userId/2f) * Style.userOffset;
        this.color = Style.userColours[userId];

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        this.paint = paint;
    }
}