package networkapi.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A {@code ConnectedClient} is a {@code Server}-side representation of a connected socket.
 */
public class ConnectedClient {
    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    private boolean isDestroyed;
    private final Timer thread;
    private final Server server;

    /**
     * Constructs a new {@code ConnectedClient}.
     * @param socket The socket that the client is connected through
     */
    protected ConnectedClient(Socket socket, Server server) {
        this.socket = socket;
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        isDestroyed = false;

        this.server = server;

        thread = new Timer();
        thread.schedule(new ClientTask(this), 0, 1);
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

    protected void destroy() throws IOException {
        server.getClients().remove(this);
        thread.cancel();
        outputStream.close();
        inputStream.close();
        socket.close();
        isDestroyed = true;
    }

    protected boolean isDestroyed() {
        return isDestroyed;
    }

    private class ClientTask extends TimerTask {
        private final ConnectedClient client;
        public ClientTask(ConnectedClient client) {
            this.client = client;
        }
        @Override
        public void run() {
            try {
                server.firePacketListeners((Packet) inputStream.readObject());
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String toString() {
        return "ConnectedClient["+socket.getInetAddress().getHostName()+":"+socket.getPort()+"]";
    }
}
