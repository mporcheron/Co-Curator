package uk.porcheron.co_curator.item;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.LinearLayout;

import java.io.FileOutputStream;
import java.util.ArrayList;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.db.DbHelper;
import uk.porcheron.co_curator.db.TableItem;
import uk.porcheron.co_curator.line.StemConnector;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Style;
import uk.porcheron.co_curator.util.UData;

/**
 * List of items
 *
 * Created by map on 07/08/15.
 */
public class ItemList extends ArrayList<Item> {
    private static final String TAG = "CC:ItemList";

    private TimelineActivity mActivity;
    private DbHelper mDbHelper;

    private LinearLayout mLayoutAbove;
    private LinearLayout mLayoutCentre;
    private LinearLayout mLayoutBelow;

    public ItemList(TimelineActivity activity, LinearLayout layoutAbove,  LinearLayout layoutCentre, LinearLayout layoutBelow) {
        mActivity = activity;
        mDbHelper = activity.getDbHelper();
        mLayoutAbove = layoutAbove;
        mLayoutCentre = layoutCentre;
        mLayoutBelow = layoutBelow;
    }

    public boolean add(int itemId, ItemType type, User user, Object data) {
        return add(itemId, type, user, data, false);
    }

    public boolean add(int itemId, ItemType type, User user, Object data, boolean drawOnly) {
        Log.d(TAG, "Add item " + itemId + " to the view (drawOnly=" + drawOnly + ")");

        // Create the item
        Item item = null;
        if(type == ItemType.NOTE) {
            item = createNote(itemId, user, (String) data);
        } else if(type == ItemType.URL) {
            item = createURL(itemId, user, (String) data);
        } else if(type == ItemType.PHOTO && data instanceof String) {
            item = createImage(itemId, user, (String) data);
        } else if(type == ItemType.PHOTO && data instanceof Bitmap) {
            item = createImage(itemId, user, (Bitmap) data);
        }

        if(item == null) {
            Log.e(TAG, "Unsupported item type: " + type.getLabel());
            return false;
        }

        add(item);

        // Drawing
        if (user.above) {
            mLayoutAbove.addView(item, item.getWidth(), (int) Style.itemFullHeight);
        } else {
            mLayoutBelow.addView(item, item.getWidth(), (int) Style.itemFullHeight);
        }
        mLayoutCentre.addView(new StemConnector(mActivity, user, item.getmStemConnectorBounds()));

        // Save to the Local Database or just draw?
        if(drawOnly) {
            return true;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TableItem.COL_GLOBAL_USER_ID, user.globalUserId);
        values.put(TableItem.COL_ITEM_TYPE, type.getTypeId());

        if(type == ItemType.PHOTO) {
            FileOutputStream out;
            Bitmap bitmap = (Bitmap) data;
            data = UData.globalUserId + "-" + UData.userId + "-" + System.currentTimeMillis() + ".jpg";

            try {
                Log.v(TAG, "Save image to local storage");
                out = mActivity.openFileOutput((String) data, Context.MODE_PRIVATE);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.close();
                Log.v(TAG, "Image saved to local storage");
            } catch (Exception e) {
                Log.e(TAG, "Could not save image");
                e.printStackTrace();
            }
        }

        values.put(TableItem.COL_ITEM_DATA, data.toString());

        long newRowId;
        newRowId = db.insert(
                TableItem.TABLE_NAME,
                null,
                values);

        db.close();

        if(newRowId >= 0) {
            Log.d(TAG, "Item " + newRowId + " created in DB");
            return true;
        } else {
            Log.e(TAG, "Could not create item in DB");
            return false;
        }
    }

    private ItemNote createNote(int itemId, User user, String text) {
        ItemNote note = new ItemNote(mActivity, itemId, user);
        note.setText(text);
        return note;
    }

    private ItemURL createURL(int itemId, User user, String url) {
        ItemURL note = new ItemURL(mActivity, itemId, user);
        note.setURL(url);
        return note;
    }

    private ItemImage createImage(int itemId, User user, String imagePath) {
        ItemImage image = new ItemImage(mActivity, itemId, user);
        image.setImagePath(imagePath);
        return image;
    }

    private ItemImage createImage(int itemId, User user, Bitmap bitmap) {
        ItemImage image = new ItemImage(mActivity, itemId, user);
        image.setBitmap(bitmap);
        return image;
    }

}
