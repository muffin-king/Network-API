package networkapi.listener;

import networkapi.net.Networkable;

public class ConnectionEvent {
    private final String hostname;
    private final int port;
    private final long timeConnected;

    public ConnectionEvent(Networkable target, long timeConnected) {
        this.hostname = target.getHostname();
        this.port = target.getPort();
        this.timeConnected = timeConnected;
    }

    public ConnectionEvent(String hostname, int port, long timeConnected) {
        this.hostname = hostname;
        this.port = port;
        this.timeConnected = timeConnected;
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
    public long getTimeConnected() {
        return timeConnected;
    }
}
