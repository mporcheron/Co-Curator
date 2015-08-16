package uk.porcheron.co_curator.db;

import android.provider.BaseColumns;

/**
 * Database structure for the `item` table.
 */
public abstract class TableItem implements BaseColumns {
    public static final String TABLE_NAME = "item";

    public static final String COL_ID = "_id";
    public static final String COL_ITEM_ID = "itemId";
    public static final String COL_GLOBAL_USER_ID = "globalUserId";
    public static final String COL_ITEM_TYPE = "itemType";
    public static final String COL_ITEM_DATA = "itemData";
    public static final String COL_ITEM_DATETIME = "itemDateTime";
    public static final String COL_ITEM_UPLOADED = "itemUploaded";
    public static final String COL_ITEM_DELETED = "itemDeleted";

    public static final int VAL_ITEM_WILL_UPLOAD = 0;
    public static final int VAL_ITEM_WONT_UPLOAD = -1;
    public static final int VAL_ITEM_UPLOADED = 1;

    public static final int VAL_ITEM_NOT_DELETED = 0;
    public static final int VAL_ITEM_DELETED = 1;

    protected static final String SQL_CREATE =
            "CREATE TABLE " + TableItem.TABLE_NAME + " (" +
                    TableItem.COL_ID + Sql.TYPE_INT + Sql.PRIMARY_KEY + Sql.COMMA +
                    TableItem.COL_ITEM_ID + Sql.TYPE_INT + Sql.COMMA +
                    TableItem.COL_GLOBAL_USER_ID + Sql.TYPE_INT + Sql.COMMA +
                    //"FOREIGN KEY (" + TableItem.COL_GLOBAL_USER_ID + ") " +
                    //"REFERENCES " + TableUser.TABLE_NAME +
                    //" (" + TableUser.COL_GLOBAL_USER_ID + ")" + Sql.COMMA +
                    TableItem.COL_ITEM_TYPE + Sql.TYPE_INT + Sql.COMMA +
                    TableItem.COL_ITEM_DATA + Sql.TYPE_TEXT + Sql.COMMA +
                    TableItem.COL_ITEM_DATETIME + Sql.TYPE_INT + Sql.COMMA +
                    TableItem.COL_ITEM_UPLOADED + Sql.TYPE_INT + Sql.COMMA +
                    TableItem.COL_ITEM_DELETED + Sql.TYPE_INT +
                    " );";

    protected static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TableItem.TABLE_NAME + ";";

    private TableItem() {
    }
}
