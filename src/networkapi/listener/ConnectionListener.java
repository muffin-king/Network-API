package networkapi.listener;

/**
 * An interface with which to create client connection event listeners that fire {@code onClientConnection} when a client connects.
 */
public interface ConnectionListener {
    /**
     * Method that is fired upon a client connection.
     * @param e the {@link ConnectionEvent} that is constructed upon connection
     */
    void onClientConnection(ConnectionEvent e);

    /**
     * Method that is fired upon a client disconnection.
     * @param e the {@link DisconnectionEvent} that is constructed upon disconnection
     */
    void onClientDisconnection(DisconnectionEvent e);
}
