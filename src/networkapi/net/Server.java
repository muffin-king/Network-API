package networkapi.net;

import networkapi.packet.Packet;
import networkapi.packet.PacketEvent;
import networkapi.packet.PacketListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A Server is a class capable of managing connected {@link Client Clients}, as well as sending and receiving {@link Packet Packets}.
 */
public class Server {
    private final ServerSocket serverSocket;
    private final ArrayList<PacketListener> packetListeners;
    private final ArrayList<ConnectedClient> clients;

    /**
     * Constructs a new {@code Server}.
     * @param port The port to open the server on
     */
    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println(serverSocket.getLocalSocketAddress());

        packetListeners = new ArrayList<>();
        clients = new ArrayList<>();

        Timer connectThread = new Timer();
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

    private void fireListeners(Packet packet) {
        for(PacketListener listener : packetListeners) {
            listener.onPacketReceive(new PacketEvent(packet, System.currentTimeMillis()));
        }
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
     * Reads a packet from a client's input stream.
     * Blocks the current thread until a packet is received.
     * @param client the client whose stream will be read from
     * @return the {@link Packet} read from the stream
     */
    public Packet readStream(ConnectedClient client) throws IOException, ClassNotFoundException {
        return (Packet) client.getInputStream().readObject();
    }

    /**
     * Returns a client associated with the socket address.
     * @param address the address of the socket
     * @return The {@link ConnectedClient} associated with the socket address
     * @throws RuntimeException No connected socket is associated with the address.
     */
    public Socket getSocketByAddress(InetSocketAddress address) {
        for(ConnectedClient client : clients) {
            Socket socket = client.getSocket();
            InetAddress targetAddress = socket.getInetAddress();
            if(targetAddress.getHostName().equals(address.getHostName()) && socket.getPort() == address.getPort())
                return socket;
        }
        throw new RuntimeException("No such socket with address "+address.getHostName()+":"+address.getPort());
    }

    private class ConnectionThread extends TimerTask {
        @Override
        public void run() {
            try {
                addClient(serverSocket.accept());
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    private class ClientThread extends TimerTask {
        private final ConnectedClient client;
        public ClientThread(ConnectedClient client) {
            this.client = client;
        }
        @Override
        public void run() {
            try {
                fireListeners(readStream(client));
            } catch (IOException | ClassNotFoundException e) {
                try {
                    disconnectClient(client);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                cancel();
            }
        }
    }

    private void addClient(Socket socket) {
        ConnectedClient client = new ConnectedClient(socket);
        client.getClientThread().schedule(new ClientThread(client), 0, 1);
        clients.add(client);
        System.out.println("Socket "+client.getSocket()+" connected");
    }

    /**
     * Disconnects a client from the server.
     * @param client The {@link ConnectedClient} to disconnect.
     */
    public void disconnectClient(ConnectedClient client) throws IOException {
        System.out.println("Terminating socket "+ client.getSocket());
        clients.remove(client);
        client.destroy();
    }
}
