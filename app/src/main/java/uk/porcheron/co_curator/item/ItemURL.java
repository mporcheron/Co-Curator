package uk.porcheron.co_curator.item;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;

import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Web;
import uk.porcheron.co_curator.val.Style;

/**
 * An item that contains a URL.
 */
public class ItemUrl extends ItemPhoto {
    private static final String TAG = "CC:ItemURL";

    private String mURL;

    private boolean mIsVideo = false;
    private static String[] YOUTUBE_URLS = {
            "http://m.youtube.com",
            "https://m.youtube.com",
            "http://www.youtube.com",
            "https://www.youtube.com",
            "http://youtube.com",
            "https://youtube.com",
            "http://youtu.be",
            "https://youtu.be"
    };

    public ItemUrl(Context context) { super(context); }

    public ItemUrl(Context context, AttributeSet attrs) { super(context, attrs); }

    public ItemUrl(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    public ItemUrl(int itemId, User user, int dateTime) {
        super(itemId, user, dateTime);

        setBounds(Style.photoWidth, Style.photoHeight, Style.photoPadding);
    }

    public String setData(String url) {
        mURL = url;
        mIsVideo = isVideo(url);

        if(mIsVideo) {
            setBounds(Style.videoWidth, Style.videoHeight, Style.videoPadding);
        } else {
            setBounds(Style.urlWidth, Style.urlHeight, Style.urlPadding);
        }

        return super.setData(Web.b64encode(url));
    }

    @Override
    public boolean onTap() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mURL));
        getContext().startActivity(browserIntent);
        return true;
    }

    public static boolean isVideo(String url) {
        for (String youtubeUrl : YOUTUBE_URLS) {
            if (url.startsWith(youtubeUrl)) {
                return true;
            }
        }
        return false;
    }

    public static int getThumbnailWidth(boolean isVideo) {
        if (isVideo) {
            return (int) (Style.videoWidth - (2 * Style.videoPadding));
        } else {
            return (int) (Style.urlWidth - (2 * Style.urlPadding));
        }
    }

    public static int getThumbnailHeight(boolean isVideo) {
        if (isVideo) {
            return (int) (Style.videoHeight - (2 * Style.videoPadding));
        } else {
            return (int) (Style.urlHeight - (2 * Style.urlPadding));
        }
    }

    public static int getThumbnailWidth() {
        return getThumbnailWidth(false);
    }

    public static int getThumbnailHeight() {
        return getThumbnailHeight(false);
    }
}
