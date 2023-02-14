package networkapi.listener;

import networkapi.net.ConnectedClient;

public class ConnectionEvent {
    private final ConnectedClient client;
    private final long timeConnected;

    /**
     * Constructs a new {@code PacketEvent}.
     * @param client the client that the {@code ConnectionEvent} will contain
     * @param timeConnected the time the client connected
     */
    public ConnectionEvent(ConnectedClient client, long timeConnected) {
        this.client = client;
        this.timeConnected = timeConnected;
    }

    /**
     * Returns the client contained by the {@code ConnectionEvent}
     * @return the client the {@code ConnectionEvent} contains
     */
    public ConnectedClient getClient() {
        return client;
    }

    /**
     * Returns the time the client connected
     * @return the time that the client connected
     */
    public long getTimeConnected() {
        return timeConnected;
    }
}
