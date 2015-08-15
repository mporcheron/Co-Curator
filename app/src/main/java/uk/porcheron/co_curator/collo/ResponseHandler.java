package uk.porcheron.co_curator.collo;

/**
 * Object that handles responses to messages received.
 */
public interface ResponseHandler {

    public boolean respond(String action, int globalUserId, String... data);

}
