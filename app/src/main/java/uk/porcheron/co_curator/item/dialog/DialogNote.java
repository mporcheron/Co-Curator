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
 * Dialog for Note items.
 */
public class DialogNote extends AbstractDialog {
    private static final String TAG = "CC:DialogNote";

    private static int LINES = 10;
    private static int ALERT_SCALE_SIZE = 3;
    private static float TEXT_SCALE_SIZE = 1.5f;

    private boolean mAutoEdit = false;

    private String mOriginalText = "";
    private final EditText mEditText;

    public DialogNote() {
        super();

        mEditText = new EditText(getActivity());
        mEditText.setSingleLine(false);
        setView(mEditText);
    }

    @Override
    protected int width() {
        return ALERT_SCALE_SIZE * (int) Style.noteWidth;
    }

    @Override
    protected int height() {
        return ALERT_SCALE_SIZE * (int) Style.noteHeight;
    }

    protected EditText getEditText() {
        return mEditText;
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

    @Override
    public void show() {
        super.show();

        if(mAutoEdit) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            mEditText.requestFocus();
        }
    }

    @Override
    protected void setStyle(int bgColor, int fgColor) {
        super.setStyle(bgColor, fgColor);

        mEditText.setBackgroundColor(bgColor);
        mEditText.setTextColor(fgColor);

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

    @Override
    protected void onClose(DialogInterface dialog, OnSubmitListener onSubmitListener, OnCancelListener onCancelListener, OnDeleteListener onDeleteListener, boolean flung) {
        final String text = mEditText.getText().toString();
        if(text.isEmpty() || text.equals(mOriginalText) || flung) {
            if(flung && onDeleteListener != null) {
                onDeleteListener.onDelete(dialog);
            } else if(onCancelListener != null) {
                onCancelListener.onCancel(dialog);
            }
        } else if(onSubmitListener != null) {
            onSubmitListener.onSubmit(dialog, text);
        }
    }

    @Override
    protected boolean onTap() {
        mEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        return true;
    }
}
