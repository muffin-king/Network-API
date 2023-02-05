package networkapi.packet;

/**
 * An interface with which to create packet event listeners that fire {@code onPacketReceive} when a packet is received.
 */
public interface PacketListener {
    /**
     * Method that is fired upon receiving any packet.
     * @param e the {@link PacketEvent} that is constructed upon the packet's arrival
     */
    void onPacketReceive(PacketEvent e);
}
