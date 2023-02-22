package networkapi.listener;

import networkapi.net.ConnectedClient;

public class DisconnectionEvent {
    private final ConnectedClient client;
    private final long timeDisconnected;
    private final int reason;
    public static final int SERVER_DISCONNECTION = 0;
    public static final int PACKET_SEND_EXCEPTION = 1;
    public static final int PACKET_READ_EXCEPTION = 2;

    /**
     * Constructs a new {@code DisconnectionEvent}.
     * @param client the client that the {@code DisconnectionEvent} will contain
     * @param timeDisconnected the time the client disconnected
     */
    public DisconnectionEvent(ConnectedClient client, long timeDisconnected, int reason) {
        this.client = client;
        this.timeDisconnected = timeDisconnected;
        this.reason = reason;
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
    public long getTimeDisconnected() {
        return timeDisconnected;
    }

    /**
     * Returns the reason the client was disconnected
     * @return the reason for client disconnection
     */
    public int getReason() {
        return reason;
    }
}
