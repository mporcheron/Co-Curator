package uk.porcheron.co_curator.item;

import uk.porcheron.co_curator.R;

/**
 * Different types of timeline items.
 */
public enum ItemType {
    UNKNOWN(-1, R.string.resource_unknown),
    PHOTO(0, R.string.resource_photo_library),
    NOTE(1, R.string.resource_note),
    URL(2, R.string.resource_url);

    private int mTypeId;
    private int mLabel;

    ItemType(int typeId, int label) {
        mTypeId = typeId;
        mLabel = label;
    }

    public static ItemType get(int typeId) {
        for (ItemType it : ItemType.values()) {
            if (it.mTypeId == typeId) {
                return it;
            }
        }

        return ItemType.UNKNOWN;
    }

    public int getLabel() {
        return mLabel;
    }

    public int getTypeId() {
        return mTypeId;
    }
}
