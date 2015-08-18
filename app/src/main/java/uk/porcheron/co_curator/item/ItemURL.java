package uk.porcheron.co_curator.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.IOException;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Image;
import uk.porcheron.co_curator.util.Web;
import uk.porcheron.co_curator.val.Phone;
import uk.porcheron.co_curator.val.Style;

/**
 * An item that contains a URL.
 */
public class ItemURL extends ItemImage {
    private static final String TAG = "CC:ItemURL";

    private String mURL;

    private boolean mIsVideo = false;
    private static String[] YOUTUBE_URLS = {
            "http://m.youtube.com",
            "https://m.youtube.com",
            "http://www.youtube.com",
            "https://www.youtube.com",
            "http://youtube.com",
            "https://youtube.com"
    };

    public ItemURL(Context context) { super(context); }

    public ItemURL(Context context, AttributeSet attrs) { super(context, attrs); }

    public ItemURL(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    public ItemURL(int itemId, User user, int dateTime) {
        super(itemId, user, dateTime);

        setBounds(Style.imageWidth, Style.imageHeight, Style.imagePadding);
    }

    public String setData(String url) {
        mURL = url;

        for (String youtubeUrl : YOUTUBE_URLS) {
            if (url.startsWith(youtubeUrl)) {
                mIsVideo = true;
                break;
            }
        }

        int imageWidth, imageHeight;
        url = Web.GET_WWW_SCREENSHOT + url;

        if (mIsVideo) {
            imageWidth = (int) (Style.videoWidth - (2 * Style.videoPadding));
            imageHeight = (int) (Style.videoHeight - (2 * Style.videoPadding));
            setBounds(Style.videoWidth, Style.videoHeight, Style.videoPadding);
        } else {
            imageWidth = (int) (Style.urlWidth - (2 * Style.urlPadding));
            imageHeight = (int) (Style.urlHeight - (2 * Style.urlPadding));
            setBounds(Style.urlWidth, Style.urlHeight, Style.urlPadding);
        }

        String filename = getUser().globalUserId + "-" + System.currentTimeMillis();

        return super.setData(ItemURL.urlToFile(url, imageWidth, imageHeight, getUser().globalUserId));
    }

    @Override
    public boolean onTap() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mURL));
        getContext().startActivity(browserIntent);
        return true;
    }
}
