package networkapi.listener;

import networkapi.net.Networkable;

public class DisconnectionEvent {
    private final String hostname;
    private final int port;
    private final long timeDisconnected;
    private final int reason;
    public static final int SERVER_DISCONNECTION = 0;
    public static final int CLIENT_DISCONNECTION = 1;
    public static final int PACKET_SEND_EXCEPTION = 2;
    public static final int PACKET_READ_EXCEPTION = 3;

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
