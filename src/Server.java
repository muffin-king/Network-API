import packets.Packet;
import packets.PacketEvent;
import packets.PacketListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private final ArrayList<Socket> sockets;

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
        sockets = new ArrayList<>();

        Timer connectThread = new Timer();
        connectThread.schedule(new ConnectionThread(), 0, 1);
    }

    /**
     * Registers a packet listener.
     * @param listener the packet listener to register to the server
     */
    public void registerPacketListener(PacketListener listener) {
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
     * @param socket the socket whose stream will be written to
     */
    public void writeStream(Object out, Socket socket) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(out);
            outputStream.flush();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes an object to a socket's stream.
     * @param out the object to be written to the stream
     * @param address the address whose associated socket's tream will be written to
     */
    public void writeStream(Object out, InetSocketAddress address) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(getSocketByAddress(address).getOutputStream());
            outputStream.writeObject(out);
            outputStream.flush();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a socket associated with the socket address.
     * @param address the address of the socket
     * @throws RuntimeException No connected socket is associated with the address.
     */
    public Socket getSocketByAddress(InetSocketAddress address) {
        for(Socket socket : sockets) {
            InetAddress targetAddress = socket.getInetAddress();
            if(targetAddress.getHostName().equals(address.getHostName()) && socket.getPort() == address.getPort())
                return socket;
        }
        throw new RuntimeException("No such socket with address "+address.getHostName()+":"+address.getPort());
    }

    /**
     * Blocks the current thread until it reads an object from a socket's stream.
     * @param socket the socket whose stream will be read from
     * @return the object read from the stream
     */
    public Object readStream(Socket socket) {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            return inputStream.readObject();
        } catch(IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private class ConnectionThread extends TimerTask {
        @Override
        public void run() {
            try {
                Socket socket = serverSocket.accept();

                Timer socketReader = new Timer();
                socketReader.schedule(new ClientThread(socket), 0, 1);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    private class ClientThread extends TimerTask {
        private final Socket socket;
        private final boolean isDown;
        public ClientThread(Socket socket) {
            this.socket = socket;
            isDown = false;
            System.out.println("Socket "+socket+" connected");
            sockets.add(socket);
        }
        @Override
        public void run() {
            if(!isDown) {
                fireListeners((Packet) readStream(socket));
            } else {
                System.out.println("Terminated socket "+socket);
                sockets.remove(socket);
                cancel();
            }
        }
    }
}
