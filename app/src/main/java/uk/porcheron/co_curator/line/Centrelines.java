package uk.porcheron.co_curator.line;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.Style;
import uk.porcheron.co_curator.util.UData;

/**
 * Created by map on 08/08/15.
 */
public class Centrelines implements SurfaceHolder.Callback {
    private static final String TAG = "CC:Centrelines";

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Style.backgroundColor);

        int w = canvas.getWidth();
        int h = canvas.getHeight();
        for(User user : UData.users){
            int y1 = (int) ((h / 2) - (Style.lineWidth / 2) + user.offset);
            int y2 = (int) (y1 + Style.lineWidth);

            canvas.drawRect(0, y1, w, y2, user.paint);
        }

        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
