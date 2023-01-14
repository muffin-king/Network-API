import javax.swing.*;
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
    private final HashMap<Integer, Object> packet;
    private InetSocketAddress address;

    public Client() {
        socket = new Socket();
        System.out.println(socket.getLocalAddress());

        String hostname = JOptionPane.showInputDialog("Enter the server hostname");
        address = new InetSocketAddress(hostname, 8081);

        connectServer(address);

        packet = new HashMap<>();

        Timer packetSender = new Timer();
        packetSender.schedule(new PacketSender(), 0, 1);

        Timer messageReader = new Timer();
        messageReader.schedule(new MessageReader(), 0, 1);
    }

    private void connectServer(InetSocketAddress address) {
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

    private class PacketSender extends TimerTask {
        @Override
        public void run() {
            if(socket.isConnected()) {
                try {
                    writeStream(packet);
                    packet.put(1, null);
                } catch (IOException exception) {
                    if (exception instanceof SocketException) {
                        System.out.println("Disconnected from server");
                        socket = new Socket();
                    } else {
                        throw new RuntimeException(exception);
                    }
                }
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
