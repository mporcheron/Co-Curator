package uk.porcheron.co_curator.util;

import android.provider.BaseColumns;

/**
 * Database contract.
 */
public class TimelineContract {

    public TimelineContract() {

    }

    public static abstract class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "user";
        public static final String COLUMN_NAME_GLOBAL_USER_ID = "globalUserId";
        public static final String COLUMN_NAME_USER_ID = "userId";
    }

    public static abstract class ItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "item";
        public static final String COLUMN_NAME_ITEM_ID = "itemId";
        public static final String COLUMN_NAME_GLOBAL_USER_ID = "globalUserId";
        public static final String COLUMN_NAME_ITEM_TYPE = "itemType";
        public static final String COLUMN_NAME_ITEM_DATA = "itemData";
        public static final String COLUMN_NAME_ITEM_DATETIME = "itemDateTime";
    }

    private static final String PRIMARY_KEY = " PRIMARY KEY";

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INT = " TEXT";
    private static final String TYPE_DT = " DATETIME";

    private static final String COMMA = ",";

    protected static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                    UserEntry.COLUMN_NAME_GLOBAL_USER_ID + TYPE_INT + PRIMARY_KEY + COMMA +
                    UserEntry.COLUMN_NAME_USER_ID + TYPE_INT +
                    " );" +
            "CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +
                    ItemEntry.COLUMN_NAME_ITEM_ID + TYPE_INT + PRIMARY_KEY + COMMA +
                    "FOREIGN KEY(" + ItemEntry.COLUMN_NAME_GLOBAL_USER_ID + ") " +
                      "REFERENCES " + UserEntry.TABLE_NAME +
                      "(" + UserEntry.COLUMN_NAME_USER_ID + ")" + COMMA +
                    ItemEntry.COLUMN_NAME_ITEM_TYPE + TYPE_INT + COMMA +
                    ItemEntry.COLUMN_NAME_ITEM_DATA + TYPE_TEXT + COMMA +
                    ItemEntry.COLUMN_NAME_ITEM_DATETIME + TYPE_DT + "default CURRENT_TIMESTAMP" +
            " );";

    protected static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME + ";" +
            "DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME;

}
