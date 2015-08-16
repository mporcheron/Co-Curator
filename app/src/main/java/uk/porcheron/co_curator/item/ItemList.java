package uk.porcheron.co_curator.item;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.collo.ClientManager;
import uk.porcheron.co_curator.collo.ColloDict;
import uk.porcheron.co_curator.collo.ResponseHandler;
import uk.porcheron.co_curator.collo.ResponseManager;
import uk.porcheron.co_curator.db.DbHelper;
import uk.porcheron.co_curator.db.TableItem;
import uk.porcheron.co_curator.db.WebLoader;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.val.Phone;
import uk.porcheron.co_curator.val.Style;
import uk.porcheron.co_curator.util.Web;

/**
 * List of items
 * <p/>
 * Created by map on 07/08/15.
 */
public class ItemList extends ArrayList<Item> implements ResponseHandler {
    private static final String TAG = "CC:ItemList";

    private DbHelper mDbHelper;

    private Map<String, Item> mItemIds = new HashMap<>();
    private SparseArray<List<Item>> mItemGlobalUserIds = new SparseArray<>();

    private HorizontalScrollView mScrollView;
    private LinearLayout mLayoutAbove;
    private LinearLayout mLayoutBelow;

    private int mDrawn = 0;

    public static final int ITEM_DELETED = 1;
    public static final int ITEM_NOT_DELETED = 0;

    public ItemList(HorizontalScrollView scrollView, LinearLayout layoutAbove, LinearLayout layoutBelow) {
        mDbHelper = DbHelper.getInstance();
        mScrollView = scrollView;
        mLayoutAbove = layoutAbove;
        mLayoutBelow = layoutBelow;

        ResponseManager.registerHandler(ColloDict.ACTION_NEW, this);
        ResponseManager.registerHandler(ColloDict.ACTION_DELETE, this);
    }

    public boolean add(ItemType type, User user, String data, boolean deleted, boolean addToLocalDb, boolean addToCloud) {
        return add(size(), type, user, data, deleted, addToLocalDb, addToCloud);
    }

    public boolean add(int itemId, ItemType type, User user, String data, boolean deleted, boolean addToLocalDb, boolean addToCloud) {
        return add(itemId, type, user, data, (int) (System.currentTimeMillis() / 1000L), deleted, addToLocalDb, addToCloud);
    }

    public synchronized boolean add(int itemId, ItemType type, User user, String data, int dateTime, boolean deleted, boolean addToLocalDb, boolean addToCloud) {
        String uniqueItemId = user.globalUserId + "-" + itemId;

        Log.v(TAG, "Item[" + uniqueItemId + "]: Add to List (type=" + type + ",user=" + user.globalUserId +
                ",data=" + data + ",dateTime=" + dateTime + ",deleted=" + deleted + ",addToLocalDb=" + addToLocalDb +
                ",addToCloud=" + addToCloud + ")");

        // Create the item
        Item item = null;
        if (type == ItemType.NOTE) {
            item = createNote(itemId, user, dateTime, data);
        } else if (type == ItemType.URL) {
            item = createURL(itemId, user, dateTime, data);
        } else if (type == ItemType.PHOTO) {
            item = createImage(itemId, user, dateTime, data);
        }

        if (item == null) {
            Log.e(TAG, "Item[" + uniqueItemId + "]: Unsupported type: " + type.getLabel());
            return false;
        }

        item.setDeleted(deleted);

        int insertAt = 0, insertAtAbove = 0, insertAtBelow = 0;
        Iterator<Item> it = iterator();
        while(it.hasNext()) {
            Item j = it.next();
            if(j.getDateTime() > dateTime) {
                break;
            }

            insertAt++;
            if(user.above && user.draw()) {
                insertAtAbove++;
            } else if(user.draw()) {
                insertAtBelow++;
            }
        }

        mItemIds.put(user.globalUserId + "-" + item.getItemId(), item);
        add(insertAt, item);

        // List of user's items
        List<Item> items = mItemGlobalUserIds.get(user.globalUserId);
        if(items == null) {
            items = new ArrayList<>();
            mItemGlobalUserIds.put(user.globalUserId, items);
        }
        items.add(item);

        // Drawing
        if(!user.draw() || deleted) {
            Log.v(TAG, "Item[" + uniqueItemId + "]: Won't draw as user is not connected or us or is deleted");
        } else {
            drawItem(user, item, user.above ? insertAtAbove : insertAtBelow);
        }

        // Save to the Local Database or just draw?
        if (addToLocalDb) {
            Log.v(TAG, "Add item to local DB");
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(TableItem.COL_GLOBAL_USER_ID, user.globalUserId);
            values.put(TableItem.COL_ITEM_ID, itemId);
            values.put(TableItem.COL_ITEM_TYPE, type.getTypeId());
            values.put(TableItem.COL_ITEM_DATA, data);
            values.put(TableItem.COL_ITEM_DATETIME, dateTime);
            values.put(TableItem.COL_ITEM_UPLOADED, addToCloud ? TableItem.VAL_ITEM_WILL_UPLOAD : TableItem.VAL_ITEM_WONT_UPLOAD);
            values.put(TableItem.COL_ITEM_DELETED, deleted);

            long newRowId;
            newRowId = db.insert(
                    TableItem.TABLE_NAME,
                    null,
                    values);

            db.close();

            if (newRowId < 0) {
                Log.e(TAG, "Item[" + uniqueItemId + "]: Could not create in Db");
                return false;
            }

            Log.v(TAG, "Item[" + uniqueItemId + "]: Created in Db (rowId=" + newRowId + ")");
        }

        // Save to the Local Database or just draw?
        if (addToCloud && user.globalUserId == Instance.globalUserId) {
            Log.v(TAG, "Upload item to cloud");
            if(type == ItemType.PHOTO) {
                new PostImageToCloud(user.globalUserId, itemId, type, dateTime).execute(data);
            } else {
                new PostTextToCloud(user.globalUserId, itemId, type, dateTime).execute(data);
            }
        }

        return true;
    }

