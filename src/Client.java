import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Client {
    private Socket socket;
    private final InetSocketAddress address;
    private final HashMap<Integer, Object> packet;

    public Client() {
        socket = new Socket();
        System.out.println(socket.getLocalAddress());

        address = new InetSocketAddress("localhost", 8081);

        packet = new HashMap<>();

        connectServer();


        Timer packetSender = new Timer();
        packetSender.schedule(new PacketSender(), 0, 1);

        Timer messageReader = new Timer();
        messageReader.schedule(new MessageReader(), 0, 1);

        Timer connector = new Timer();
        connector.schedule(new ServerConnector(), 0, 1);
    }

    private void connectServer() {
        try {
            socket.connect(address);
        } catch (IOException e) {
            return;
        }
        System.out.println("Connected to server " + address.getHostName());
    }

    public void writeStream(Object out) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(out);
        outputStream.flush();
    }

    public Object readStream() throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        return inputStream.readObject();
    }

    public void packObject(int id, Object object) {
        packet.put(id, object);
    }

    private class ServerConnector extends TimerTask {
        @Override
        public void run() {
            if(!socket.isConnected())
                connectServer();
        }
    }

    private class PacketSender extends TimerTask {
        @Override
        public void run() {
            try {
                writeStream(packet);
                packet.put(1, null);
            } catch (IOException exception) {
                if(exception instanceof SocketException) {
                    System.out.println("Disconnected from server");
                    socket = new Socket();
                }
                throw new RuntimeException(exception);
            }
        }
    }

    private class MessageReader extends TimerTask {
        Scanner input;
        String message;
        public MessageReader() {
            input = new Scanner(System.in);
            message = "";
        }
        @Override
        public void run() {
            System.out.print("Enter a message: ");
            message = input.nextLine();
            packObject(InfoType.MESSAGE, message);
            System.out.println();
        }
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        System.out.println();
    }
}
