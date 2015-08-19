package uk.porcheron.co_curator.item.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
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
 * Dialog for Note items.
 */
public class DialogNote {
    private static final String TAG = "CC:DialogNote";

    private TimelineActivity mActivity;

    private static int LINES = 10;
    private static int ALERT_SCALE_SIZE = 3;
    private static float TEXT_SCALE_SIZE = 1.5f;

    private OnSubmitListener mOnSubmitListener = null;
    private OnCancelListener mOnCancelListener = null;
    private OnDeleteListener mOnDeleteListener = null;
    private boolean mDeleted = false;

    private boolean mAutoEdit = false;

    private static float X_LEEWAY = 100;

    private String mOriginalText = "";

    private final AlertDialog.Builder mBuilder;
    private AlertDialog mDialog = null;
    private final EditText mEditText;


    public DialogNote() {
        mActivity = TimelineActivity.getInstance();

        mEditText = new EditText(mActivity);
        mBuilder = new AlertDialog.Builder(mActivity)
                .setView(mEditText);
    }

    protected EditText getEditText() {
        return mEditText;
    }

    public DialogNote setOnSubmitListener(final OnSubmitListener onSubmitListener) {
        mOnSubmitListener = onSubmitListener;
        return this;
    }

    public DialogNote setOnCancelListener(final OnCancelListener onCancelListener) {
        mOnCancelListener = onCancelListener;
        return this;
    }

    public DialogNote setOnDeleteListener(final OnDeleteListener onDeleteListener) {
        mOnDeleteListener = onDeleteListener;
        return this;
    }

    public DialogNote setAutoEdit(boolean value) {
        mAutoEdit = value;
        return this;
    }

    public DialogNote setText(String text) {
        mOriginalText = text;
        mEditText.setText(text);
        return this;
    }

    public DialogNote create() {
        mBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                final String text = mEditText.getText().toString();
                if(text.isEmpty() || text.equals(mOriginalText) || mDeleted) {
                    if(mDeleted && mOnDeleteListener != null) {
                        mOnDeleteListener.onDelete(dialog);
                    } else if(mOnCancelListener != null) {
                        mOnCancelListener.onCancel(dialog);
                    }
                } else if(mOnSubmitListener != null) {
                    mOnSubmitListener.onSubmit(dialog, text);
                }
            }
        });

        mDialog = mBuilder.create();

        return this;
    }

    public void show() {
        if(mDialog == null) {
            create();
        }

        int width = ALERT_SCALE_SIZE * (int) Style.noteWidth;
        int height = ALERT_SCALE_SIZE * (int) Style.noteHeight;

        mDialog.show();
        mDialog.getWindow().setLayout(width, height);


        User u = Instance.user();
        setStyle(u.bgColor, u.fgColor);

        if(mAutoEdit) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            mEditText.requestFocus();
        }
    }

    private void setStyle(int bgColor, int fgColor) {

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

        mEditText.setBackgroundColor(bgColor);
        mEditText.setTextColor(fgColor);

        mEditText.setSingleLine(false);
        mEditText.setLines(LINES);

        int padding = ALERT_SCALE_SIZE * (int) Style.notePadding;
        mEditText.setPadding(padding, padding, padding, padding);
        mEditText.setGravity(View.TEXT_ALIGNMENT_TEXT_START);
        mEditText.setLineSpacing(0, Style.noteLineSpacing);
        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PT, TEXT_SCALE_SIZE * Style.noteFontSize);
        mEditText.setSelection(mEditText.getText().toString().length());

        final GestureDetector gD  = new GestureDetector(TimelineActivity.getInstance(), new DialogGestureListener());
        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gD.onTouchEvent(event);
                return true;
            }
        });

        setCursorDrawableColor(mEditText, fgColor);
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

    class DialogGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(TAG, "SingleTapConfirmed");

            mEditText.requestFocus();

            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(Math.abs(e1.getX() - e2.getX()) > X_LEEWAY) {
                return false;
            }

            if(e1.getY() < e2.getY()) {
                return false;
            }

            mDialog.getWindow().setWindowAnimations(R.style.DialogAnimation);
            mDeleted = true;
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
