package uk.porcheron.co_curator.db;

import android.provider.BaseColumns;

/**
 * Created by map on 09/08/15.
 */
public abstract class TableItem implements BaseColumns {
    public static final String TABLE_NAME = "item";
    public static final String COL_ITEM_ID = "itemId";
    public static final String COL_GLOBAL_USER_ID = "globalUserId";
    public static final String COL_ITEM_TYPE = "itemType";
    public static final String COL_ITEM_DATA = "itemData";
    public static final String COL_ITEM_DATETIME = "itemDateTime";

    protected static final String SQL_CREATE =
            "CREATE TABLE " + TableItem.TABLE_NAME + " (" +
                    TableItem.COL_ITEM_ID + Sql.TYPE_INT + Sql.PRIMARY_KEY + Sql.COMMA +
                    TableItem.COL_GLOBAL_USER_ID + Sql.TYPE_INT + Sql.COMMA +
                    //"FOREIGN KEY (" + TableItem.COL_GLOBAL_USER_ID + ") " +
                    //"REFERENCES " + TableUser.TABLE_NAME +
                    //" (" + TableUser.COL_GLOBAL_USER_ID + ")" + Sql.COMMA +
                    TableItem.COL_ITEM_TYPE + Sql.TYPE_INT + Sql.COMMA +
                    TableItem.COL_ITEM_DATA + Sql.TYPE_TEXT + Sql.COMMA +
                    TableItem.COL_ITEM_DATETIME + Sql.TYPE_DT + " default CURRENT_TIMESTAMP" +
                    " );";

    protected static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TableItem.TABLE_NAME + ";";

    private TableItem() {}
}
