package uk.porcheron.co_curator.dialog;

import android.app.Activity;
import android.text.InputType;

/**
 * Dialog for Url items.
 */
public class DialogUrl extends DialogNote {
    private static final String TAG = "CC:DialogUrl";

    public DialogUrl(Activity activity) {
        super(activity);

        getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_URI);
    }
}
