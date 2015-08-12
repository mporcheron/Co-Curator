package uk.porcheron.co_curator.db;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.IData;
import uk.porcheron.co_curator.util.Image;
import uk.porcheron.co_curator.util.Web;

/**
 * Created by map on 12/08/15.
 */
public class WebLoader {
    private static final String TAG = "CC:WebLoader";

    private TimelineActivity mActivity;

    WebLoader(TimelineActivity activity) {
        mActivity = activity;
    }

    protected void loadUsersFromWeb() {

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("groupId", "" + IData.groupId));

        JSONArray response = Web.requestArr(Web.GET_USERS, nameValuePairs);
        if(response != null) {
            for(int i = 0; i < response.length(); i++) {
                try {
                    JSONObject userJ = (JSONObject) response.get(i);
                    IData.users.add(userJ.getInt("globalUserId"), userJ.getInt("userId"), false);
                } catch (JSONException e) {
                    Log.e(TAG, "Could not get user from the cloud");
                    e.printStackTrace();
                }

            }
        }

    }

    protected void loadItemsFromWeb(int globalUserId) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("globalUserId", "" + globalUserId));

        JSONArray response = Web.requestArr(Web.GET_ITEMS, nameValuePairs);
        if(response != null) {
            for(int i = 0; i < response.length(); i++) {
                try {
                    JSONObject itemJ = (JSONObject) response.get(i);

                    final int itemId = itemJ.getInt("id");

                    if(IData.items.getByItemId(globalUserId, itemId) == null) {
                        final User user = IData.users.getByGlobalUserId(globalUserId);
                        String jsonData = itemJ.getString("data");
                        final ItemType type = ItemType.get(itemJ.getInt("type"));
                        final String datetime = itemJ.getString("datetime");

                        if(type == ItemType.PHOTO) {
                            Bitmap b = getBitmapFromURL(jsonData);
                            try {
                                jsonData = Image.save(mActivity, b);
                            } catch (IOException e) {
                                Log.e(TAG, "Failed to download image " + jsonData);
                                e.printStackTrace();
                            }
                        }

                        final String data = jsonData;

                        mActivity.runOnUiThread(new Runnable(){
                            public void run() {
                                IData.items.add(itemId, type, user, data, false);
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Could not get items from the cloud");
                    e.printStackTrace();
                }

            }
        }

    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            Log.e(TAG, "Failed to get image from URL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
