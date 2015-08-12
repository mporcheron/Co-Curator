package uk.porcheron.co_curator.item;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class ItemList extends ArrayList<Item> implements SurfaceHolder.Callback {
    private static final String TAG = "CC:ItemList";

    private TimelineActivity mActivity;
    private DbHelper mDbHelper;

    private Map<Integer,List<RectF>> mBranches = new HashMap<Integer,List<RectF>>();

    private LinearLayout mLayoutAbove;
    private RelativeLayout mLayoutCentre;
    private SurfaceView mStemSurface;
    private LinearLayout mLayoutBelow;

    private int mWidthAbove = 0;
    private int mWidthBelow = 0;

    public ItemList(TimelineActivity activity, LinearLayout layoutAbove, RelativeLayout layoutCentre, SurfaceView stemSurface, LinearLayout layoutBelow) {
        mActivity = activity;
        mDbHelper = activity.getDbHelper();
        mLayoutAbove = layoutAbove;
        mLayoutCentre = layoutCentre;
        mStemSurface = stemSurface;
        mLayoutBelow = layoutBelow;

        mStemSurface.getHolder().addCallback(this);
    }

    public boolean add(int itemId, ItemType type, User user, String data) {
        return add(itemId, type, user, data, false);
    }

    public boolean add(int itemId, ItemType type, User user, String data, boolean drawOnly) {
        Log.d(TAG, "Add item " + itemId + " to the view (drawOnly=" + drawOnly + ")");

        // Create the item
        Item item = null;
        if(type == ItemType.NOTE) {
            item = createNote(itemId, user, data);
        } else if(type == ItemType.URL) {
            item = createURL(itemId, user, data);
        } else if(type == ItemType.PHOTO && data instanceof String) {
            item = createImage(itemId, user, data);
        }

        if(item == null) {
            Log.e(TAG, "Unsupported item type: " + type.getLabel());
            return false;
        }

        add(item);

        // Drawing
        int slotX = 0;
        if (user.above) {
            mLayoutAbove.addView(item, item.getWidth(), (int) Style.itemFullHeight);
            slotX = mWidthAbove;
            mWidthAbove += item.getSlotBounds().width();
        } else {
            mLayoutBelow.addView(item, item.getWidth(), (int) Style.itemFullHeight);
            slotX = mWidthBelow;
            mWidthBelow += item.getSlotBounds().width();
        }
        //mLayoutCentre.addView(new StemConnector(mActivity, user, item.getmStemConnectorBounds()));

        // Branch drawing
        List<RectF> branches;
        if(mBranches.containsKey(user.userId)) {
            branches = mBranches.get(user.userId);
        } else {
            branches = new ArrayList<RectF>();
        }

        Log.d(TAG, "Add " + slotX + " to item " + item.getItemId() + "'s branch");
        item.getBranchBounds().offset(slotX, 0);
        branches.add(item.getBranchBounds());
        mBranches.put(user.userId, branches);

        mLayoutCentre.setMinimumWidth(Math.max(mWidthAbove, mWidthBelow));
        mStemSurface.invalidate();

        // Save to the Local Database or just draw?
        if(drawOnly) {
            return true;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TableItem.COL_GLOBAL_USER_ID, user.globalUserId);
        values.put(TableItem.COL_ITEM_TYPE, type.getTypeId());

        // FIXME: SAVING FILES TO LCOAL STORAGE
        if(type == ItemType.PHOTO) {
            FileOutputStream out;
            //Bitmap bitmap = (Bitmap) data;
            data = UData.globalUserId + "-" + UData.userId + "-" + System.currentTimeMillis() + ".jpg";

            try {
                Log.v(TAG, "Save image to local storage");
                out = mActivity.openFileOutput((String) data, Context.MODE_PRIVATE);
                //bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Style.backgroundColor);

        int w = canvas.getWidth();
        int h = canvas.getHeight();

        for(int i = Style.userLayers.length - 1; i >= 0; i--) {
            Log.d(TAG, "Draw trunk and branch for " + i);
            User user = UData.users.get(i);

            int y1 = (int) (((h - Style.layoutCentreHeight) / 2) + user.centrelineOffset);
            int y2 = (int) (y1 + Style.lineWidth);

            canvas.drawRect(0, y1, w, y2, user.bgPaint);

            List<RectF> branches = mBranches.get(i);
            if(branches == null) {
                continue;
            }
            Log.d(TAG, " there are " + branches.size() + " for user " + i);

            for(RectF branch : branches) {
                Log.d(TAG, "draw branch at " + branch.toString());

                canvas.drawRect(branch, user.bgPaint);
            }
        }

        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
