package uk.porcheron.co_curator.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by map on 09/08/15.
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "CC:Db";

    public static final String DATABASE_NAME = "CoCurator.db";
    public static final int DATABASE_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TableUser.SQL_CREATE);
        db.execSQL(TableUser.SQL_DUMMY_DATA);
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