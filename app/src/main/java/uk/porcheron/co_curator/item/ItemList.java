package uk.porcheron.co_curator.item;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    private static Random mRandom = new Random();
    private final DbHelper mDbHelper;

    private final Map<String, Item> mItemIds = new HashMap<>();
    private final Map<String, Boolean> mForthcomingItemIds = new HashMap<>();
    private final SparseArray<List<Item>> mItemGlobalUserIds = new SparseArray<>();
    private final List<Item> mDrawnItems = new ArrayList<>();
    private final List<Item> mDrawnAboveX = new ArrayList<>();
    private final List<Item> mDrawnBelowX = new ArrayList<>();
    private final Map<Long,Float> mTimestampX = new HashMap<>();

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

    public List<Item> getAllVisible() {
        Collections.sort(mDrawnItems, new Comparator<Item>() {

            @Override
            public int compare(Item lhs, Item rhs) {
                return (int) (lhs.getDateTime() - rhs.getDateTime());
            }
        });

        return mDrawnItems;
    }

    public final void cancelLongPress() {
        for(Item i : this) {
            i.cancelLongPress();
        }
    }

    public int sizeVisible() {
        return mDrawnItems.size();
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

//    public synchronized boolean add(int itemId, ItemType type, User user, String data, boolean deleted, boolean addToLocalDb, boolean addToCloud) {
//        return add(itemId, type, user, data, (int) (System.currentTimeMillis() / 1000L), deleted, addToLocalDb, addToCloud);
//    }

    public synchronized boolean add(int itemId, ItemType type, User user, String data, long dateTime, boolean deleted, boolean addToLocalDb, boolean addToCloud) {
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

        int insertAt = 0;
        Iterator<Item> it = iterator();
        while (it.hasNext()) {
            Item j = it.next();
            if (j.getDateTime() > dateTime) {
                break;
            }

            insertAt++;
        }

//        SparseArray halfPosition = user.above ? mDrawnAboveX : mDrawnBelowX;
//        for(int j = 0; j < halfPosition.size(); j++) {
//
//        }
//        while (it.hasNext()) {
//            Item j = it.next();
//            if (j.getDateTime() > dateTime) {
//                break;
//            }
//
//            insertAt++;
//            if (user.above && user.draw()) {
//                insertAtAbove++;
//            } else if (user.draw()) {
//                insertAtBelow++;
//            }
//        }

        mItemIds.put(uniqueItemId, item);
        add(insertAt, item);

        for(int i = insertAt; i < size() - insertAt; i++) {
            item.setItemIndex(i);
        }

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
            drawItem(user, item);
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
            values.put(TableItem.COL_ITEM_DATETIME, item.getDateTime());
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
        long finalDateTime = item.getDateTime();
        if (addToCloud && user.globalUserId == Instance.globalUserId) {
            Log.v(TAG, "Upload item to cloud");
            if(type == ItemType.PHOTO) {
                new ItemCloud.AddImage(user.globalUserId, itemId, type, finalDateTime).execute(data);
            } else {
                new ItemCloud.AddText(user.globalUserId, itemId, type, finalDateTime).execute(data);
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
            ColloManager.broadcast(ColloDict.ACTION_DELETE, globalUserId, itemId);
        }
    }

    public long getDateTimeClosestTo(float x) {
        long dateTime = (System.currentTimeMillis() / 1000L);

        int itemX = -1;
        float diff = Integer.MAX_VALUE;
        Item item = null;

        for(Item i : mDrawnItems) {
            int[] pos = new int[2];
            i.getLocationInWindow(pos);

            pos[0] += mScrollView.getScrollX();
            float testDiff = Math.abs(pos[0] - x);
            if(testDiff < diff) {
                itemX = pos[0];
                diff = testDiff;
                item = i;
            }
        }

        if(item == null) {
            return dateTime;
        }

        float midPointX = itemX + (item.getWidth() / 2);
        if(x >= midPointX) {
            dateTime = item.getDateTime() + 1;
        } else {
            dateTime = item.getDateTime() - 1;
        }

        return dateTime;
    }

    public Item getItemClosestTo(float x) {
        float diff = Integer.MAX_VALUE;
        Item item = null;

        for(Item i : mDrawnItems) {
            int[] pos = new int[2];
            i.getLocationInWindow(pos);

            pos[0] += mScrollView.getScrollX();
            float testDiff = Math.abs(pos[0] - x);
            if(testDiff < diff) {
                diff = testDiff;
                item = i;
            }
        }

        return item;
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
            ColloManager.broadcast(ColloDict.ACTION_UPDATE, globalUserId, itemId);
        }
    }


    private void drawItem(User user, Item item) {
        Log.d(TAG, "Go forth and draw Item[" + item.getUniqueItemId() + "]");
        if(mDrawnItems.contains(item)) {
            Log.e(TAG, "Item is already drawn");
            return;
        }

        Log.d(TAG, "Go forth and draw Item[" + item.getUniqueItemId() + "]");
        int scrollTo = Phone.screenWidth;

        if (user.above) {
            scrollTo = drawInFold(mLayoutAbove, mDrawnAboveX, item, scrollTo);
        } else {
            scrollTo = drawInFold(mLayoutBelow, mDrawnBelowX, item, scrollTo);
        }

        mDrawnItems.add(item);
        mDrawn++;

        // Spacing
        int randomPadRight = (int) Style.itemXGapMin;// + mRandom.nextInt((int) Style.itemXGapOffset);
        int randomPadRightHalf = randomPadRight / 2;

        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) item.getLayoutParams();
        if(user.above) {
            p.setMargins(randomPadRightHalf, (int) item.getOuterMargin(), randomPadRightHalf, 0);
        } else {
            p.setMargins(randomPadRightHalf, 0, randomPadRightHalf, (int) item.getOuterMargin());
        }
        item.requestLayout();

        // Scroll?
        if(user.globalUserId == Instance.globalUserId) {
            //float minAutoScrollWidth = scrollTo - Style.autoscrollSlack - Phone.screenWidth;
            //if (minAutoScrollWidth <= mScrollView.getScrollX()) {
                final int targetX = scrollTo;
                mScrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScrollView.smoothScrollTo(targetX, 0);
                    }
                }, 500);
            //}
        }

        item.setDrawn(true);
    }

    private int drawInFold(LinearLayout layout, List<Item> drawnItems, Item item, int minWidth) {
        int pos = 0, x = 0;

        Item prev = null;
        for(int tPos = pos; tPos < layout.getChildCount(); tPos++) {
            Item compare = (Item) layout.getChildAt(pos);
            if(item.getDateTime() > compare.getDateTime()) {
                pos = tPos + 1;
            }

            x += compare.getMeasuredWidth();
        }

        layout.addView(item, pos);
        drawnItems.add(pos, item);
        item.setDrawnX(x);

        if(pos == layout.getChildCount() - 1) {
            return Math.max(minWidth, layout.getWidth() + item.getMeasuredWidth());
        } else {
            return (int) item.getDrawnX();
        }
    }

