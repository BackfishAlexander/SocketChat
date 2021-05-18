import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * ChatServer.java
 *
 * Runs a server to handle chats between many clients
 *
 * @author abackfis@purdue.edu, hmohite@purdue.edu
 * @version 04/26/2020
 */
final class ChatServer {
    public static String welcomeMessage = "Welcome to JavaChat!\n" +
            "Type /help for commands";
    public static String helpMessage = "Commands:\n/list\n/msg <username> <message>\n/logout";

    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private ChatFilter filter;
    private Object gate;


    private ChatServer(int port) {
        this.port = port;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("JavaChat Server Started.");
            while (true) {
                Socket socket = serverSocket.accept();
                /*String usr = "";
                try {
                    ObjectInputStream temp = new ObjectInputStream(socket.getInputStream());
                    usr = (String) temp.readObject();
                    temp.close();
                    if (!(findName(usr) == "")) {
                        socket.close();
                        continue;
                    }
                } catch (Exception e) {
                    socket.close();
                    continue;
                }*/
                ClientThread c = new ClientThread(socket, uniqueId++);
                Runnable r = c;
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                //c.server = this;
                t.start();
                //c.username = usr;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void broadcast(String message) {
        //System.out.println(message);
        for (int i = 0; i < clients.size(); i++) {
            try {
                ClientThread client = clients.get(i);
                //ChatFilter filter = this.filter;
                //ChatFilter filter = new ChatFilter("badwords");
                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                Date date = new Date();
                String finalString = String.format("[%s] %s", getDate(), message);
                finalString = this.filter.filter(finalString);
                client.writeMessage(finalString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void directMessage(String message, String username) {
        for (int i = 0; i < clients.size(); i++) {
            try {
                ClientThread client = clients.get(i);
                if (client.username.toLowerCase().equals(username.toLowerCase())) {
                    client.writeMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void remove(int id) {
        for (int i = 0; i < clients.size(); i++) {
            try {
                ClientThread client = clients.get(i);
                if (client.id == id) {
                    client.sInput.close();
                    client.sOutput.close();
                    client.socket.close();
                    clients.remove(client);
                }
            } catch (Exception e) {
                System.out.println("unable to remove");
            }
        }
    }

    private void remove(ClientThread object) {
        for (int i = 0; i < clients.size(); i++) {
            try {
                ClientThread client = clients.get(i);
                if (client.equals(object)) {
                    client.sInput.close();
                    client.sOutput.close();
                    client.socket.close();
                    clients.remove(client);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void close() {
        for (int i = 0; i < clients.size(); i++) {
            remove(clients.get(i));
        }
    }

    private String findName(String name) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = clients.get(i);
            if (client.username.toLowerCase().equals(name.toLowerCase()))
                return client.username;
        }
        return "";
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        int port;
        String filter = "";
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[0]);
                filter = args[1];
            } catch (Exception e) {
                port = 1500;
            }
        } else if (args.length > 0)
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                port = 1500;
            }
        else
            port = 1500;

        ChatServer server = new ChatServer(port);
        server.filter = new ChatFilter(filter);
        server.start();
    }


    /**
     * ClientThread subclass
     * <p>
     * Threaded client listener for input and output sent between users
     *
     * @author abackfis@purdue.edu, hmohite@purdue.edu
     * @version 04/26/2020
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username = "Anonymous";
        ChatMessage cm;
        //ChatServer server;

        private boolean writeMessage(String message) {
            try {
                this.sOutput.writeObject(new ChatMessage(message));
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
                if (!findName(username).equals("")) {
                    sOutput.writeObject(new ChatMessage("Username is already taken."));
                    sOutput.close();
                    sInput.close();
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client
            if (!socket.isClosed())
                directMessage(ChatServer.welcomeMessage, this.username);
            else {
                clients.remove(this);
                return;
            }

            while (true) {
                String msg;
                String[] arguments;
                String command;
                try {
                    cm = (ChatMessage) sInput.readObject();
                    if (cm.getType() == 1) {
                        System.out.println(username + " has logged off.");
                        broadcast(username + " has logged off.");
                        remove(this);
                        break;
                    }
                    msg = cm.getMessage();
                    arguments = msg.split(" ");
                    command = arguments[0].toLowerCase();
                    if (command.equals("/msg")) {
                        try {
                            String user = arguments[1];
                            if (!this.username.toLowerCase().equals(user.toLowerCase())) {
                                if (!findName(user).equals("")) {
                                    String pmMsg = msg.substring(msg.indexOf(arguments[2]));
                                    String pm = "[PM from " + this.username + "]: " + pmMsg;
                                    String pm2 = "[PM to " + findName(user) + "]: " + pmMsg;
                                    directMessage(pm, user);
                                    directMessage(pm2, this.username);

                                } else {
                                    directMessage("User " + user + " not found.", this.username);
                                }
                            } else {
                                directMessage("Cannot send private message to yourself.", this.username);
                            }
                        } catch (Exception e) {
                            directMessage("Syntax: /msg <username> <message>", this.username);
                        }
                    } else if (command.equals("/list")) {
                        StringBuilder list = new StringBuilder();
                        list.append("---Other online users---");
                        for (int i = 0; i < clients.size(); i++) {
                            if (!this.username.equals(clients.get(i).username)) {
                                list.append("\n");
                                list.append(clients.get(i).username);
                            }
                        }
                        if (clients.size() == 0)
                            list.append("\n");
                        directMessage(list.toString(), this.username);
                    } else if (command.equals("/help")) {
                        directMessage(ChatServer.helpMessage, this.username);
                    } else {
                        broadcast(username + ": " + msg);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println(username + " has logged off.");
                    broadcast(username + " has logged off.");
                    //e.printStackTrace();
                    clients.remove(this);
                    break;
                }
                System.out.println(String.format("[%s] %s: %s", getDate(), this.username, msg));


                // Send message back to the client
                /*try {
                    sOutput.writeObject("Pong");
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        }
    }
}
