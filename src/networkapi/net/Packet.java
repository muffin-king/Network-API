package networkapi.net;

import java.io.Serial;
import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * A {@code Packet} is a way to store data for sending between a {@link Client} and a {@link Server}.
 */
public class Packet implements Serializable, Cloneable {
    @Serial
    private static final long serialVersionUID = -1203921031231233412L;
    private final int ID;
    private final Object data;
    private final long time;
    private final InetSocketAddress address;

    /**
     * Constructs a new {@code Packet}.
     * @param  ID The integer ID of the packet
     * @param data The object contained by the packet
     * @param address The address from where the packet was sent from
     */
    public Packet(int ID, Object data, InetSocketAddress address) {
        this.ID = ID;
        this.data = data;
        time = System.currentTimeMillis();
        this.address = address;
    }

    /**
     * Returns the packet's origin address.
     * @return The address of the packet's origin
     */
    public InetSocketAddress getAddress() {
        return address;
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
     * Returns the time that the packet was created.
     * @return the time in milliseconds that the packet was created at
     */
    public long getTime() {
        return time;
    }

    public boolean equals(Object object) {
        return (object instanceof Packet packet) && (getID() == packet.getID()) && (getTime() == packet.getTime()) && getData().equals(packet.getData());
    }
    
    public Packet clone() {
        Packet clone = null;
        try {
            clone = (Packet) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return clone;
    }
}
