package uk.porcheron.co_curator.item;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.collo.ColloDict;
import uk.porcheron.co_curator.collo.ColloManager;
import uk.porcheron.co_curator.db.DbHelper;
import uk.porcheron.co_curator.db.TableItem;
import uk.porcheron.co_curator.util.Web;

/**
 * Cloud utilities for items.
 */
class ItemCloud {
    private static final String TAG = "CC:ItemCloud";

    static class AddText extends AsyncTask<String, Void, Boolean> {

        protected int mGlobalUserId;
        protected int mItemId;
        protected ItemType mItemType;
        protected int mDateTime;

        AddText(int globalUserId, int itemId, ItemType itemType, int dateTime) {
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
            SQLiteDatabase db = DbHelper.getInstance().getWritableDatabase();

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
            ColloManager.broadcast(ColloDict.ACTION_NEW, mItemId);

            return Boolean.TRUE;
        }
    }

    static class AddImage extends AddText {

        AddImage(int globalUserId, int itemId, ItemType itemType, int dateTime) {
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

    static class Remove extends AsyncTask<Void, Void, Boolean> {

        protected int mGlobalUserId;
        protected int mItemId;

        Remove(int globalUserId, int itemId) {
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

    static class Update extends AsyncTask<String, Void, Boolean> {

        protected int mGlobalUserId;
        protected int mItemId;

        Update(int globalUserId, int itemId) {
            mGlobalUserId = globalUserId;
            mItemId = itemId;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("globalUserId", "" + mGlobalUserId));
            nameValuePairs.add(new BasicNameValuePair("id", "" + mItemId));
            nameValuePairs.add(new BasicNameValuePair("data", "" + params[0]));

            JSONObject obj = Web.requestObj(Web.UPDATE, nameValuePairs);
            return obj != null && obj.has("success");
        }
    }
}
