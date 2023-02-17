package networkapi.net;

import networkapi.listener.PacketEvent;
import networkapi.listener.PacketListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
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

        connectServer(new InetSocketAddress(hostname, port));

        packetListeners = new ArrayList<>();
    }

    /**
     * Constructs a new unconnected Client.
     */
    public Client() {
        socket = new Socket();

        packetListeners = new ArrayList<>();
    }

    /**
     * Returns the client's hostname.
     * @return The client's hostname
     */
    public String getHostname() {
        return socket.getLocalAddress().getHostName();
    }

    /**
     * Returns the client's port.
     * @return The client's port
     */
    public int getPort() {
        return socket.getLocalPort();
    }

    /**
     * Returns the hostname the client is connected to.
     * @return The hostname the client is connected to
     */
    public String getConnectedHostname() {
        return socket.getInetAddress().getHostName();
    }

    /**
     * Returns the port the client is connected to.
     * @return The port the client is connected to
     */
    public int getConnectedPort() {
        return socket.getPort();
    }

    /**
     * Returns the state of the client's connection
     * @return true if the client's {@code Socket} is both connected to an address and is not closed
     */
    public boolean isConnected() {
        return socket.isConnected() && !socket.isClosed();
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
        }
    }

    /**
     * Disconnects from the currently connected {@link Server}.
     * @throws RuntimeException The client is not connected to any address.
     */
    public void disconnect() throws IOException {
        if(socket.isConnected()) {
            listenerThread.cancel();
            socket.shutdownOutput();
            socket.shutdownInput();
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
    public void writeStream(Object out) throws IOException {
        outputStream.writeObject(out);
        outputStream.flush();
    }

    /**
     * Reads a {@code Packet} from the client's input stream.
     */
    public Object readStream() throws IOException, ClassNotFoundException {
        try {
            return inputStream.readObject();
        } catch(SocketException e) {
            if(e.getMessage().equals("Socket closed"))
                return null;
            else
                throw new RuntimeException(e);
        }
    }

    private class ListenerTask extends TimerTask {
        @Override
        public void run() {
            try {
                fireListeners((Packet) readStream());
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
