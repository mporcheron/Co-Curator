package uk.porcheron.co_curator.item;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;

import uk.porcheron.co_curator.ImageDialogActivity;
import uk.porcheron.co_curator.R;
import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.item.dialog.DialogNote;
import uk.porcheron.co_curator.item.dialog.DialogPhoto;
import uk.porcheron.co_curator.item.dialog.DialogUrl;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.CCLog;
import uk.porcheron.co_curator.util.Event;
import uk.porcheron.co_curator.util.Image;
import uk.porcheron.co_curator.util.Web;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.val.Style;

/**
 * An item that contains text.
 */
public class ItemPhoto extends Item {
    private static final String TAG = "CC:ItemImage";

    private TimelineActivity mActivity;

    private Bitmap mBitmapThumbnail = null;
    private String mImagePath;

    public ItemPhoto(Context context) { super(context); }

    public ItemPhoto(Context context, AttributeSet attrs) { super(context, attrs); }

    public ItemPhoto(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    public ItemPhoto(int itemId, User user, int dateTime) {
        super(user, itemId, dateTime);

        mActivity = TimelineActivity.getInstance();

        setBounds(Style.photoWidth, Style.photoHeight, Style.photoPadding);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF b = getInnerBounds();

        if(mBitmapThumbnail == null) {
            try {
                FileInputStream fis = getContext().openFileInput(mImagePath + "-thumb.png");
                mBitmapThumbnail = BitmapFactory.decodeStream(fis);
                fis.close();

                if (mBitmapThumbnail == null) {
                    Log.e(TAG, "Could not decode thumbnail file to bitmap " + mImagePath);
                }
            } catch (Exception e) {
                Log.e(TAG, "Could not open " + mImagePath + "-thumb");
            }
        }

        if (mBitmapThumbnail != null) {
            canvas.drawBitmap(mBitmapThumbnail, b.left, b.top, Style.normalPaint);
            mBitmapThumbnail = null;
        } else {
            canvas.drawRect(b, Style.normalPaint);
        }

    }

    @Override
    public String getData() { return mImagePath; }

    @Override
    public String setData(String imagePath) {
        mImagePath = imagePath;
        mBitmapThumbnail = null;
        return imagePath;
    }

    @Override
    public boolean onTap() {
        return onLongPress();
    }

    @Override
    protected boolean onLongPress() {
        boolean userMatches = getUser().equals(Instance.user());
        onSelect(false, userMatches);

        return true;
    }

    @Override
    protected void onSelect(boolean editable, boolean deletable) {
        boolean userMatches = getUser().equals(Instance.user());

        try {
            new DialogPhoto()
                    .setSource(mImagePath + ".png")
                    .setOnDeleteListener(new DialogNote.OnDeleteListener() {
                        @Override
                        public void onDelete(DialogInterface dialog) {
                            CCLog.write(Event.ITEM_DELETE, "{uniqueItemId=" + getUniqueItemId() + "}");
                            Instance.items.remove(ItemPhoto.this, true, true, true);
                        }
                    })
                    .setUser(getUser())
                    .isDeletable(deletable)
                    .isEditable(editable)
                    .create()
                    .show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static int getThumbnailWidth() {
        return (int) (Style.photoWidth - (2 * Style.photoPadding));
    }

    public static int getThumbnailHeight() {
        return (int) (Style.photoHeight - (2 * Style.photoPadding));
    }

}
