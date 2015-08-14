package uk.porcheron.co_curator.val;

import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;

/**
 * Data relating to this instance of the running application.
 */
public class Instance {
    public static int globalUserId;
    public static int userId;
    public static int groupId;
    public static UserList users;
    public static ItemList items;
    public static final int LOCAL_PORT = 54545;

    public static User user() {
        return users.getByGlobalUserId(globalUserId);
    }
}
