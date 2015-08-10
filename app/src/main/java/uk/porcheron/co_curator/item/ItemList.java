package uk.porcheron.co_curator.item;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.db.DbHelper;
import uk.porcheron.co_curator.db.TableItem;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.Style;
import uk.porcheron.co_curator.util.UData;

/**
 * List of items
 *
 * Created by map on 07/08/15.
 */
public class ItemList extends ArrayList<ItemContainer> {
    private static final String TAG = "CC:ItemList";

    private Context mContext;
    private DbHelper mDbHelper;

    private int mGlobalUserId;
    private UserList mUsers;

    private LinearLayout mLayoutAbove;
    private LinearLayout mLayoutCentre;
    private LinearLayout mLayoutBelow;

    public ItemList(TimelineActivity context, LinearLayout layoutAbove,  LinearLayout layoutCentre, LinearLayout layoutBelow) {
        mContext = context;
        mDbHelper = context.getDbHelper();
        mLayoutAbove = layoutAbove;
        mLayoutCentre = layoutCentre;
        mLayoutBelow = layoutBelow;
    }

    public boolean add(int itemId, ItemType type, User user, Object data) {
        return add(itemId, type, user, data, false);
    }

    public boolean add(int itemId, ItemType type, User user, Object data, boolean localOnly) {
        Log.d(TAG, "Add item " + itemId + " to the view (localOnly=" + localOnly + ")");

        // Context
        boolean above = size() % 2 == 0;
        ViewGroup view;
        if (above) {
            view = mLayoutAbove;
        } else {
            view = mLayoutBelow;
        }

        // Instance
        Item item = null;
        if(type == ItemType.NOTE) {
            item = createNote(mContext, itemId, user, (String) data);
        } else if(type == ItemType.URL) {
            item = createURL(mContext, itemId, user, (String) data);
        } else if(type == ItemType.PHOTO && data instanceof String) {
            item = createImage(mContext, view, itemId, user, (String) data);
        } else if(type == ItemType.PHOTO && data instanceof Bitmap) {
            item = createImage(mContext, view, itemId, user, (Bitmap) data);
        }

        if(item == null) {
            Log.e(TAG, "Unsupported item type: " + type.getLabel());
            return false;
        }

        Log.v(TAG, "Create item container");
        ItemContainer container = new ItemContainer(mContext, item, user, above);

        Log.v(TAG, "Add item to container");
        add(container);

        Log.v(TAG, "Add container to the view");

        // Drawing
        mLayoutCentre.addView(container.getNotch());
        view.addView(container);

        // Local Database
        if(localOnly) {
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
                out = mContext.openFileOutput((String) data, Context.MODE_PRIVATE);
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

    private ItemNote createNote(Context context,int itemId, User user, String text) {
        ItemNote note = new ItemNote(context, itemId, user);
        note.setText(text);
        return note;
    }

    private ItemURL createURL(Context context,int itemId, User user, String url) {
        ItemURL note = new ItemURL(context, itemId, user);
        note.setURL(url);
        return note;
    }

    private ItemImage createImage(Context context, ViewGroup vg, int itemId, User user, String imagePath) {
        ItemImage image = new ItemImage(context, vg, itemId, user);
        image.setImagePath(imagePath);
        return image;
    }

    private ItemImage createImage(Context context, ViewGroup vg, int itemId, User user, Bitmap bitmap) {
        ItemImage image = new ItemImage(context, vg, itemId, user);
        image.setBitmap(bitmap);
        return image;
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

}
