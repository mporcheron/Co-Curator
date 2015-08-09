package uk.porcheron.co_curator.item;

import java.util.HashMap;
import java.util.Map;

import uk.porcheron.co_curator.R;

/**
 * Different types of timeline items.
 */
public enum ItemType {
    PHOTO(0, R.string.resource_photo_library),
    NOTE(1, R.string.resource_note),
    URL(2, R.string.resource_url);

    private int mTypeId;
    private int mLabel;

    ItemType(int typeId, int label) {
        mTypeId = typeId;
        mLabel = label;
    }

    public int getLabel() {
        return mLabel;
    }

    public int getTypeId() {
        return mTypeId;
    }

    public static ItemType get(int typeId) {
        for(ItemType it : ItemType.values()) {
            if(it.mTypeId == typeId) {
                return it;
            }
        }

        return null;
    }
}
