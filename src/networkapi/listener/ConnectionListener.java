package networkapi.listener;

/**
 * An interface with which to create client connection event listeners that fire {@code onClientConnection} when a client connects.
 */
public interface ConnectionListener {
    /**
     * Method that is fired upon receiving any packet.
     * @param e the {@link PacketEvent} that is constructed upon the packet's arrival
     */
    void onClientConnection(ConnectionEvent e);
}
