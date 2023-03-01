package networkapi.net.packet;

import networkapi.net.Networkable;

public class DisconnectionPacket extends Packet {
    /**
     * Instantiates a new DisconnectionPacket, which is capable of instructing a connected {@code Client} or {@code Server} to disconnect.
     * Should not be necessary, use the {@code Client} or {@code Server}'s built-in disconnection methods instead.
     * @param origin The {@code Networkable} origin of the disconnection packet
     */
    public DisconnectionPacket(Networkable origin) {
        super(0, null, origin);
    }
}
