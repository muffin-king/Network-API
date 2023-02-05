package networkapi.net;

import networkapi.packet.Packet;
import networkapi.packet.PacketEvent;
import networkapi.packet.PacketListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A Client is a class capable of connecting to {@link Server Servers}, as well as sending and receiving {@link Packet Packets}.
 */
public class Client {
    private Socket socket;
    private final ArrayList<PacketListener> packetListeners;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Timer listenerThread;

    /**
     * Constructs a new {@code Client} and attempts to connect to the specified address.
     * @param hostname The hostname of the address to connect to.
     * @param port The port of the address to connect to.
     */
    public Client(String hostname, int port) {
        socket = new Socket();
        System.out.println(socket.getLocalAddress());

        connectServer(new InetSocketAddress(hostname, port));

        packetListeners = new ArrayList<>();

        System.out.println(socket.getLocalPort());
    }

    /**
     * Constructs a new unconnected Client.
     */
    public Client() {
        socket = new Socket();
        System.out.println(socket.getLocalAddress());

        packetListeners = new ArrayList<>();

        System.out.println(socket.getLocalPort());
    }

    /**
     * Returns the client's socket.
     * @return The client's {@link Socket}
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Registers a packet listener implementing {@link PacketListener}.
     * @param listener the packet listener to register to the client
     */
    public void addPacketListener(PacketListener listener) {
        packetListeners.add(listener);
    }

    private void fireListeners(Packet packet) {
        for(PacketListener listener : packetListeners) {
            listener.onPacketReceive(new PacketEvent(packet, System.currentTimeMillis()));
        }
    }

    /**
     * Connects to a {@link Server} at the specified address.
     * @param address the address to attempt to connect to
     * @throws RuntimeException The client is already connected to an address.
     */
    public void connectServer(InetSocketAddress address) {
        if(socket.isConnected()) {
            throw new RuntimeException("Cannot connect a Client that is already connected to an address");
        } else {
            try {
                socket.connect(address);
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());

                listenerThread = new Timer();
                listenerThread.schedule(new ListenerTask(), 0, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Connected to server " + address.getHostName());
        }
    }

    /**
     * Disconnects from the currently connected {@link Server}.
     * @throws RuntimeException The client is not connected to any address.
     */
    public void disconnect() throws IOException {
        if(socket.isConnected()) {
            listenerThread.cancel();

            outputStream.close();
            inputStream.close();
            socket.close();
            socket = new Socket();
        } else {
            throw new RuntimeException("Cannot disconnect an unconnected Client");
        }
    }

    /**
     * Writes a {@code Packet} to the client's output stream.
     * @param out the packet to be written to the stream
     */
    public void writeStream(Packet out) throws IOException {
        outputStream.writeObject(out);
        outputStream.flush();
    }

    /**
     * Reads a {@code Packet} from the client's input stream.
     */
    public Packet readStream() throws IOException, ClassNotFoundException {
        return (Packet) inputStream.readObject();
    }

    private class ListenerTask extends TimerTask {
        @Override
        public void run() {
            try {
                fireListeners(readStream());
            } catch (IOException | ClassNotFoundException ignored) {}
        }
    }
}
