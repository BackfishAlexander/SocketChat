import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * ChatClient.java
 *
 * Connects to a server to form a messaging service
 *
 * @author abackfis@purdue.edu, hmohite@purdue.edu
 * @version 04/26/2020
 */
final class ChatClient {
    public static String defaultName = "Anonymous";
    public static String defaultServer = "localhost";
    public static int defaultPort = 1500;

    public static Scanner scanner = new Scanner(System.in);

    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        //System.out.println("Connecting to server...");
        //while (true) {
        try {
            socket = new Socket(server, port);
            //break;
        } catch (IOException e) {
            System.out.println("Unable to connect to server.");
            return false;
        }
        //}
        System.out.println("Connected to server.");

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages

        Runnable r = new ListenFromServer();
        Runnable rw = new WritingThread();

        Thread t = new Thread(r);
        Thread tw = new Thread(rw);

        t.start();
        tw.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            System.out.println("Lost connection to server. Closing program...");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            return;
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults
        String user = ChatClient.defaultName;
        String server = ChatClient.defaultServer;
        int port = ChatClient.defaultPort;

        int numArgs = args.length;
        if (numArgs > 0)
            user = args[0];
        if (numArgs > 1)
            try {
                port = Integer.parseInt(args[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        if (numArgs > 2)
            server = args[2];


        // Create your client and start it
        ChatClient client = new ChatClient(server, port, user);
        client.start();

        // Send an empty message to the server
        //client.sendMessage(new ChatMessage());
    }


    /**
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     *
     * @author abackfis@purdue.edu, hmohite@purdue.edu
     * @version 04/26/2020
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            while (true) {
                try {
                    ChatMessage cm = (ChatMessage) sInput.readObject();
                    String msg = cm.getMessage();
                    System.out.println(msg);
                } catch (IOException | ClassNotFoundException e) {
                    //e.printStackTrace();
                    System.out.println("Lost connection to server. Closing program...");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    return;
                }
            }
        }
    }

    /**
     * This is a private class inside of the ChatClient
     * It will be responsible for sending messages to the ChatServer.
     *
     * @author abackfis@purdue.edu, hmohite@purdue.edu
     * @version 04/26/2020
     */

    private final class WritingThread implements Runnable {
        public void run() {
            while (true) {
                Scanner scan = ChatClient.scanner;

                String msg = scan.nextLine();
                try {
                    if (msg.toLowerCase().equals("/logout")) {
                        sendMessage(new ChatMessage(msg, 1));
                        //System.out.println("Lost connection to server. Closing program...");
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        return;
                    }
                    sendMessage(new ChatMessage(msg));
                } catch (Exception e) {
                    //e.printStackTrace();
                    System.out.println("Lost connection to server. Closing program...");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        System.out.println("");
                    }
                    return;
                }
            }
        }
    }
}