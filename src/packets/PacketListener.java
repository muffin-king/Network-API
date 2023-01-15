package packets;

/**
 * An interface with which to create packet listeners that fire a method when a packet is received.
 */
public interface PacketListener {
    /**
     * Method that is fired upon receiving any packet
     *
     * @param e the PacketEvent that is constructed upon the packet's arrival
     */
    void onPacketReceive(PacketEvent e);
}
