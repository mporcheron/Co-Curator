package uk.porcheron.co_curator.util;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by map on 12/08/15.
 */
public class Web {
    private static final String TAG = "CC:Web";

    public static final String ROOT = "https://www.porcheron.uk/cocurator/";

    public static final String LOGIN = ROOT + "login.php";
    public static final String GET_USERS = ROOT + "getUsers.php";
    public static final String GET_ITEMS = ROOT + "getItems.php";
    public static final String POST_ITEMS = ROOT + "post.php";

    public static JSONObject requestObj(String uri,  List<NameValuePair> data) {
        String response = request(uri, data);

        if(response == null) {
            return null;
        }

        try {
            return new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONArray requestArr(String uri,  List<NameValuePair> data) {
        String response = request(uri, data);

        if(response == null) {
            return null;
        }

        try {
            return new JSONArray(response);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String request(String uri,  List<NameValuePair> data) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri);

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(data));

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity messageEntity = httpResponse.getEntity();
            InputStream is = messageEntity.getContent();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuffer builder = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }

            Log.v(TAG, builder.toString());
            return builder.toString();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
