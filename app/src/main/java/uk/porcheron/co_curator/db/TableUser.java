package uk.porcheron.co_curator.db;

import android.provider.BaseColumns;

/**
 * Database structure for the `user` table.
 */
public abstract class TableUser implements BaseColumns {
    public static final String TABLE_NAME = "user";
    public static final String COL_GLOBAL_USER_ID = "globalUserId";
    public static final String COL_USER_ID = "userId";

    protected static final String SQL_CREATE =
            "CREATE TABLE " + TableUser.TABLE_NAME + " (" +
                    TableUser.COL_GLOBAL_USER_ID + Sql.TYPE_INT + Sql.COMMA +
                    TableUser.COL_USER_ID + Sql.TYPE_INT +
                    " );";

    protected static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TableUser.TABLE_NAME + ";";

    private TableUser() {
    }
}
