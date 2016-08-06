package uk.porcheron.co_curator.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.IOException;

import uk.porcheron.co_curator.val.Phone;
import uk.porcheron.co_curator.val.Style;

/**
 * Dialog for Note items.
 */
public class DialogPhoto extends AbstractDialog {
    private static final String TAG = "CC:DialogNote";

    private static int LINES = 10;
    private static int ALERT_SCALE_SIZE = 3;
    private static float TEXT_SCALE_SIZE = 1.6f;

    private boolean mAutoEdit = false;

    private float mSourceWidth;
    private float mSourceHeight;

    private String mSource = "";
    private final ImageView mImageView;

    public DialogPhoto(Activity activity) {
        super(activity);

        mImageView = new ImageView(getActivity());
        mImageView.setClickable(true);
        setContentView(mImageView, true);
    }

    @Override
    protected int width() {
        return (int) mSourceWidth;
    }

    @Override
    protected int height() {
        return (int) mSourceHeight;
    }

    protected ImageView getImageView() {
        return mImageView;
    }

    public DialogPhoto setSource(String source) throws IOException {
        mSource = source;

        FileInputStream fis = getActivity().openFileInput(source);
        Bitmap b = BitmapFactory.decodeStream(fis);
        mImageView.setImageBitmap(b);
        fis.close();

        if(b == null) {
            return this;
        }

        float maxWidth = Phone.screenWidth - (Style.dialogMinXSpace * 2);
        float maxHeight = Phone.screenHeight - (Style.dialogMinYSpace * 2);

        float imageWidth = b.getWidth();
        float imageHeight = b.getHeight();

        float finalWidth = imageWidth;
        float finalHeight = imageHeight;

        if(finalWidth > maxWidth) {
            float ratio = maxWidth / imageWidth;

            finalWidth = maxWidth;
            finalHeight = imageHeight * ratio;
        }

        if(finalHeight > maxHeight) {
            float ratio = maxHeight / finalHeight;

            finalHeight = maxHeight;
            finalWidth = finalWidth * ratio;
        }

        mSourceWidth = finalWidth;
        mSourceHeight = finalHeight;

        return this;
    }

    @Override
    protected void setStyle(Dialog dialog) {
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.getWindow().setLayout(width(), height());
//
//        int padding = (int) 60;
//        mImageView.setPadding(padding, 0, padding, 0);
    }

    @Override
    protected void onClose(DialogInterface dialog, OnSubmitListener onSubmitListener, OnCancelListener onCancelListener, OnDeleteListener onDeleteListener, boolean flung) {
        Log.d(TAG, "onClose");
        if(flung && onDeleteListener != null) {
            onDeleteListener.onDelete(dialog);
        } else if(onCancelListener != null) {
            onCancelListener.onCancel(dialog);
        }

        super.onClose(dialog, onSubmitListener, onCancelListener, onDeleteListener, flung);
    }

    @Override
    protected boolean onTap() {
        Log.e(TAG, "open in photos app");
        return true;
    }
}
