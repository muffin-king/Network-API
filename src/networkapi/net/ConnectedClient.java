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
    ConnectedClient(Socket socket, Server server) {
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
     * Returns the client's associated socket.
     * @return The client's {@link Socket}
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Returns the object output stream created for the socket
     * Should not be necessary outside of {@code Server}; use {@code Server.writeStream} to send packets.
     * @return An {@link ObjectOutputStream} associated with the socket
     */
    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Returns the object input stream created for the socket
     * Should not be necessary outside of {@code Server}.
     * @return An {@link ObjectInputStream} associated with the socket
     */
    public ObjectInputStream getInputStream() {
        return inputStream;
    }

    /**
     * Destroys the client closing its streams and closing the socket.
     * Does not remove the client from a {@code Server}'s list of connected clients.
     */
    public void destroy() throws IOException {
        thread.cancel();
        outputStream.close();
        inputStream.close();
        socket.close();
        isDestroyed = true;
    }

    public boolean isDestroyed() {
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
                server.firePacketListeners((Packet) server.readStream(client));
            } catch (IOException | ClassNotFoundException e) {
                try {
                    server.disconnectClient(client);
                    cancel();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                throw new RuntimeException(e);
            }
        }
    }
}
