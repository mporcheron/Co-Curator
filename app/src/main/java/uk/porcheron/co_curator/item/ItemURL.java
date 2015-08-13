package uk.porcheron.co_curator.item;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;

/**
 * An item that contains a URL.
 */
public class ItemURL extends ItemNote implements View.OnClickListener {
    private static final String TAG = "CC:ItemURL";

    private String mURL;

    public ItemURL(Context context) { super(context); }

    public ItemURL(Context context, AttributeSet attrs) { super(context, attrs); }

    public ItemURL(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    public ItemURL(int itemId, User user, String dateTime) {
        super(itemId, user, dateTime);

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
