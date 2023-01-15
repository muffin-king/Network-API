package packets;

import java.io.Serializable;

public class Packet implements Serializable {
    protected final int ID;
    protected Object data;
    protected long time;

    /**
     * Constructs a new packets.Packet.
     * @param  ID The integer ID of the packet
     * @param data The object contained by the packet
     */
    public Packet(int ID, Object data) {
        this.ID = ID;
        this.data = data;
        time = System.currentTimeMillis();
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
}