//    private void calculateSpacing(Item item, int pos, LinearLayout layout) {
//        if(pos == 0) {
//            return;
//        }
//
//        Item prevViewThisLayout = (Item) layout.getChildAt(pos - 1);
//        RectF slot = prevViewThisLayout.getSlotBounds();
//        float xpos = prevViewThisLayout.getDrawnX() + slot.width();
//        float leftMargin = 0;
//
////        // Do we need to add to the left margin?
////        if(Instance.addedUsers > 1 && !mTimestampX.isEmpty()) {
////            long nearestTimestamp = -1, tsDiff = 0;
////            long targetTimestamp = item.getDateTime();
////            for (Long timestamp : mTimestampX.keySet()) {
////                if (nearestTimestamp < 0) {
////                    nearestTimestamp = timestamp;
////                    tsDiff = timestamp;
////                    continue;
////                }
////
////                long diff = timestamp - targetTimestamp;
////                if (diff < 0 || diff > tsDiff) {
////                    continue;
////                }
////
////                nearestTimestamp = timestamp;
////                tsDiff = diff;
////            }
////
////            float nearestX = mTimestampX.get(nearestTimestamp);
////            if(xpos < nearestX) {
////                leftMargin = nearestX - xpos + Style.itemXGapMin;
////            }
////        }
//
//        Log.d(TAG, "Set Item[" + item.getUniqueItemId() + "] [" + item.getData() + "] as being drawn at " + xpos);
//        item.setDrawnX(xpos + leftMargin);
////        mTimestampX.put(item.getDateTime(), xpos + leftMargin);
//
////        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
////                FrameLayout.LayoutParams.WRAP_CONTENT,
////                FrameLayout.LayoutParams.WRAP_CONTENT
////        );
////        params.setMargins((int) leftMargin, 0, 0, 0);
////        item.setLayoutParams(params);
//    }

    public void retestDrawing() {
        mDrawn = 0;
        int insertAtAbove = 0;
        int insertAtBelow = 0;
        for(Item item : this) {
            User user = item.getUser();

            if(user.draw() && !item.isDeleted()) {
                if(!item.isDrawn()) {
                    Log.v(TAG, "User[" + user.globalUserId + "] is " + Instance.drawnUsers + "th user, offset=" + user.offset);
                    drawItem(user, item);
                }
            } else {
                if(item.isDrawn()) {
                    if(user.above) {
//                        mDrawnAboveX.remove(item);
                        mLayoutAbove.removeView(item);

//                        for(int j = 0; j < mDrawnBelowX.size(); j++) {
//                            Item otherItem = mDrawnBelowX.valueAt(j);
//                            if(j < insertAtAbove) {
//                                continue;
//                            }
//                            calculateSpacing(otherItem, mDrawnBelowX.keyAt(j), mLayoutBelow);
//                        }
                    } else {
//                        mDrawnBelowX.remove(item);
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
        int otherGlobalUserId = -1, itemId = -1;

        try {
            otherGlobalUserId = Integer.parseInt(data[0]);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid globalUserId received");
            return false;
        }

        try {
            itemId = Integer.parseInt(data[1]);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid itemId received");
            return false;
        }

        switch(action) {
            case ColloDict.ACTION_NEW:
            case ColloDict.ACTION_UPDATE:
                WebLoader.loadItemFromWeb(otherGlobalUserId, itemId);
                return true;

            case ColloDict.ACTION_DELETE:
                Item item = getByItemId(otherGlobalUserId, itemId);
                if(item != null) {
                    remove(item, true, false, false);
                } else {
                    Log.e(TAG, "Received delete request for Item[" + otherGlobalUserId + "-" + itemId + "], but we don't have it");
                }
                return true;
        }

        return false;
    }


}
