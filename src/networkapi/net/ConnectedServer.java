package networkapi.net;

public class ConnectedServer extends Networkable {
    private final String hostname;
    private final int port;

    protected ConnectedServer(Client client) {
        this.hostname = client.getConnectedHostname();
        this.port = client.getConnectedPort();
    }

    /**
     * Returns the hostname of the server.
     * @return The server's hostname
     */
    @Override
    public String getHostname() {
        return hostname;
    }

    /**
     * Returns the port of the server.
     * @return The server's port]
     */
    @Override
    public int getPort() {
        return port;
    }
}
