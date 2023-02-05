package networkapi.net;

import networkapi.packet.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Timer;

/**
 * A {@code ConnectedClient} is a {@code Server}-side representation of a connected socket.
 */
public class ConnectedClient {
    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    private final Timer clientThread;

    /**
     * Constructs a new {@code ConnectedClient}.
     * @param socket The socket that the client is connected through
     */
    public ConnectedClient(Socket socket) {
        this.socket = socket;
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        clientThread = new Timer();
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
     * Should not be necessary outside of {@code Server}; use {@link Server#writeStream(Packet, ConnectedClient)} to send packets.
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
     * Returns the thread associated with the client.
     * Should not be necessary outside of {@code Server}.
     * @return The client's {@link Timer} thread
     */
    public Timer getClientThread() {
        return clientThread;
    }

    /**
     * Destroys the client by canceling and purging its thread, closing its streams, and closing the socket.
     * Does not remove the client from a {@code Server}'s list of connected clients.
     */
    public void destroy() throws IOException {
        clientThread.cancel();
        clientThread.purge();
        outputStream.close();
        inputStream.close();
        socket.close();
    }
}
