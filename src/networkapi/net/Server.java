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

public class Server {
    private final ServerSocket serverSocket;
    private final ArrayList<PacketListener> packetListeners;
    private final ArrayList<ConnectedClient> clients;
    private final Timer clientsTimer;

    /**
     * Constructs a new Server, capable of sending and receiving packets to connected clients.
     */
    public Server() {
        try {
            serverSocket = new ServerSocket(8081);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(serverSocket.getLocalSocketAddress());

        packetListeners = new ArrayList<>();
        clients = new ArrayList<>();

        clientsTimer = new Timer();

        Timer connectThread = new Timer();
        connectThread.schedule(new ConnectionThread(), 0, 1);
    }

    /**
     * Returns an array of all connected sockets.
     * @return an array of connected sockets
     */
    public ConnectedClient[] getSockets() {
        ConnectedClient[] ccArray = new ConnectedClient[clients.size()];
        for(int i = 0; i < ccArray.length; i++)
            ccArray[i] = clients.get(i);
        return ccArray;
    }

    /**
     * Registers a packet listener.
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
     * Writes an object to a socket's stream.
     * @param out the object to be written to the stream
     * @param client The client to send the object to
     */
    public void writeStream(Object out, ConnectedClient client) {
        try {
            client.getOutputStream().writeObject(out);
            client.getOutputStream().flush();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Blocks the current thread until it reads an object from a socket's stream.
     * @param client the client whose stream will be read from
     * @return the object read from the stream
     */
    public Object readStream(ConnectedClient client) throws IOException, ClassNotFoundException {
        return client.getInputStream().readObject();
    }

    /**
     * Returns a socket associated with the socket address.
     * @param address the address of the socket
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
                Socket socket = serverSocket.accept();
                clientsTimer.schedule(new ClientThread(new ConnectedClient(socket)), 0, 1);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    private class ClientThread extends TimerTask {
        private final ConnectedClient client;
        private boolean isDown;
        public ClientThread(ConnectedClient client) {
            this.client = client;
            isDown = false;
            System.out.println("Socket "+client.getSocket()+" connected");
            clients.add(client);
        }
        @Override
        public void run() {
            if(!isDown) {
                try {
                    fireListeners((Packet) readStream(client));
                } catch (IOException e) {
                    isDown = true;
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                clients.remove(client);
                client.destroy();
                System.out.println("Terminated socket "+ client);
                cancel();
            }
        }
    }
}
