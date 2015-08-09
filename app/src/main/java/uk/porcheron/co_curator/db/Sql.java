package uk.porcheron.co_curator.db;

/**
 * Database contract.
 */
public class Sql {
    protected static final String PRIMARY_KEY = " PRIMARY KEY";

    protected static final String TYPE_TEXT = " TEXT";
    protected static final String TYPE_INT = " INTEGER";
    protected static final String TYPE_DT = " DATETIME";

    protected static final String COMMA = ", ";

    protected static final String SQL_CREATE =
            TableUser.SQL_CREATE + TableItem.SQL_CREATE;

    protected static final String SQL_DELETE =
            TableUser.SQL_DELETE + TableItem.SQL_DELETE;

    private Sql() {}
}
