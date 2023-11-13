import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

//uncomment message receivers, main

interface MyListener {
    public void performed(String message, MessType type);
}

/**
 * Łącznik z serverem. <p></p>
 * Zainicjuj obiekt "Client(MESSAGE_RECEIVER)" oraz wykonaj metode {@link #run()}. <p></p>
 * "MESSAGE_RECEIVER" to twoj obiekt odbierajacy wiadomosci. Musi implementować {@link MyListener}
 * i zawierac {@link MyListener#performed(String, MessType)} ktory przujmuje wiadomosc i jej typ. <p></p>
 * Zobacz rowniez: {@link MessType}.<p></p>
 * Przykladowe uzycie: {@link Test}
 */
public class Client implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
//    public volatile String input;
    private  boolean done = false;
    private String board = " "; //-----------------------------------------ustawienie poczatkowe szachow
    private final MyListener listener;
    public Client(MyListener obj) {
        listener = obj;
    }

    public void shutdown() {
        done = true;
        try {
            in.close();
            out.close();
            if(!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            //ignore
        }
    }

//    class InputHandler implements Runnable {
//        @Override
//        public void run() {
//            while (!done) {
//                while (input == null) {
//                    Thread.onSpinWait();
//                }
//                String message = input;
//                input = null;
//                if(message.equals("/quit")) {
//                    out.println(message);
//                    shutdown();
//                    break;
//                }
//                else {
//                    out.println(message);
//                }
//            }
//        }
//    }

    class TestInput implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    String tmp = inReader.readLine();
                    if(tmp.equals("/quit")) {
                        inReader.close();
                        send(tmp);
                        break;
                    }
                    send(tmp);
                }

            } catch (IOException e) {
                shutdown();
            }
        }
    }


    public static void main(String[] args) {
//        Client client = new Client();
//        client.run();
    }


    /**
     * Wysyla wiadomosc na serwer. Nie uzywac jesli mozesz skorzystac z: <p>
     * {@link #playWith(String)}, <p>
     * {@link #playersOnline()}, <p>
     * {@link #confirm()}, <p>
     * {@link #reject()}, <p>
     * {@link #messageOpponent(String)}, <p>
     * {@link #makeMove(String)}
     * @param message wiadomosc
     */
    public void send(String message) {
        if(message.equals("/quit")) {
            out.println(message);
            shutdown();
        }
        else {
            out.println(message);
        }
    }

    /**
     * Łączy z serwerem oraz tworzy wątki do obsługi konsoli i funkcji.
     */
    public void run() {
        try {
            client = new Socket("127.0.0.1", 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            TestInput testInput = new TestInput();
            Thread t1 = new Thread(testInput);
            t1.start();

//            InputHandler inHandler = new InputHandler();
//            Thread t = new Thread(inHandler);
//            t.start();

            String inMessage;
            while((inMessage = in.readLine()) != null) {
                if(inMessage.startsWith("S")) {
                    receiveSystemMessage(inMessage.substring(1));
                }
                else if (inMessage.startsWith("X")) {
                    receiveMove(inMessage.substring(1));
                }
                else if (inMessage.startsWith("M")) {
                    receiveOpponentsMessage(inMessage.substring(1));
                }
                else if(inMessage.startsWith("C")) {
                    confirmed(inMessage.substring(1));
                }
                else if(inMessage.startsWith("R")) {
                    rejected(inMessage.substring(1));
                }
                else {
                    System.out.println("UNHANDLED: " + inMessage);
                }

            }
        } catch (IOException e) {
            // unlucky
        }
    }
    public void quit() {
        send("/quit");
        shutdown();
    }

    public void confirmed(String message) {
        listener.performed(message, MessType.CONFIRM);
    }
    public void rejected(String message) {
        listener.performed(message, MessType.REJECT);
    }

    /**
     * Przyjmuje wiadomosc systemowa
     * @param message wiadomosc
     */

    public void receiveSystemMessage(String message) {
//        System.out.println("System: " + message);
        listener.performed(message, MessType.SYSTEM_MESSAGE);
    }

    /**
     * przyjmuje wiadomosc od przeciwnika. Tylko podczas gry (mam nadzieje)
     * @param message wiadomosc
     */
    public void receiveOpponentsMessage(String message) {
//        System.out.println("Message: " + message);
        listener.performed(message, MessType.OPPONENT_MESSAGE);
    }

    /**
     * przyjmuje ruch
     * @param move ruch
     */
    public void receiveMove(String move) {
        board = move;
//        System.out.println("Move: " + board);
        listener.performed(board, MessType.MOVE);

    }

    /**
     * Pyta gracza o nazwie nick czy chce grac.
     * @param nick nazwa gracza
     */
    public void playWith(String nick) {
        send("/playWith " + nick);
    }

    /**
     * Pyta o wszyskich graczy
     * @return tablice graczy (jesli w grze, z dopiskiem inGame)
     * @throws IOException wyjatek
     */
    public ArrayList<String> playersOnline() throws IOException {
        send("/playersOnline");
        String inMessage;
        ArrayList<String> players = new ArrayList<>();
        while((inMessage = in.readLine()) != null && !inMessage.equals("/end")) {
            players.add(inMessage);
        }
        return players;
    }

    /**
     * Jesli gracz zostal zaproszony, przyjmuje zaproszenie tym samym dolaczajac siebie i drugiego gracza do gry.
     */
    public void confirm() {
        send("/confirm");
    }

    /**
     * Odrzuca zaproszenie, jesli zaproszony. Nie wykonuje akcji w innym wypadku.
     */
    public void reject() {
        send("/reject");
    }

    /**
     * Wysyła oponentowi wiadomosc. Dziala tylko podczas gry.
     * @param message wiadomosc
     */
    public void messageOpponent(String message) {
        send("M" + message);
    }

    /**
     * Wysyla ruch gracza.
     * @param move ruch
     */
    public void makeMove(String move) {
        board = move;
        send("X" + board);
    }
}
