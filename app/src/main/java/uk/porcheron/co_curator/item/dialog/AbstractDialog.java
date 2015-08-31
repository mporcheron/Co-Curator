package uk.porcheron.co_curator.item.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

import uk.porcheron.co_curator.R;
import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.val.Style;

/**
 * Abstract custom dialog.
 */
public abstract class AbstractDialog {
    private static final String TAG = "CC:AbstractDialog";

    private TimelineActivity mActivity;

    private OnSubmitListener mOnSubmitListener = null;
    private OnCancelListener mOnCancelListener = null;
    private OnDeleteListener mOnDeleteListener = null;
    private boolean mFlung = false;

    private static float X_LEEWAY = 100;

    private User mUser = null;
    private boolean mEditable = false;
    private boolean mDeletable = false;

    private final AlertDialog.Builder mBuilder;
    private AlertDialog mDialog = null;
    private final GestureDetector mGestureDetector;

    public AbstractDialog() {
        mActivity = TimelineActivity.getInstance();
        mBuilder = new AlertDialog.Builder(mActivity);
        mGestureDetector = new GestureDetector(TimelineActivity.getInstance(), new DialogGestureListener());
    }

    protected GestureDetector getGestureDetector() {
        return mGestureDetector;
    }

    protected final void setContentView(View view) {
        setContentView(view, false);
    }

    protected final User getUser() {
        return mUser == null ? Instance.user() : mUser;
    }

    public final AbstractDialog setUser(User user) {
        mUser = user;
        return this;
    }

    public final AbstractDialog isEditable(boolean editable) {
        mEditable = editable;
        return this;
    }

    protected final boolean isEditable() {
        return mEditable;
    }

    public final AbstractDialog isDeletable(boolean deletable) {
        mDeletable = deletable;
        return this;
    }

    protected final boolean isDeletable() {
        return mDeletable;
    }

    protected final void setContentView(View view, boolean tHack) {
        setView(view);
        if(tHack) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mGestureDetector.onTouchEvent(event);
                    return true;
                }
            });
        } else {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }
    }

    protected final AbstractDialog setView(View view) {
        mBuilder.setView(view);
        return this;
    }

    public final AbstractDialog setOnSubmitListener(final OnSubmitListener onSubmitListener) {
        mOnSubmitListener = onSubmitListener;
        return this;
    }

    public final AbstractDialog setOnCancelListener(final OnCancelListener onCancelListener) {
        mOnCancelListener = onCancelListener;
        return this;
    }

    public final AbstractDialog setOnDeleteListener(final OnDeleteListener onDeleteListener) {
        mOnDeleteListener = onDeleteListener;
        return this;
    }

    protected final TimelineActivity getActivity() {
        return mActivity;
    }

    protected abstract void onClose(DialogInterface dialog, OnSubmitListener onSubmitListener, OnCancelListener onCancelListener, OnDeleteListener onDeleteListener, boolean flung);

    public AbstractDialog create() {
        mBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onClose(dialog, mOnSubmitListener, mOnCancelListener, mOnDeleteListener, mFlung);
            }
        });


        mDialog = mBuilder.create();
        mDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mDialog.dismiss();
                    return true;
                }
                return false;
            }
        });

        return this;
    }

    protected abstract int width();
    protected abstract int height();

    public void show() {
        if(mDialog == null) {
            create();
        }

        int width = width();
        int height = height();

        mDialog.show();
        mDialog.getWindow().setLayout(width, height);

        setStyle(mDialog);
    }

    protected void setStyle(Dialog dialog) {
        int bgColor = getUser().bgColor;

        int objeto = mActivity.getResources().getIdentifier("buttonPanel","id","android");
        View vistaObjeto = mDialog.findViewById(objeto);
        if (vistaObjeto != null){
            vistaObjeto.setBackgroundColor(bgColor);
        }

        objeto = mActivity.getResources().getIdentifier("topPanel", "id", "android");
        vistaObjeto = mDialog.findViewById(objeto);
        if (vistaObjeto != null){
            vistaObjeto.setBackgroundColor(bgColor);
        }

        objeto = mActivity.getResources().getIdentifier("alertTitle","id","android");
        vistaObjeto = mDialog.findViewById(objeto);
        if (vistaObjeto != null){
            ((TextView)vistaObjeto).setTextColor(bgColor);
        }

        objeto = mActivity.getResources().getIdentifier("titleDivider","id","android");
        vistaObjeto = mDialog.findViewById(objeto);
        if (vistaObjeto != null){
            vistaObjeto.setBackgroundColor(bgColor);
        }

        objeto = mActivity.getResources().getIdentifier("contentPanel","id","android");
        vistaObjeto = mDialog.findViewById(objeto);
        if (vistaObjeto != null){
            vistaObjeto.setBackgroundColor(bgColor);
        }

        objeto = mActivity.getResources().getIdentifier("buttonPanel","id","android");
        vistaObjeto = mDialog.findViewById(objeto);
        if (vistaObjeto!=null){
            vistaObjeto.setBackgroundColor(bgColor);
        }

        objeto = mActivity.getResources().getIdentifier("parentPanel", "id", "android");
        vistaObjeto = mDialog.findViewById(objeto);
        if (vistaObjeto != null){
            vistaObjeto.setBackgroundColor(bgColor);
        }
    }


    private static void setCursorDrawableColor(EditText editText, int color) {
        try {
            Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            fCursorDrawableRes.setAccessible(true);
            int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);
            Field fEditor = TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            Object editor = fEditor.get(editText);
            Class<?> clazz = editor.getClass();
            Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);
            Drawable[] drawables = new Drawable[2];
            drawables[0] = editText.getContext().getResources().getDrawable(mCursorDrawableRes, null);
            drawables[1] = editText.getContext().getResources().getDrawable(mCursorDrawableRes, null);
            drawables[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            drawables[1].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            fCursorDrawable.set(editor, drawables);
        } catch (final Throwable ignored) {
        }
    }

    public interface OnSubmitListener {

        void onSubmit(DialogInterface dialog, String text);

    }

    public interface OnCancelListener extends DialogInterface.OnCancelListener {

    }

    public interface OnDeleteListener {

        void onDelete(DialogInterface dialog);

    }

    protected boolean onTap() {
        return false;
    }

    private static final int MIN_X = 200;

    class DialogGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(!mDeletable) {
                return false;
            }

            if(e1.getX() < e2.getX() + MIN_X) {
                return false;
            }

//            if(Math.abs(e1.getX() - e2.getX()) > X_LEEWAY) {
//                return false;
//            }

            mDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
            mFlung = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDialog.cancel();
                }
            }, 100);

            return true;
        }

    }

}
