package networkapi.net;

import networkapi.listener.PacketEvent;
import networkapi.listener.PacketListener;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A Client is a class capable of connecting to {@link Server Servers}, as well as sending and receiving {@link Packet Packets}.
 */
public class Client {
    private Socket socket;
    private final EventListenerList listeners;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Timer listenerThread;
    private PrintStream debugOutput;
    private boolean isConnected;

    /**
     * Constructs a new unconnected Client.
     * @param debugOutput The {@link PrintStream} for debug messages to be written to, disables debug messages if null
     */
    public Client(PrintStream debugOutput) {
        socket = new Socket();
        listeners = new EventListenerList();
        this.debugOutput = debugOutput;
        isConnected = false;
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
     * Returns the hostname the client is connected to, {@code null} if unconnected.
     * @return The hostname the client is connected to
     */
    public String getConnectedHostname() {
        return isConnected ? socket.getInetAddress().getHostName() : null;
    }

    /**
     * Returns the port the client is connected to, -1 if unconnected.
     * @return The port the client is connected to
     */
    public int getConnectedPort() {
        return isConnected ? socket.getPort() : -1;
    }

    /**
     * Returns the state of the client's connection
     * @return true if the client is connected to an address
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Registers a packet listener implementing {@link PacketListener}.
     * @param listener the packet listener to register to the client
     */
    public void addPacketListener(PacketListener listener) {
        listeners.add(PacketListener.class, listener);
        debugMessage("Registered packet listener "+listener.getClass().getName());
    }

    private void firePacketListeners(Packet packet) {
        for(PacketListener listener : listeners.getListeners(PacketListener.class)) {
            listener.onPacketReceive(new PacketEvent(packet, System.currentTimeMillis()));
        }
    }

    /**
     * Connects to a {@link Server} at the specified address.
     * @param address the address to attempt to connect to
     * @throws RuntimeException The client is already connected to an address.
     */
    public void connectServer(InetSocketAddress address) {
        debugMessage("Connecting to server...");
        if(isConnected) {
            throw new RuntimeException("Cannot connect a Client that is already connected to an address");
        } else {
            try {
                socket.bind(null);
                socket.connect(address);
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());

                listenerThread = new Timer();
                listenerThread.schedule(new ListenerTask(), 0, 1);

                isConnected = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        debugMessage("Connected to Server["+address.getHostName()+":"+address.getPort()+"]");
    }

    /**
     * Disconnects from the currently connected {@link Server}.
     * @throws RuntimeException The client is not connected to any address.
     */
    public void disconnect() throws IOException {
        debugMessage("Disconnecting client...");
        if(isConnected) {
            isConnected = false;
            listenerThread.cancel();
            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();
            socket = new Socket();
        } else {
            throw new RuntimeException("Cannot disconnect an unconnected client");
        }
        debugMessage("Client disconnected");
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
    protected Object readStream() throws IOException, ClassNotFoundException {
        return inputStream.readObject();
    }

    private class ListenerTask extends TimerTask {
        @Override
        public void run() {
            try {
                firePacketListeners((Packet) readStream());
            } catch (IOException | ClassNotFoundException e) {
                try {
                    disconnect();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    /**
     * Sets the output stream for debug messages.
     * @param debugOutput The {@link PrintStream} for debug messages to be written to
     */
    public void setDebugOutput(PrintStream debugOutput) {
        this.debugOutput = debugOutput;
        debugMessage("Debug output set to "+debugOutput);
    }

    private void debugMessage(String message) {
        if(debugOutput != null)
            debugOutput.println(message);
    }
}
