package uk.porcheron.co_curator.item;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.EllipsizingTextView;
import uk.porcheron.co_curator.util.Style;

/**
 * An item that contains a URL.
 * <p/>
 * Created by map on 06/08/15.
 */
public class ItemURL extends ItemNote implements View.OnClickListener {
    private static final String TAG = "CC:ItemURL";

    private String mURL;

    public ItemURL(TimelineActivity activity, int itemId, User user) {
        super(activity, itemId, user);

        setOnClickListener(this);
    }

    public void setURL(String url) {
        mURL = url;
        setText(url.replace("http://", ""));
    }

    @Override
    public void onClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mURL));
        getContext().startActivity(browserIntent);
    }
}
