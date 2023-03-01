package networkapi.listener;

import networkapi.net.Networkable;

/**
 * {@code DisconnectionEvent} represents when a {@code Networkable} has disconnected from a {@code Client} or {@code Server}.
 */
public class DisconnectionEvent {
    private final String hostname;
    private final int port;
    private final long timeDisconnected;
    private final int reason;
    public static final int SERVER_DISCONNECTION = 0;
    public static final int CLIENT_DISCONNECTION = 1;

    public DisconnectionEvent(Networkable target, long timeDisconnected, int reason) {
        this.hostname = target.getHostname();
        this.port = target.getPort();
        this.timeDisconnected = timeDisconnected;
        this.reason = reason;
    }

    /**
     * Returns the hostname of the {@code Networkable} connected.
     * @return the {@code Networkable}'s hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Returns the port of the {@code Networkable} connected.
     * @return the {@code Networkable}'s port
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the time the {@code Networkable} connected
     * @return the time that the {@code Networkable} connected
     */
    public long getTimeDisconnected() {
        return timeDisconnected;
    }

    /**
     * Returns the reason the {@code Networkable} was disconnected
     * @return the reason for {@code Networkable} disconnection
     */
    public int getReason() {
        return reason;
    }
}
