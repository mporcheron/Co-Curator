package uk.porcheron.co_curator.item;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.collo.ColloDict;
import uk.porcheron.co_curator.collo.ColloManager;
import uk.porcheron.co_curator.db.DbHelper;
import uk.porcheron.co_curator.db.TableItem;
import uk.porcheron.co_curator.db.WebLoader;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.val.Phone;
import uk.porcheron.co_curator.val.Style;

/**
 * List of items
 * <p/>
 * Created by map on 07/08/15.
 */
public class ItemList extends ArrayList<Item> implements ColloManager.ResponseHandler {
    private static final String TAG = "CC:ItemList";

    private final DbHelper mDbHelper;

    private final Map<String, Item> mItemIds = new HashMap<>();
    private final Map<String, Boolean> mForthcomingItemIds = new HashMap<>();
    private final SparseArray<List<Item>> mItemGlobalUserIds = new SparseArray<>();
    private final List<Item> mDrawnItems = new ArrayList<>();

    private final ItemScrollView mScrollView;
    private final LinearLayout mLayoutAbove;
    private final LinearLayout mLayoutBelow;

    private int mDrawn = 0;

    public static final int ITEM_DELETED = 1;
    public static final int ITEM_NOT_DELETED = 0;

    public ItemList(ItemScrollView scrollView, LinearLayout layoutAbove, LinearLayout layoutBelow) {
        mDbHelper = DbHelper.getInstance();
        mScrollView = scrollView;
        mLayoutAbove = layoutAbove;
        mLayoutBelow = layoutBelow;

        mLayoutAbove.removeAllViews();
        mLayoutBelow.removeAllViews();

        mLayoutAbove.setMinimumWidth(Phone.screenWidth);
        mLayoutBelow.setMinimumWidth(Phone.screenWidth);

        ColloManager.ResponseManager.registerHandler(ColloDict.ACTION_NEW, this);
        ColloManager.ResponseManager.registerHandler(ColloDict.ACTION_UPDATE, this);
        ColloManager.ResponseManager.registerHandler(ColloDict.ACTION_DELETE, this);
    }

    public int sizeVisible() {
        return mDrawn;
    }

    public List<Item> getByGlobalUserId(int globalUserId) {
        return mItemGlobalUserIds.get(globalUserId);
    }

    public boolean containsByItemId(int globalUserId, int itemId, boolean allowForthcoming) {
        String uniqueId = globalUserId + "-" + itemId;

        if(allowForthcoming) {
            return mItemIds.containsKey(uniqueId) || mForthcomingItemIds.containsKey(uniqueId);
        }

        return mItemIds.containsKey(uniqueId);
    }

    public Item getByItemId(int globalUserId, int itemId) {
        return mItemIds.get(globalUserId + "-" + itemId);
    }

    public synchronized boolean add(int itemId, ItemType type, User user, String data, boolean deleted, boolean addToLocalDb, boolean addToCloud) {
        return add(itemId, type, user, data,  (int) (System.currentTimeMillis() / 1000L), deleted, addToLocalDb, addToCloud);
    }

//    public boolean add(int itemId, ItemType type, User user, String data, boolean deleted, boolean addToLocalDb, boolean addToCloud) {
//        return add(itemId, type, user, data, (int) (System.currentTimeMillis() / 1000L), deleted, addToLocalDb, addToCloud);
//    }

    public synchronized boolean add(int itemId, ItemType type, User user, String data, int dateTime, boolean deleted, boolean addToLocalDb, boolean addToCloud) {
        String uniqueItemId = user.globalUserId + "-" + itemId;

        Log.v(TAG, "Item[" + uniqueItemId + "]: Add to List (type=" + type + ",user=" + user.globalUserId +
                ",data=" + data + ",dateTime=" + dateTime + ",deleted=" + deleted + ",addToLocalDb=" + addToLocalDb +
                ",addToCloud=" + addToCloud + ")");

        // Create the item
        Item item = null;
        if (type == ItemType.NOTE) {
            item = new ItemNote(itemId, user, dateTime);
        } else if (type == ItemType.URL) {
            item = new ItemUrl(itemId, user, dateTime);
        } else if (type == ItemType.PHOTO) {
            item = new ItemPhoto(itemId, user, dateTime);
        }

        if (item == null) {
            Log.e(TAG, "Item[" + uniqueItemId + "]: Unsupported type: " + type.getLabel());
            return false;
        }

        item.setData(data);
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

        Log.e(TAG, "We've recorded that we've got Item[" + uniqueItemId + "]");
        mItemIds.put(uniqueItemId, item);
        add(insertAt, item);

        // List of user's items
        List<Item> items = mItemGlobalUserIds.get(user.globalUserId);
        if(items == null) {
            items = new ArrayList<>();
            mItemGlobalUserIds.put(user.globalUserId, items);
        }

        items.add(item);

        // Drawing
        final int layoutPosition = user.above ? insertAtAbove : insertAtBelow;
        if(!user.draw() || deleted) {
            Log.v(TAG, "Item[" + uniqueItemId + "]: Won't draw as user is not connected or us or is deleted");
        } else {
            drawItem(user, item, layoutPosition);
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
                new ItemCloud.AddImage(user.globalUserId, itemId, type, dateTime).execute(data);
            } else {
                new ItemCloud.AddText(user.globalUserId, itemId, type, dateTime).execute(data);
            }
        }

