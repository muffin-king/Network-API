package networkapi.net;

import networkapi.listener.*;

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
    private final ArrayList<PacketListener> packetListeners;

    private final ArrayList<ConnectionListener> connectionListeners;
    private final ArrayList<ConnectedClient> clients;
    private Timer connectThread;

    /**
     * Constructs a new {@code Server}.
     * @param port The port to open the server on
     */
    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);

        packetListeners = new ArrayList<>();
        connectionListeners = new ArrayList<>();
        clients = new ArrayList<>();

        connectThread = new Timer();
        connectThread.schedule(new ConnectionThread(), 0, 1);
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
        packetListeners.add(listener);
    }

    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    void firePacketListeners(Packet packet) {
        for(PacketListener listener : packetListeners)
            listener.onPacketReceive(new PacketEvent(packet, System.currentTimeMillis()));
    }

    void fireConnectionListeners(ConnectedClient client) {
        for(ConnectionListener listener : connectionListeners)
            listener.onClientConnection(new ConnectionEvent(client, System.currentTimeMillis()));
    }

    void fireDisconnectionListeners(ConnectedClient client, int reason) {
        for(ConnectionListener listener : connectionListeners)
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
     * Shuts down the server.
     * @throws IOException the server socket throws an IOException while closing
     */
    public void shutDown() throws IOException {
        connectThread.cancel();
        for(ConnectedClient client : clients)
            disconnectClient(client);
        serverSocket.close();
    }
}
