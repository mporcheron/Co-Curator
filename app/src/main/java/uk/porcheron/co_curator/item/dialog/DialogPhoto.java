package uk.porcheron.co_curator.item.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;

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

        mSourceWidth = Math.min(Phone.screenWidth - (Style.dialogMinXSpace * 2), b.getWidth());
        mSourceHeight = Math.min(Phone.screenHeight - (Style.dialogMinYSpace * 2), b.getHeight());

        return this;
    }

    @Override
    protected void setStyle(Dialog dialog) {
        //super.setStyle(bgColor, fgColor);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        Log.d(TAG, "Here be the DialogPhoto");

        int padding = (int) 60;
        mImageView.setPadding(padding, 0, padding, 0);
    }

    @Override
    protected void onClose(DialogInterface dialog, OnSubmitListener onSubmitListener, OnCancelListener onCancelListener, OnDeleteListener onDeleteListener, boolean flung) {
        if(flung && onDeleteListener != null) {
            onDeleteListener.onDelete(dialog);
        } else if(onCancelListener != null) {
            onCancelListener.onCancel(dialog);
        }
    }

    @Override
    protected boolean onTap() {
        Log.e(TAG, "open in photos app");
        return true;
    }
}
