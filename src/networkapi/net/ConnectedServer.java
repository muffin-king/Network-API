package networkapi.net;

public class ConnectedServer extends Networkable {
    private final String hostname;
    private final int port;
    protected ConnectedServer(Client client) {
        this.hostname = client.getConnectedHostname();
        this.port = client.getConnectedPort();
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public int getPort() {
        return port;
    }
}
