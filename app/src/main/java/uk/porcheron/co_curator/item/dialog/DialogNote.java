package uk.porcheron.co_curator.item.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

import uk.porcheron.co_curator.val.Style;

/**
 * Dialog for Note items.
 */
public class DialogNote extends AbstractDialog {
    private static final String TAG = "CC:DialogNote";

    private static int LINES = 10;
    private static float ALERT_SCALE_SIZE = 2.5f;
    private static float TEXT_SCALE_SIZE = 2f;

    private boolean mAutoEdit = false;

    private String mOriginalText = "";
    private final EditText mEditText;

    public DialogNote(Activity activity) {
        super(activity);

        mEditText = new EditText(getActivity());
        mEditText.setSingleLine(false);
        mEditText.setHorizontallyScrolling(false);
        setContentView(mEditText);
    }

    @Override
    protected int width() {
        return (int) (ALERT_SCALE_SIZE * Style.noteWidth);
    }

    @Override
    protected int height() {
        return (int) (ALERT_SCALE_SIZE * Style.noteHeight);
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

        if(isEditable() && mAutoEdit) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            mEditText.requestFocus();
        }
    }

    @Override
    protected void setStyle(Dialog dialog) {
        super.setStyle(dialog);

        int bgColor = getUser().bgColor;
        int fgColor = getUser().fgColor;

        mEditText.setBackgroundColor(bgColor);
        mEditText.setTextColor(fgColor);

        int padding = (int) (ALERT_SCALE_SIZE * Style.notePadding);
        mEditText.setPadding(padding, padding, padding, padding);
        mEditText.setGravity(View.TEXT_ALIGNMENT_CENTER);
        mEditText.setLineSpacing(0, Style.noteLineSpacing);
        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PT, TEXT_SCALE_SIZE * Style.noteFontSize);
        mEditText.setSelection(mEditText.getText().toString().length());

        int maxLength = Style.noteLength;
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

        mEditText.setCursorVisible(false);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                mEditText.setCursorVisible(false);
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return false;
            }
        });

        if(!isEditable()) {
            mEditText.setInputType(InputType.TYPE_NULL);
        }

        mEditText.setLines(LINES);

        //setCursorDrawableColor(mEditText, fgColor);
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
    protected boolean onTap() {
        if(isEditable()) {
            mEditText.setCursorVisible(true);
//
//            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            mEditText.requestFocus();
            return true;
        }

        return false;
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
}