    private void drawItem(User user, Item item, int pos) {
        mDrawn++;

        int minWidth = Phone.screenWidth;
        if (user.above) {
            mLayoutAbove.addView(item, Math.min(mLayoutAbove.getChildCount(), pos));
            minWidth = Math.max(mLayoutAbove.getWidth() + item.getMeasuredWidth(), minWidth);
        } else {
            mLayoutBelow.addView(item, Math.min(mLayoutBelow.getChildCount(), pos));
            minWidth = Math.max(mLayoutBelow.getWidth() + item.getMeasuredWidth(), minWidth);
        }

        mLayoutAbove.setMinimumWidth(minWidth);
        mLayoutBelow.setMinimumWidth(minWidth);

        float minAutoScrollWidth = minWidth - Style.autoscrollSlack - Phone.screenWidth;
        if (user.globalUserId == Instance.globalUserId || minAutoScrollWidth <= mScrollView.getScrollX()) {
            final int targetX = minWidth;
            mScrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScrollView.smoothScrollTo(targetX, 0);
                }
            }, 500);
        }

        item.setDrawn(true);
    }

    public void remove(int globalUserId, int itemId, boolean removeFromLocalDb, boolean removeFromCloud, boolean notifyClients) {
        Item i = getByItemId(globalUserId, itemId);
        if(i == null) {
            return;
        }

        // Remove from list
        i.setDeleted(true);

        // Remove from local DB
        if(removeFromLocalDb) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(TableItem.COL_ITEM_DELETED, TableItem.VAL_ITEM_DELETED);

            String whereClause = TableItem.COL_GLOBAL_USER_ID + "=? AND " +
                    TableItem.COL_ITEM_ID + "=?";
            String[] whereArgs = {"" + globalUserId, "" + itemId};

            int rowsAffected = db.update(TableItem.TABLE_NAME, values, whereClause, whereArgs);
            if (rowsAffected != 1) {
                Log.e(TAG, "Failed to set deleted for Item[" + globalUserId + ":" + itemId + "]");
            }

            db.close();
        }

        // Remove from view
        TimelineActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Instance.items.retestDrawing();
            }
        });

        // Delete from cloud
        if(removeFromCloud) {
            new DeleteFromCloud(globalUserId, itemId).execute();
        }

        // Notify neighbours
        if(notifyClients) {
            ClientManager.postMessage(ColloDict.ACTION_DELETE, itemId);
        }
    }

    public void retestDrawing() {
        mDrawn = 0;
        int insertAtAbove = 0;
        int insertAtBelow = 0;
        for(Item item : this) {
            User user = item.getUser();

            if(user.draw() && !item.isDeleted()) {
                if(!item.isDrawn()) {
                    Log.v(TAG, "User[" + user.globalUserId + "] is " + Instance.drawnUsers + "th user, offset=" + user.offset);
                    drawItem(user, item, user.above ? insertAtAbove : insertAtBelow);
                    // draw
                }
            } else {
                if(item.isDrawn()) {
                    if(user.above) {
                        mLayoutAbove.removeView(item);
                    } else {
                        mLayoutBelow.removeView(item);
                    }
                    item.setDrawn(false);
                }
            }

            if(user.draw()) {
                if (user.above) {
                    insertAtAbove++;
                } else {
                    insertAtBelow++;
                }
            }

        }
    }

    private ItemNote createNote(int itemId, User user, int dateTime, String text) {
        ItemNote note = new ItemNote(itemId, user, dateTime);
        note.setText(text);
        return note;
    }

    private ItemURL createURL(int itemId, User user, int dateTime, String url) {
        ItemURL note = new ItemURL(itemId, user, dateTime);
        note.setURL(url);
        return note;
    }

    private ItemImage createImage(int itemId, User user, int dateTime, String imagePath) {
        ItemImage image = new ItemImage(itemId, user, dateTime);
        image.setImagePath(imagePath);
        return image;
    }

    public Item getByItemId(int globalUserId, int itemId) {
        return mItemIds.get(globalUserId + "-" + itemId);
    }

    @Override
    public boolean respond(String action, int globalUserId, String... data) {
        int itemId = -1;

        try {
            itemId = Integer.parseInt(data[0]);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid itemId received");
            return false;
        }

        switch(action) {
            case ColloDict.ACTION_NEW:
                WebLoader.loadItemFromWeb(globalUserId, itemId);
                return true;

            case ColloDict.ACTION_DELETE:
                remove(globalUserId, itemId, true, false, false);
                return true;
        }

        return false;
    }

    private class PostTextToCloud extends AsyncTask<String, Void, Boolean> {

        protected int mGlobalUserId;
        protected int mItemId;
        protected ItemType mItemType;
        protected int mDateTime;

        PostTextToCloud(int globalUserId, int itemId, ItemType itemType, int dateTime) {
            mGlobalUserId = globalUserId;
            mItemId = itemId;
            mItemType = itemType;
            mDateTime = dateTime;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("globalUserId", "" + mGlobalUserId));
            nameValuePairs.add(new BasicNameValuePair("itemId", "" + mItemId));
            nameValuePairs.add(new BasicNameValuePair("itemType", "" + mItemType.getTypeId()));
            nameValuePairs.add(new BasicNameValuePair("itemData", "" + params[0]));
            nameValuePairs.add(new BasicNameValuePair("itemDateTime", "" + mDateTime));

            JSONObject obj = Web.requestObj(Web.POST_ITEMS, nameValuePairs);
            return pushChanges(obj);
        }

        protected Boolean pushChanges(JSONObject request) {
            if(request == null || !request.has("success")) {
                Log.e(TAG, "Could not post item to cloud");
                return Boolean.FALSE;
            }

            Log.v(TAG, "Item successfully uploaded, notify clients");

            // Update local DB
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(TableItem.COL_ITEM_UPLOADED, TableItem.VAL_ITEM_UPLOADED);

            Log.e(TAG, "Set uploaded status to " + TableItem.VAL_ITEM_UPLOADED + " for " + mGlobalUserId + ":" + mItemId);
            String whereClause = TableItem.COL_GLOBAL_USER_ID + "=? AND " +
                    TableItem.COL_ITEM_ID + "=?";
            String[] whereArgs = {"" + mGlobalUserId, "" + mItemId};

            int rowsAffected = db.update(TableItem.TABLE_NAME, values, whereClause, whereArgs);
            if(rowsAffected != 1) {
                Log.e(TAG, "Failed to set uploaded for Item[" + mGlobalUserId + "-" + mItemId + "] - " + rowsAffected);
            }

            db.close();

            // Tell collocated devices
            ClientManager.postMessage(ColloDict.ACTION_NEW, mItemId);

            return Boolean.TRUE;
        }
    }

    private class PostImageToCloud extends PostTextToCloud {

        PostImageToCloud(int globalUserId, int itemId, ItemType itemType, int dateTime) {
            super(globalUserId, itemId, itemType, dateTime);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            MultipartEntity entity = new MultipartEntity();

            try {
                entity.addPart("globalUserId", new StringBody("" + mGlobalUserId));
                entity.addPart("itemId", new StringBody("" + mItemId));
                entity.addPart("itemType", new StringBody("" + mItemType.getTypeId()));
                entity.addPart("itemDateTime", new StringBody("" + mDateTime));

                File file = TimelineActivity.getInstance().getFileStreamPath(params[0] + ".png");
                entity.addPart("itemData", new FileBody(file));

                JSONObject obj = Web.requestObj(Web.POST_ITEMS, entity);
                return pushChanges(obj);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return Boolean.FALSE;
        }
    }


    private class DeleteFromCloud extends AsyncTask<Void, Void, Boolean> {

        protected int mGlobalUserId;
        protected int mItemId;
        protected ItemType mItemType;
        protected int mDateTime;

        DeleteFromCloud(int globalUserId, int itemId) {
            mGlobalUserId = globalUserId;
            mItemId = itemId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("globalUserId", "" + mGlobalUserId));
            nameValuePairs.add(new BasicNameValuePair("id", "" + mItemId));

            JSONObject obj = Web.requestObj(Web.DELETE, nameValuePairs);
            return obj != null && obj.has("success");
        }
    }

    public int sizeVisible() {
        return mDrawn;
    }

    public List<Item> getByGlobalUserId(int globalUserId) {
        return mItemGlobalUserIds.get(globalUserId);
    }
}
