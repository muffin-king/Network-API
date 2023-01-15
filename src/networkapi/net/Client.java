package networkapi.net;

import networkapi.packet.Packet;
import networkapi.packet.PacketEvent;
import networkapi.packet.PacketListener;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Client {
    private Socket socket;
    private final ArrayList<PacketListener> packetListeners;

    /**
     * Constructs a new Client, capable of sending and receiving packets to a connected server.
     */
    public Client(InetSocketAddress address) {
        socket = new Socket();
        System.out.println(socket.getLocalAddress());

        connectServer(address);

        packetListeners = new ArrayList<>();

        Timer clientThread = new Timer();
        clientThread.schedule(new ListenerThread(), 0, 1);

        System.out.println(socket.getLocalPort());
    }

    public Client(String hostname, int port) {
        socket = new Socket();
        System.out.println(socket.getLocalAddress());

        connectServer(new InetSocketAddress(hostname, port));

        packetListeners = new ArrayList<>();

        Timer clientThread = new Timer();
        clientThread.schedule(new ListenerThread(), 0, 1);

        System.out.println(socket.getLocalPort());
    }

    public Client() {
        socket = new Socket();
        System.out.println(socket.getLocalAddress());

        packetListeners = new ArrayList<>();

        Timer clientThread = new Timer();
        clientThread.schedule(new ListenerThread(), 0, 1);

        System.out.println(socket.getLocalPort());
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
     * Connects to a server at the specified address.
     * @param address the address to attempt to connect to
     */
    public void connectServer(InetSocketAddress address) {
        try {
            socket.connect(address);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Connected to server " + address.getHostName());
    }

    /**
     * Disconnects from the current server by instantiating a new Socket in place of the current socket.
     */
    public void disconnect() {
        socket = new Socket();
    }

    /**
     * Writes an object to a socket's stream.
     * @param out the object to be written to the stream
     */
    public void writeStream(Object out) {
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
     */
    public Object readStream() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            return inputStream.readObject();
        } catch(IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private class ListenerThread extends TimerTask {
        @Override
        public void run() {
            fireListeners((Packet) readStream());
        }
    }
}
