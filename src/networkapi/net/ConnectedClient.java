package networkapi.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConnectedClient {
    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;

    public ConnectedClient(Socket socket) {
        this.socket = socket;
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public ObjectInputStream getInputStream() {
        return inputStream;
    }

    public void destroy() {
        try {
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
