package networkapi.net.packet;

import networkapi.net.Networkable;

import java.io.Serializable;

public class DisconnectionPacket extends Packet {
    public DisconnectionPacket(Networkable origin) {
        super(0, null, origin);
    }
}
