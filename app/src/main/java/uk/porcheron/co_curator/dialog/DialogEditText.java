package uk.porcheron.co_curator.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

/**
 * Created by map on 06/09/15.
 */
public class DialogEditText extends EditText {

    public DialogEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public DialogEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public DialogEditText(Context context) {
        super(context);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if(mOnBackListener != null) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getAction() == EditorInfo.IME_ACTION_DONE) {
                mOnBackListener.onBackPressed();
                return false;
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    private OnBackListener mOnBackListener = null;

    void setOnBackListener(OnBackListener onBackListener) {
        mOnBackListener = onBackListener;
    }

    interface OnBackListener {
        void onBackPressed();
    }

}