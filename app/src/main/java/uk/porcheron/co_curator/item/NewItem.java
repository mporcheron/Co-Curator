package uk.porcheron.co_curator.item;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import uk.porcheron.co_curator.R;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.UData;

/**
 * Created by map on 09/08/15.
 */
public class NewItem {
    private static final String TAG = "CC:NewItem";

    public static void prompt(final Context context, final View view, final boolean forceAdd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_add_message);

        if(forceAdd) {
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    NewItem.prompt(context, view, forceAdd);
                }
            });
        } else {
            builder.setNegativeButton(R.string.dialog_add_negative, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
        }

        final ItemType[] types = ItemType.values();
        CharSequence[] typeLabels = new CharSequence[types.length];
        for(int i = 0; i < typeLabels.length; i++) {
            typeLabels[i] = context.getString(types[i].getLabel());
        }

        builder.setItems(typeLabels, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ItemType type = types[which];
                switch (type) {
                    case PHOTO:
                        Toast.makeText(context, "Photo", Toast.LENGTH_SHORT).show();
                        break;

                    case NOTE:
                        NewItem.note(context, view, forceAdd);
                        break;

                    case URL:
                        NewItem.url(context, view, forceAdd);
                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static void note(final Context context, final View view, final boolean promptOnCancel) {
        final EditText editText = new EditText(context);
        editText.setSingleLine(false);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.dialog_note_title))
                .setView(editText)
                .setPositiveButton(context.getString(R.string.dialog_note_positive), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = editText.getText().toString();
                        if(!NewItem.newNote(text)) {
                            NewItem.prompt(context, view, promptOnCancel);
                        }
                    }
                })
                .setNegativeButton(context.getString(R.string.dialog_note_negative), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(promptOnCancel) {
                            NewItem.prompt(context, view, true);
                        }
                    }
                })
                .create();

        dialog.show();
        editText.requestFocus();

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private static void url(final Context context, final View view, final boolean promptOnCancel) {
        final EditText editText = new EditText(context);
        editText.setSingleLine(false);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.dialog_url_title))
                .setView(editText)
                .setPositiveButton(context.getString(R.string.dialog_url_positive), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = editText.getText().toString();
                        if(!NewItem.newURL(text)) {
                            NewItem.prompt(context, view, promptOnCancel);
                        }
                    }
                })
                .setNegativeButton(context.getString(R.string.dialog_url_negative), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(promptOnCancel) {
                            NewItem.prompt(context, view, true);
                        }
                    }
                })
                .create();

        dialog.show();
        editText.requestFocus();

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private static boolean newNote(String text) {
        return UData.items.add(UData.items.size(), ItemType.NOTE, UData.user(), text);
    }

    private static boolean newURL(String url) {
        return UData.items.add(UData.items.size(), ItemType.URL, UData.user(), url);
    }
}
