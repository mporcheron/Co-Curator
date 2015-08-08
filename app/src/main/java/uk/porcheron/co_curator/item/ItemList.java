package uk.porcheron.co_curator.item;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;

import java.util.ArrayList;

import uk.porcheron.co_curator.user.User;

/**
 * List of items
 *
 * Created by map on 07/08/15.
 */
public class ItemList extends ArrayList<Item> {
    private static final String TAG = "CC:ItemList";

    private Context mContext;
    private LinearLayout mLayoutAbove;
    private LinearLayout mLayoutBelow;

    public ItemList(Context context, LinearLayout layoutAbove,  LinearLayout layoutBelow) {
        mContext = context;
        mLayoutAbove = layoutAbove;
        mLayoutBelow = layoutBelow;
    }

    public void add(ItemType type, User user, String data) {
        boolean above = size() % 2 == 0;

        Item item = null;
        if(type == ItemType.NOTE) {
            item = createNote(data, user, above);
        } else {
            //...
        }

        if(item == null) {
            Log.e(TAG, "Unsupported item type: " + type.mLabel);
            return;
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1.0f;


        add(item);
        if (above) {
            //params.gravity = Gravity.TOP;
            //item.setLayoutParams(params);
            mLayoutAbove.addView(item);
        } else {
            //params.gravity = Gravity.TOP;
            //item.setLayoutParams(params);
            mLayoutBelow.addView(item);
        }
    }

    private NoteItem createNote(String text, User user, boolean above) {
        NoteItem note = new NoteItem(mContext, user, above);
        note.setText(text);
        return note;
    }

}
