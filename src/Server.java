import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
    private ServerSocket serverSocket;
    InputStream input;
    OutputStream output;
    private ConnectionThread connectionAcceptor;

    public Server() throws IOException, InterruptedException {
        serverSocket = new ServerSocket(8081);
        System.out.println(serverSocket.getLocalSocketAddress());

        Timer connectThread = new Timer();
        connectThread.schedule(new ConnectionThread(), 0, 1);

    }

    public void writeStream(Object out, Socket socket) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(out);
        outputStream.flush();
    }

    public Object readStream(Socket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        return inputStream.readObject();
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
        private Socket socket;
        private boolean isDown;
        public ClientThread(Socket socket) {
            this.socket = socket;
            isDown = false;
            System.out.println("Socket "+socket+" connected");
        }
        @Override
        public void run() {
            if(!isDown) {
                try {
                    Object packetVal = ((HashMap<Integer, Object>) readStream(socket)).get(InfoType.MESSAGE);

                    if (packetVal != null)
                        System.out.println(packetVal);
                } catch (IOException | ClassNotFoundException exception) {
                    if (exception instanceof SocketException) {
                        isDown = true;
                    } else {
                        throw new RuntimeException(exception);
                    }
                }
            } else {
                System.out.println("Terminated socket "+socket);
                cancel();
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = new Server();
    }
}
