package networkapi.net;

import networkapi.listener.*;
import networkapi.net.packet.DisconnectionPacket;
import networkapi.net.packet.Packet;

import javax.swing.event.EventListenerList;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A Server is a class capable of managing connected {@link Client Clients}, as well as sending and receiving {@link Packet Packets}.
 */
public class Server extends Networkable {
    private ServerSocket serverSocket;
    private final EventListenerList listeners;
    private final ArrayList<ConnectedClient> clients;
    private Timer connectThread;
    private PrintStream debugOutput;
    private boolean isAccepting;
    private final DisconnectionPacket disconnectionPacket;

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

        disconnectionPacket = new DisconnectionPacket(this);

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

    /**
     * Registers a packet listener implementing {@link PacketListener}.
     * @param listener the packet listener to register to the server
     */
    public void addPacketListener(PacketListener listener) {
        listeners.add(PacketListener.class, listener);
        debugMessage("Registered packet listener "+listener.getClass().getName());
    }

    /**
     * Registers a connection listener implementing {@link ConnectionListener}.
     * @param listener the connection listener to register to the server
     */
    public void addConnectionListener(ConnectionListener listener) {
        listeners.add(ConnectionListener.class, listener);
        debugMessage("Registered connection listener "+listener.getClass().getName());
    }

    void firePacketListeners(Packet packet) {
        for(PacketListener listener : listeners.getListeners(PacketListener.class))
            listener.onPacketReceive(new PacketEvent(packet, System.currentTimeMillis()));
    }

    private void fireConnectionListeners(ConnectedClient client) {
        for(ConnectionListener listener : listeners.getListeners(ConnectionListener.class))
            listener.onClientConnection(new ConnectionEvent(client, System.currentTimeMillis()));
    }

    private void fireDisconnectionListeners(ConnectedClient client, int reason) {
        for(ConnectionListener listener : listeners.getListeners(ConnectionListener.class))
            listener.onClientDisconnection(new DisconnectionEvent(client, System.currentTimeMillis(), reason));
    }

    /**
     * Writes a packet to a client's output stream.
     * @param out the {@link Packet} to be written to the stream
     * @param client The client to send the object to
     */
    public void writeStream(Packet out, ConnectedClient client) throws IOException {
        client.getOutputStream().writeObject(out);
        client.getOutputStream().flush();
    }

    /**
     * Writes a packet to a client's output stream.
     * @param ID the ID of the packet
     * @param data the {@code Serializable} object to be contained by the packet
     * @param client The client to send the object to
     */
    public void writeStream(int ID, Serializable data, ConnectedClient client) throws IOException {
        client.getOutputStream().writeObject(new Packet(ID, data, client));
        client.getOutputStream().flush();
    }

    protected Packet readStream(ConnectedClient client) throws IOException, ClassNotFoundException {
        return (Packet) client.getInputStream().readObject();
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
                if(isAccepting)
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
        writeStream(disconnectionPacket, client);
        clients.remove(client);
        client.destroy();
        fireDisconnectionListeners(client, reason);
        debugMessage(client+" disconnected from server");
    }

    protected void removeDisconnectedClient(ConnectedClient client) throws IOException {
        clients.remove(client);
        client.destroy();
        fireDisconnectionListeners(client, DisconnectionEvent.SERVER_DISCONNECTION);
        debugMessage(client+" disconnected from server");
    }

    /**
     * Disconnects a client from the server.
     * @param client The {@link ConnectedClient} to disconnect.
     */
    public void disconnectClient(ConnectedClient client) throws IOException {
        writeStream(disconnectionPacket, client);
        clients.remove(client);
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
     * @param port The port of the server, automatically picks a port if 0
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
     * Shuts down the server.
     * @throws IOException the server socket throws an IOException while closing
     */
    public void close() throws IOException {
        debugMessage("Server closing...");
        connectThread.cancel();
        for(int i = 0; i < clients.size(); i++) {
            disconnectClient(clients.get(i), DisconnectionEvent.SERVER_DISCONNECTION);
        }
        serverSocket.close();
        serverSocket = new ServerSocket();
        isAccepting = false;
        debugMessage("Server closed");
    }

    /**
     * Returns the server's hostname.
     * @return The server's hostname, null if unbound
     */
    public String getHostname() {
        return !isAccepting ? null : serverSocket.getInetAddress().getHostName();
    }

    /**
     * Returns the server's port, -1 if the server is closed.
     * @return The server's port
     */
    public int getPort() {
        return !isAccepting ? -1 : serverSocket.getLocalPort();
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