        return true;
    }

    public synchronized void registerForthcomingItem(int globalUserId, int itemId) {
        mForthcomingItemIds.put(globalUserId + "-" + itemId, true);
    }

    public synchronized void unremove(Item item) {
        int globalUserId = item.getUser().globalUserId;
        int itemId = item.getItemId();

        item.setDeleted(false);

        // Unremove from view
        TimelineActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Instance.items.retestDrawing();
            }
        });

        // Unremove from local DB
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TableItem.COL_ITEM_DELETED, TableItem.VAL_ITEM_NOT_DELETED);

        String whereClause = TableItem.COL_GLOBAL_USER_ID + "=? AND " +
                TableItem.COL_ITEM_ID + "=?";
        String[] whereArgs = {"" + globalUserId, "" + itemId};

        int rowsAffected = db.update(TableItem.TABLE_NAME, values, whereClause, whereArgs);
        if (rowsAffected != 1) {
            Log.e(TAG, "Failed to unset deleted for Item[" + globalUserId + ":" + itemId + "]");
        }

        db.close();
    }

    public synchronized void remove(Item item, boolean removeFromLocalDb, boolean removeFromCloud, boolean notifyClients) {
        int globalUserId = item.getUser().globalUserId;
        int itemId = item.getItemId();

        // Remove from list
        item.setDeleted(true);
        mDrawnItems.remove(item);

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
            new ItemCloud.Remove(globalUserId, itemId).execute();
        }

        // Notify neighbours
        if(notifyClients) {
            ColloManager.broadcast(ColloDict.ACTION_DELETE, itemId);
        }
    }

    public synchronized void update(Item item, String data, boolean updateCloud, boolean notifyClients) {
        int globalUserId = item.getUser().globalUserId;
        int itemId = item.getItemId();
        item.setData(data);

        Log.d(TAG, "Updating Item[" + globalUserId + "-" + itemId + "] to " + data);

        if(mDrawnItems.contains(item)) {
            item.invalidate();
        }

        // Update local DB
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TableItem.COL_ITEM_DATA, data);

        String whereClause = TableItem.COL_GLOBAL_USER_ID + "=? AND " +
                TableItem.COL_ITEM_ID + "=?";
        String[] whereArgs = {"" + globalUserId, "" + itemId };

        int rowsAffected = db.update(TableItem.TABLE_NAME, values, whereClause, whereArgs);
        if (rowsAffected == 0) {
            Log.e(TAG, "Failed to update data for Item[" + globalUserId + ":" + itemId + "]");
        }

        db.close();

        // Update cloud
        if(updateCloud) {
            new ItemCloud.Update(globalUserId, itemId).execute(data);
        }

        // Notify clients
        if(notifyClients) {
            ColloManager.broadcast(ColloDict.ACTION_UPDATE, itemId);
        }
    }

    private void drawItem(User user, Item item, int pos) {
        if(mDrawnItems.contains(item)) {
            Log.e(TAG, "Item is already drawn");
            return;
        }

        int minWidth = Phone.screenWidth;
        if (user.above) {
            mLayoutAbove.addView(item, Math.min(mLayoutAbove.getChildCount(), pos));
            minWidth = Math.max(mLayoutAbove.getWidth() + item.getMeasuredWidth(), minWidth);
        } else {
            mLayoutBelow.addView(item, Math.min(mLayoutBelow.getChildCount(), pos));
            minWidth = Math.max(mLayoutBelow.getWidth() + item.getMeasuredWidth(), minWidth);
        }

        mDrawnItems.add(item);
        mDrawn++;

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
                }
            } else {
                if(item.isDrawn()) {
                    if(user.above) {
                        mLayoutAbove.removeView(item);
                    } else {
                        mLayoutBelow.removeView(item);
                    }
                    mDrawnItems.remove(item);
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
            case ColloDict.ACTION_UPDATE:
                WebLoader.loadItemFromWeb(globalUserId, itemId);
                return true;

            case ColloDict.ACTION_DELETE:
                Item item = getByItemId(globalUserId, itemId);
                if(item != null) {
                    remove(item, true, false, false);
                } else {
                    Log.e(TAG, "Received delete request for Item[" + globalUserId + "-" + itemId + "], but we don't have it");
                }
                return true;
        }

        return false;
    }


}
