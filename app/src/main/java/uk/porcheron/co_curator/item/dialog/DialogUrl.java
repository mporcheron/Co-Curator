package uk.porcheron.co_curator.item.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.InputType;
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
 * Dialog for Url items.
 */
public class DialogUrl extends DialogNote {
    private static final String TAG = "CC:DialogUrl";

    public DialogUrl() {
        super();

        getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_URI);
    }
}
