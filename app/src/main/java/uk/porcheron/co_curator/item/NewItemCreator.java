package uk.porcheron.co_curator.item;

/**
 * Created by map on 09/08/15.
 */
public interface NewItemCreator {

    public boolean newNote(String text);
    public boolean newPhoto();
    public boolean newURL(String url);

}
