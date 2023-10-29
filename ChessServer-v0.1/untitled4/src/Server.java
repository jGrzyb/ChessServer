import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    private ArrayList<Game> games = new ArrayList<>();

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            System.out.println("SERVER: Server is running");
            while(!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }
    public void broadcast(String message) {
        for(ConnectionHandler ch : connections) {
            if(ch != null) {
                ch.sendMessage(message);
            }
        }
    }
    public void shutdown() {
        try {
            done = true;
            if(!server.isClosed()) {
                server.close();
            }
            for(ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public ConnectionHandler findUser(String nickname) {
        for(ConnectionHandler ch : connections) {
            if(Objects.equals(ch.nickname, nickname)) {
                return ch;
            }
        }
        System.out.println("SERVER: Nie znaleziono uzytkownika: " + nickname);
        return null;
    }

    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;
        private String supposedName;
        private Game game = null;
        private String inviter = null;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter your nickname: ");
                supposedName = in.readLine();
                while(findUser(supposedName) != null) {
                    out.println("Please choose another nickname: ");
                    supposedName = in.readLine();
                }
                nickname = supposedName;
                System.out.println("SERVER: " + nickname + " connected!    Online: " + connections.size());
                broadcast("SERVER: " + nickname + " joined!");
                String message;
                while((message = in.readLine()) != null) {
                    if(message.startsWith("/quit")) {
                        String leftName = nickname;
                        broadcast("SERVER: " + nickname + " left");
                        shutdown();
                        System.out.println("SERVER: " + leftName + " left.    Online: " + connections.size());
                        break;
                    }
                    else if(game == null) {
                        outsideGame(message);
                    }
                    else {
                        inGame(message);
                    }
//                    if(message.startsWith("/nick")) {
//                        String[] messageSplit = message.split(" ", 2);
//                        if(messageSplit.length == 2) {
//                            System.out.println("SERVER: " + nickname + " changed nickname to " + messageSplit[1]);
//                            out.println("SERVER: Successfully changed nickname to: " + messageSplit[1]);
//                            broadcast("SERVER: " + nickname + " changed nickname to " + messageSplit[1]);
//                            nickname = messageSplit[1];
//                        }
//                        else {
//                            out.println("SERVER: No nickname provided.");
//                        }
//                    }
//                    else if(message.startsWith("/quit")) {
//                        String leftName = nickname;
//                        broadcast("SERVER: " + nickname + " left");
//                        shutdown();
//                        System.out.println("SERVER: " + leftName + " left.    Online: " + connections.size());
//                        break;
//                    }
//                    else {
//                        broadcast(nickname + ": " + message);
//                    }
                }
            } catch(IOException e) {
                shutdown();
            }
        }

        public void outsideGame(String message) {
            if(message.startsWith("/playWith")) {
                String[] messageSplit = message.split(" ", 2);
                if(findUser(messageSplit[1]) != null) {
                    ConnectionHandler ch = findUser(messageSplit[1]);
                    if(Objects.equals(ch.nickname, nickname)) {
                        out.println("Wanna play with yourself ?!");
                        return;
                    }
                    if(ch.game == null) {
                        ch.sendMessage("Wanna play with: " + nickname + "?");
                        ch.inviter = nickname;
                    }
                    else { out.println("SERVER: Player already in another game"); }
                }
                else { out.println("SERVER: There is no such player."); }
            }
            else if(message.startsWith("/playersOnline")) {
                out.println("Active players:");
                for(ConnectionHandler ch : connections) {
                    out.println(ch.nickname + (ch.game == null ? "" : " inGame"));
                }
            }
            else if(message.startsWith("/Confirm")) {
                if(inviter != null) {
                    ConnectionHandler ch = findUser(inviter);
                    game = new Game(this, ch);
                    ch.game = game;
                    System.out.println("SERVER: New game: " + game.players[0].nickname + " " + game.players[1].nickname);
                    out.println("SERVER: In game with: " + game.players[1].nickname);
                    ch.sendMessage("SERVER: In game with: " + game.players[0].nickname);
                    games.add(game); //na koniec if-a
                }
            }
            else if(message.startsWith("/Reject")) {
                if(inviter != null) {
                    ConnectionHandler ch = findUser(inviter);
                    ch.sendMessage("Invitation rejected");
                    inviter = null;
                }
            }
        }
        public void inGame(String message) {
            if(message.startsWith("M")) {
                if (game.players[0] == this) {
                    game.players[1].sendMessage(nickname + ": " + message.substring(1));
                } else {
                    game.players[0].sendMessage(nickname + ": " + message.substring(1));
                }
            }
            else if (message.startsWith("X")) {
                System.out.println("SERVER: (" + nickname + ") Move: " + message.substring(1));
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
        public void shutdown() {
            try {
                in.close();
                out.close();
                if(!client.isClosed()) {
                    client.close();
                }
                connections.remove(this);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    class Game {
        ConnectionHandler[] players = new ConnectionHandler[2];
        Game(ConnectionHandler ch1, ConnectionHandler ch2) {
            players[0] = ch1;
            players[1] = ch2;
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
