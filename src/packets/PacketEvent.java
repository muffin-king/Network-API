package packets;

public class PacketEvent {
    protected final Packet packet;
    protected final long timeReceived;

    /**
     * Constructs a new PacketEvent.
     *
     * @param packet the packet that the PacketEvent contains
     * @param timeReceived the time the packet was received
     */
    public PacketEvent(Packet packet, long timeReceived) {
        this.packet = packet;
        this.timeReceived = timeReceived;
    }

    /**
     * Returns the packet contained by the PacketEvent
     *
     * @return the packet the PacketEvent contains
     */
    public Packet getPacket() {
        return packet;
    }

    /**
     * Returns the time the packet was received
     *
     * @return the time that the packet was received
     */
    public long getTimeReceived() {
        return timeReceived;
    }
}
