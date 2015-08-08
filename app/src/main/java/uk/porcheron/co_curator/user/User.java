package uk.porcheron.co_curator.user;

import android.graphics.Paint;

import uk.porcheron.co_curator.util.Style;

/**
 * Created by map on 08/08/15.
 */
public class User {
    public int userId = 0;
    public int colour = 0;
    public float offset = 0;
    public Paint paint;

    public User(int userId) {
        this.userId = userId;
        this.offset = (float) Math.ceil(userId % 2 == 0 ? -userId/2f : userId/2f) * Style.userOffset;
        this.colour = Style.userColours[userId];

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colour);
        this.paint = paint;
    }
}