package networkapi.net;

/**
 * {@code Networkable} is an abstract class that is used for
 * {@link Client}, {@link Server}, {@link ConnectedClient}, and {@link ConnectedServer}
 * organization in events and packets.
 */
public abstract class Networkable {
    public abstract String getHostname();
    public abstract int getPort();
}
