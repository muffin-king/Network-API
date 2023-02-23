package networkapi.net;

import networkapi.listener.*;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A Server is a class capable of managing connected {@link Client Clients}, as well as sending and receiving {@link Packet Packets}.
 */
public class Server {
    private final ServerSocket serverSocket;
    private final EventListenerList listeners;
    private final ArrayList<ConnectedClient> clients;
    private Timer connectThread;

    /**
     * Constructs a new {@code Server}.
     */
    public Server() throws IOException {
        serverSocket = new ServerSocket();

        listeners = new EventListenerList();
        clients = new ArrayList<>();
    }

    /**
     * Returns an array of all connected clients.
     * @return an array of {@link ConnectedClient ConnectedClients}
     */
    public ConnectedClient[] getClients() {
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
    }

    public void addConnectionListener(ConnectionListener listener) {
        listeners.add(ConnectionListener.class, listener);
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
    }

    private void disconnectClient(ConnectedClient client, int reason) throws IOException {
        clients.remove(client);
        client.destroy();
        fireDisconnectionListeners(client, reason);
    }

    /**
     * Disconnects a client from the server.
     * @param client The {@link ConnectedClient} to disconnect.
     */
    public void disconnectClient(ConnectedClient client) throws IOException {
        clients.remove(client);
        client.destroy();
        fireDisconnectionListeners(client, DisconnectionEvent.SERVER_DISCONNECTION);
    }

    /**
     * Returns if the server is currently capable of accepting connections (is open).
     * @return true if the server socket is bound and not closed.
     */
    public boolean isAccepting() {
        return serverSocket.isBound() && !serverSocket.isClosed();
    }

    /**
     * Opens the server on the specified port.
     * @throws IOException the server socket throws an IOException while binding
     */
    public void open(int port) throws IOException {
        serverSocket.bind(new InetSocketAddress(port));
        connectThread = new Timer();
        connectThread.schedule(new ConnectionThread(), 0, 1);
    }

    /**
     * Opens the server, automatically choosing the port.
     * @throws IOException the server socket throws an IOException while binding
     */
    public void open() throws IOException {
        serverSocket.bind(null);
        connectThread = new Timer();
        connectThread.schedule(new ConnectionThread(), 0, 1);
    }

    /**
     * Shuts down the server.
     * @throws IOException the server socket throws an IOException while closing
     */
    public void close() throws IOException {
        connectThread.cancel();
        for(ConnectedClient client : clients)
            disconnectClient(client);
        serverSocket.close();
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
        return serverSocket.isClosed() ? -1 : serverSocket.getLocalPort();
    }
}
