import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    private final ArrayList<Game> games = new ArrayList<>();

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
                ch.systemMessage(message);
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
                systemMessage("Please enter your nickname: ");
                String supposedName = in.readLine();
                while(findUser(supposedName) != null) {
                    systemMessage("Please choose another nickname: ");
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
                playWith(extractName(message));
            }
            else if(message.startsWith("/playersOnline")) {
                playersOnline();
            }
            else if(message.startsWith("/confirm")) {
                confirm();
            }
            else if(message.startsWith("/reject")) {
                reject();
            }
        }
        public void inGame(String message) {
            if(message.startsWith("M")) {
                messageOpponent(message);
            }
            else if (message.startsWith("X")) {
                makeMove(message);
            }
            else if(message.startsWith("E")) {
                endGame(1);
            }
        }

        /**
         * Konczy gre. Jesli 1 to gracz 1, 2 to gracz 2, 0 to remis.
         * @param winner kto wygral
         */
        public void endGame(int winner) {
            Game tmp = new Game(game.players[0], game.players[1]);
            if(winner == 1) {
                tmp.players[0].systemMessage("You won");
                tmp.players[1].systemMessage("You lost");
            }
            else if(winner == 2) {
                tmp.players[0].systemMessage("You lost");
                tmp.players[1].systemMessage("You won");
            }
            else if(winner == 0) {
                tmp.players[0].systemMessage("Draw");
                tmp.players[1].systemMessage("Draw");
            }
            tmp.players[0].systemMessage("SERVER: endOfGame");
            tmp.players[0].game = null;
            tmp.players[1].systemMessage("SERVER: endOfGame");
            tmp.players[1].game = null;
            Server.this.games.remove(this);
        }

        public void sendToOpponent(String message) {
            if (game.players[0] == this) {
                game.players[1].sendMessage(message);
            } else {
                game.players[0].sendMessage(message);
            }
        }
        public void messageOpponent(String message) {
            sendToOpponent("M" + nickname + ": " + message.substring(1));
        }
        public void makeMove(String move) {
            System.out.println("SERVER: (" + nickname + ") Move: " + move);
            sendToOpponent(move);
        }

        public String extractName(String message) {
            String[] messageSplit = message.split(" ", 2);
            if(messageSplit.length >= 2) {
                return messageSplit[1];
            }
            return null;
        }
        public void playWith(String nick) {
            if(nick != null && findUser(nick) != null) {
                ConnectionHandler ch = findUser(nick);
                if(Objects.equals(ch.nickname, nickname)) {
                    systemMessage("Wanna play with yourself ?!");
                    return;
                }
                if(ch.game == null) {
                    ch.systemMessage("Wanna play with: " + nickname + "?");
                    ch.inviter = nickname;
                }
                else { systemMessage("SERVER: Player already in another game"); }
            }
            else { systemMessage("SERVER: There is no such player."); }
        }
        public void playersOnline() {
            systemMessage("Active players:");
            for(ConnectionHandler ch : connections) {
                systemMessage(ch.nickname + (ch.game == null ? "" : " inGame"));
            }
            systemMessage("/end");
        }
        public void confirm() {
            if(inviter != null) {
                ConnectionHandler ch = findUser(inviter);
                game = new Game(this, ch);
                ch.game = game;
                System.out.println("SERVER: New game: " + game.players[0].nickname + " " + game.players[1].nickname);
                systemMessage("SERVER: In game with: " + game.players[1].nickname);
                ch.systemMessage("SERVER: In game with: " + game.players[0].nickname);
                games.add(game); //na koniec if-a
            }
        }
        public void reject() {
            if(inviter != null) {
                ConnectionHandler ch = findUser(inviter);
                ch.systemMessage("Invitation rejected");
                inviter = null;
            }
        }


        public void sendMessage(String message) {
            out.println(message);
        }
        public void systemMessage(String message) {
            sendMessage("S" + message);
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
