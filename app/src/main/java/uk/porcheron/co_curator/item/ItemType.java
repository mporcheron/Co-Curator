package uk.porcheron.co_curator.item;

import uk.porcheron.co_curator.R;

/**
 * Different types of timeline items.
 */
public enum ItemType {
    PHOTO(R.string.resource_photo_library),
    NOTE(R.string.resource_note),
    URL(R.string.resource_url);

    /**
     * String label for the item type.
     */
    public int mLabel;

    ItemType(int label) {
        mLabel = label;
    }
}
