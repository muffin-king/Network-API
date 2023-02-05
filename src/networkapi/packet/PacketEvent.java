package networkapi.packet;

/**
 * A {@code PacketEvent} is a class that represents when a {@link Packet} was received.
 */
public class PacketEvent {
    private final Packet packet;
    private final long timeReceived;

    /**
     * Constructs a new {@code PacketEvent}.
     * @param packet the packet that the {@code PacketEvent} will contain
     * @param timeReceived the time the packet was received
     */
    public PacketEvent(Packet packet, long timeReceived) {
        this.packet = packet;
        this.timeReceived = timeReceived;
    }

    /**
     * Returns the packet contained by the {@code PacketEvent}
     * @return the packet the {@code PacketEvent} contains
     */
    public Packet getPacket() {
        return packet;
    }

    /**
     * Returns the time the packet was received
     * @return the time that the packet was received
     */
    public long getTimeReceived() {
        return timeReceived;
    }
}
