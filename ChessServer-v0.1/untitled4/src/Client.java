import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

// TODO:
//uncomment message receivers,
// uncomment main,
// ustawienie początkowe szachów

/**
 * Klasa Client obsługuje połączenie z serverem, wysyłanie i otrzymywanie wiadomości
 * Zainicjuj obiekt "Client(MESSAGE_RECEIVER)" oraz wykonaj metode {@link #run()}.
 * "MESSAGE_RECEIVER" to twoj obiekt odbierajacy wiadomosci. Musi implementować {@link MyListener}
 * i zawierac {@link MyListener#performed(String, MessType)} ktory przujmuje wiadomosc i jej typ.
 * Zobacz rowniez: {@link MessType}.
 * Przykladowe uzycie: {@link Test}
 */
public class Client implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private  boolean done = false;
    private String board = " "; //-----------------------------------------ustawienie poczatkowe szachow
    private final MyListener listener;

    /**
     * Tworzy objekt typu {@link Client}
     * @param obj obiekt nasłuchujący wiadomości z serwera.
     */
    public Client(MyListener obj) {
        listener = obj;
    }

    private void shutdown() {
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

    /**
     * Klasa TestInput obsługuje wejście z konsoli. Służy do testowania programu z poziomu konsoli. Nie należy używać w końcowym programie.
     */
    class TestInput implements Runnable {
        /**
         * Implementuje metodę run() z {@link Runnable}
         */
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

//    public static void main(String[] args) {
//        Client client = new Client();
//        client.run();
//    }


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
    private void send(String message) {
        if(message.equals("/quit")) {
            out.println(message);
            shutdown();
        }
        else {
            out.println(message);
        }
    }

    /**
     * Łączy z serwerem oraz tworzy wątki do obsługi konsoli. Przyjmuje dane z serwera. Implementuje metodę run z Runnable.
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

    /**
     * Kończy połączenie z serwerem.
     */
    public void quit() {
        send("/quit");
        shutdown();
    }

    private void confirmed(String message) {
        listener.performed(message, MessType.CONFIRM);
    }
    private void rejected(String message) {
        listener.performed(message, MessType.REJECT);
    }

    /**
     * Przyjmuje wiadomosc systemowa
     * @param message wiadomosc
     */
    private void receiveSystemMessage(String message) {
//        System.out.println("System: " + message);
        listener.performed(message, MessType.SYSTEM_MESSAGE);
    }

    /**
     * przyjmuje wiadomosc od przeciwnika. Tylko podczas gry (mam nadzieje)
     * @param message wiadomosc
     */
    private void receiveOpponentsMessage(String message) {
//        System.out.println("Message: " + message);
        listener.performed(message, MessType.OPPONENT_MESSAGE);
    }

    /**
     * przyjmuje ruch
     * @param move ruch
     */
    private void receiveMove(String move) {
        board = move;
//        System.out.println("Move: " + board);
        listener.performed(board, MessType.MOVE);

    }

    /**
     * Wysyła zapytanie przez serwer do gracza o nazwie nick czy chce grać.
     * @param nick nazwa gracza
     */
    public void playWith(String nick) {
        send("/playWith " + nick);
    }

    /**
     * Wysyła zapytanie do serwera o listę aktywnych graczy
     * @return tablica graczy (jesli w grze, z dopiskiem inGame)
     * @throws IOException wyjątek
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
     * Jesli gracz zostal zaproszony, informuje serwer, że przyjmuje zaproszenie tym samym dolaczajac siebie i drugiego gracza do gry. Nie wykonuj akcji w innym wypadku.
     */
    public void confirm() {
        send("/confirm");
    }

    /**
     * Informuje serwer, że odrzuca zaproszenie, jesli zaproszony. Nie wykonuje akcji w innym wypadku.
     */
    public void reject() {
        send("/reject");
    }

    /**
     * Wysyła oponentowi wiadomosc. Dziala tylko podczas gry.
     * @param message wiadomość
     */
    public void messageOpponent(String message) {
        send("M" + message);
    }

    /**
     * Wysyla ruch gracza do przeciwnika.
     * @param move String zawierająy planszę.
     */
    public void makeMove(String move) {
        board = move;
        send("X" + board);
    }
}
