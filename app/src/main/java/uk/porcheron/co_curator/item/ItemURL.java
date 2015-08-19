package uk.porcheron.co_curator.item;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;

import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.val.Style;

/**
 * An item that contains a URL.
 */
public class ItemURL extends ItemPhoto {
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

        setBounds(Style.photoWidth, Style.photoHeight, Style.photoPadding);
    }

    public String setData(String url) {
        mURL = url;

        for (String youtubeUrl : YOUTUBE_URLS) {
            if (url.startsWith(youtubeUrl)) {
                mIsVideo = true;
                break;
            }
        }

        return super.setData(url);
//        int photoWidth, photoHeight;
//        url = Web.GET_WWW_SCREENSHOT + url;
//
//        if (mIsVideo) {
//            photoWidth = (int) (Style.videoWidth - (2 * Style.videoPadding));
//            photoHeight = (int) (Style.videoHeight - (2 * Style.videoPadding));
//            setBounds(Style.videoWidth, Style.videoHeight, Style.videoPadding);
//        } else {
//            photoWidth = (int) (Style.urlWidth - (2 * Style.urlPadding));
//            photoHeight = (int) (Style.urlHeight - (2 * Style.urlPadding));
//            setBounds(Style.urlWidth, Style.urlHeight, Style.urlPadding);
//        }
//
//        String filename = getUser().globalUserId + "-" + System.currentTimeMillis();
//
//        return super.setData(ItemURL.urlToFile(url, photoWidth, photoHeight, getUser().globalUserId));
    }

    @Override
    public boolean onTap() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mURL));
        getContext().startActivity(browserIntent);
        return true;
    }
}
