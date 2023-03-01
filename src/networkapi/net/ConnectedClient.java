package networkapi.net;

import networkapi.net.packet.DisconnectionPacket;
import networkapi.net.packet.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A {@code ConnectedClient} is a {@code Server}-side representation of a connected socket.
 */
public class ConnectedClient extends Networkable {
    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    private final Timer thread;
    private final Server server;
    private boolean isConnected;

    protected ConnectedClient(Socket socket, Server server) {
        this.socket = socket;
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        this.server = server;

        isConnected = true;

        thread = new Timer();
        thread.schedule(new ClientTask(), 0, 1);
    }

    /**
     * Returns the hostname the client is connected from.
     * @return The client's hostname
     */
    public String getHostname() {
        return socket.getInetAddress().getHostName();
    }

    /**
     * Returns the port the client is connected from.
     * @return The client's port
     */
    public int getPort() {
        return socket.getPort();
    }

    protected Socket getSocket() {
        return socket;
    }

    protected ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    protected ObjectInputStream getInputStream() {
        return inputStream;
    }

    public boolean isConnected() {
        return isConnected;
    }

    protected void destroy() throws IOException {
        isConnected = false;
        thread.cancel();
        outputStream.close();
        inputStream.close();
        socket.close();
    }

    private class ClientTask extends TimerTask {
        @Override
        public void run() {
            try {
                Packet packet = server.readStream(ConnectedClient.this);
                if(packet instanceof DisconnectionPacket)
                    server.removeDisconnectedClient(ConnectedClient.this);
                else
                    server.firePacketListeners(packet);
            } catch (IOException | ClassNotFoundException e) {
                if(isConnected)
                    throw new RuntimeException(e);
            }
        }
    }

    public String toString() {
        return "ConnectedClient["+socket.getInetAddress().getHostName()+":"+socket.getPort()+"]";
    }
}
