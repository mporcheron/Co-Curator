package uk.porcheron.ace;

/**
 * Actions that should be performable by the implementation.
 */
public enum AceAction {
    DISCOVER(".discover");

    /** Base Action identifier. */
    private static final String ACTION = "uk.porcheron.ace.ble.action";

    /** Identifier of the Action type */
    public final String ident;

    /**
     * @param ident Identifier of the Action type
     */
     AceAction(String ident) {
        this.ident = ACTION + ident;
    }

    /**
     * Compare this AceAction with a given identifier.
     *
     * @param ident Identifer to test if equal to this AceAction's identifier.
     * @return {@code true} if the identifiers are equal.
     */
    public boolean equals(String ident) {
        return this.ident.equals(ident);
    }

    /**
     * Compare this AceAction with another AceAction.
     *
     * @param action AceAction to test if equal to this AceAction's identifier.
     * @return {@code true} if the identifiers are equal.
     */
    public boolean equals(AceAction action) {
        return this.ident.equals(action.ident);
    }
}
