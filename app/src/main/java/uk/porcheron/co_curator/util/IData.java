package uk.porcheron.co_curator.util;

import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;

/**
 * Created by map on 10/08/15.
 */
public class IData {
    public static int globalUserId;
    public static int userId;
    public static int groupId;
    public static UserList users;
    public static ItemList items;

    public static User user() {
        return users.getByGlobalUserId(globalUserId);
    }
}
