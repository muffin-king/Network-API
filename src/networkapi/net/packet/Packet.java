package networkapi.net.packet;

import networkapi.net.Client;
import networkapi.net.Networkable;
import networkapi.net.Server;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * A {@code Packet} is a way to store data for sending between a {@link Client} and a {@link Server}.
 */
public class Packet implements Serializable, Cloneable {
    private final int ID;
    private final Serializable data;
    private final String hostname;
    private final int port;

    /**
     * Constructs a new {@code Packet}.
     * @param ID The integer ID of the packet (0 is reserved for disconnection packets)
     * @param data The object contained by the packet
     * @param origin The {@code Networkable} origin of the packet
     */
    public Packet(int ID, Serializable data, Networkable origin) {
        this.ID = ID;
        this.data = data;
        this.hostname = origin.getHostname();
        this.port = origin.getPort();
    }

    /**
     * Returns the packet's origin address.
     * @return The address of the packet's origin
     */
    public InetSocketAddress getAddress() {
        return new InetSocketAddress(hostname, port);
    }

    /**
     * Returns the object contained by the packet
     * @return the object the packet contains
     */
    public Object getData() {
        return data;
    }

    /**
     * Returns the assigned integer ID of the packet.
     * @return the integer assigned to identify the packet
     */
    public int getID() {
        return ID;
    }

    /**
     * Returns whether the packet came from the specified client.
     * @param source The {@code Networkable} object to check
     * @return true if the packet came from the specified source, false otherwise
     */
    public boolean sameSource(Networkable source) {
        return source.getHostname().equals(hostname) && source.getPort() == port;
    }

    public boolean equals(Packet packet) {
        return ID == packet.getID() && data.equals(packet.getData()) && getAddress().equals(packet.getAddress());
    }
    
    public Packet clone() {
        Packet clone;
        try {
            clone = (Packet) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return clone;
    }
}
