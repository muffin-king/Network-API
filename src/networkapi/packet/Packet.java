package networkapi.packet;

import java.io.Serializable;
import java.net.InetSocketAddress;

public class Packet implements Serializable, Cloneable {
    protected final int ID;
    protected Object data;
    protected long time;
    protected final InetSocketAddress address;

    /**
     * Constructs a new Packet.
     * @param  ID The integer ID of the packet
     * @param data The object contained by the packet
     */
    public Packet(int ID, Object data, InetSocketAddress address) {
        this.ID = ID;
        this.data = data;
        time = System.currentTimeMillis();
        this.address = address;
    }

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
     * Sets the object contained by the packet.
     *
     * @param data the object to be contained in the packet
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * Returns the assigned integer ID of the packet.
     *
     * @return the integer assigned to identify the packet
     */
    public int getID() {
        return ID;
    }

    /**
     * Returns the time that the packet was created.
     *
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
