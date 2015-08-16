package uk.porcheron.co_curator.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import uk.porcheron.co_curator.TimelineActivity;

/**
 * Database helper class for handling the connection to the Android SQLite implementation.
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "CC:DbHelper";

    public static final String DATABASE_NAME = "CoCurator.db";
    public static final int DATABASE_VERSION = 3;

    private static TimelineActivity mActivity = TimelineActivity.getInstance();
    private static DbHelper mInstance = null;

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DbHelper getInstance() {
        if (mInstance == null) {
            mInstance = new DbHelper(mActivity);
        }
        return mInstance;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TableUser.SQL_CREATE);
        db.execSQL(TableItem.SQL_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrade DB from " + oldVersion + " to " + newVersion);

        db.execSQL(TableItem.SQL_DELETE);
        db.execSQL(TableUser.SQL_DELETE);

        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Downgrade DB from " + oldVersion + " to " + newVersion);
        onUpgrade(db, oldVersion, newVersion);
    }
}