package networkapi.net;

import networkapi.listener.*;

import javax.swing.event.EventListenerList;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A Server is a class capable of managing connected {@link Client Clients}, as well as sending and receiving {@link Packet Packets}.
 */
public class Server {
    private ServerSocket serverSocket;
    private final EventListenerList listeners;
    private ArrayList<ConnectedClient> clients;
    private Timer connectThread;
    private PrintStream debugOutput;
    private boolean isAccepting;

    /**
     * Constructs a new {@code Server}.
     * @param debugOutput The {@link PrintStream} for debug messages to be written to, disables debug messages if null
     */
    public Server(PrintStream debugOutput) throws IOException {
        this.debugOutput = debugOutput;

        serverSocket = new ServerSocket();

        listeners = new EventListenerList();
        clients = new ArrayList<>();

        isAccepting = false;

        debugMessage("Unbound server instantiated successfully");
    }

    /**
     * Returns an array of all connected clients.
     * @return an array of {@link ConnectedClient ConnectedClients}
     */
    public ConnectedClient[] getClientArray() {
        ConnectedClient[] ccArray = new ConnectedClient[clients.size()];
        for(int i = 0; i < ccArray.length; i++)
            ccArray[i] = clients.get(i);
        return ccArray;
    }

    protected ArrayList<ConnectedClient> getClients() {
        return clients;
    }

    /**
     * Registers a packet listener implementing {@link PacketListener}.
     * @param listener the packet listener to register to the server
     */
    public void addPacketListener(PacketListener listener) {
        listeners.add(PacketListener.class, listener);
        debugMessage("Registered packet listener "+listener.getClass().getName());
    }

    public void addConnectionListener(ConnectionListener listener) {
        listeners.add(ConnectionListener.class, listener);
        debugMessage("Registered connection listener "+listener.getClass().getName());
    }

    protected void firePacketListeners(Packet packet) {
        for(PacketListener listener : listeners.getListeners(PacketListener.class))
            listener.onPacketReceive(new PacketEvent(packet, System.currentTimeMillis()));
    }

    protected void fireConnectionListeners(ConnectedClient client) {
        for(ConnectionListener listener : listeners.getListeners(ConnectionListener.class))
            listener.onClientConnection(new ConnectionEvent(client, System.currentTimeMillis()));
    }

    protected void fireDisconnectionListeners(ConnectedClient client, int reason) {
        for(ConnectionListener listener : listeners.getListeners(ConnectionListener.class))
            listener.onClientDisconnection(new DisconnectionEvent(client, System.currentTimeMillis(), reason));
    }

    /**
     * Writes a packet to a client's output stream.
     * @param out the {@link Packet} to be written to the stream
     * @param client The client to send the object to
     */
    public void writeStream(Object out, ConnectedClient client) throws IOException {
        client.getOutputStream().writeObject(out);
        client.getOutputStream().flush();
    }

    /**
     * Reads a packet from a client's input stream.
     * Blocks the current thread until a packet is received.
     * @param client the client whose stream will be read from
     * @return the {@link Packet} read from the stream
     */
    protected Object readStream(ConnectedClient client) throws IOException, ClassNotFoundException {
        return client.getInputStream().readObject();
    }

    /**
     * Returns a client associated with the socket address.
     * @param address the address of the socket
     * @return The {@link ConnectedClient} associated with the socket address
     * @throws RuntimeException No connected socket is associated with the address.
     */
    public ConnectedClient getClientByAddress(InetSocketAddress address) {
        for(ConnectedClient client : clients) {
            InetAddress targetAddress = client.getSocket().getInetAddress();
            if(targetAddress.getHostName().equals(address.getHostName()) && client.getSocket().getPort() == address.getPort())
                return client;
        }
        throw new RuntimeException("No such socket with address "+address.getHostName()+":"+address.getPort());
    }

    private class ConnectionThread extends TimerTask {
        @Override
        public void run() {
            try {
                addClient(serverSocket.accept());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addClient(Socket socket) {
        ConnectedClient client = new ConnectedClient(socket, this);
        clients.add(client);
        fireConnectionListeners(client);
        debugMessage(client+" connected to server");
    }

    private void disconnectClient(ConnectedClient client, int reason) throws IOException {
        client.destroy();
        fireDisconnectionListeners(client, reason);
        debugMessage(client+" disconnected from server");
    }

    /**
     * Disconnects a client from the server.
     * @param client The {@link ConnectedClient} to disconnect.
     */
    public void disconnectClient(ConnectedClient client) throws IOException {
        client.destroy();
        fireDisconnectionListeners(client, DisconnectionEvent.SERVER_DISCONNECTION);
        debugMessage(client+" disconnected from server");
    }

    /**
     * Returns if the server is currently capable of accepting connections (is open).
     * @return true if the server is open
     */
    public boolean isAccepting() {
        return isAccepting;
    }

    /**
     * Opens the server on the specified port.
     * @throws IOException the server socket throws an IOException while binding
     */
    public void open(int port) throws IOException {
        debugMessage("Opening server...");
        InetSocketAddress address = new InetSocketAddress(port);
        serverSocket.bind(address);
        connectThread = new Timer();
        connectThread.schedule(new ConnectionThread(), 0, 1);
        isAccepting = true;
        debugMessage("Server opened, bound to "+address.getHostName()+":"+address.getPort());
    }

    /**
     * Opens the server, automatically choosing the port.
     * @throws IOException the server socket throws an IOException while binding
     */
    public void open() throws IOException {
        InetSocketAddress address = new InetSocketAddress(0);
        serverSocket.bind(address);
        connectThread = new Timer();
        connectThread.schedule(new ConnectionThread(), 0, 1);
        isAccepting = true;
        debugMessage("Server opened, bound to "+address.getHostName()+":"+address.getPort());
    }

    /**
     * Shuts down the server.
     * @throws IOException the server socket throws an IOException while closing
     */
    public void close() throws IOException {
        debugMessage("Server closing...");
        connectThread.cancel();
        clients = new ArrayList<>();
        serverSocket.close();
        serverSocket = new ServerSocket();
        isAccepting = false;
        debugMessage("Server closed");
    }

    /**
     * Returns the server's hostname.
     * @return The server's hostname
     */
    public String getHostname() {
        return serverSocket.getInetAddress().getHostName();
    }

    /**
     * Returns the server's port, -1 if the server is closed.
     * @return The server's port
     */
    public int getPort() {
        return isAccepting ? -1 : serverSocket.getLocalPort();
    }

    /**
     * Sets the output stream for debug messages.
     * @param debugOutput The {@link PrintStream} for debug messages to be written to, disables debug messages if null
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
