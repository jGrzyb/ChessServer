import java.net.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Łącznik z serverem. Stworz obiekt Client i wykonaj dla niego metode {@link #run() run}.
 */
public class Client  implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    public volatile String input;
    private  boolean done = false;

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

    class InputHandler implements Runnable {
        @Override
        public void run() {
            while (!done) {
                while (input == null) {
                    Thread.onSpinWait();
                }
                String message = input;
                input = null;
                if(message.equals("/quit")) {
                    out.println(message);
                    shutdown();
                    break;
                }
                else {
                    out.println(message);
                }
            }
        }
    }

    class TestInput implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    String tmp = inReader.readLine();
                    if(tmp.equals("/quit")) {
                        inReader.close();
                        input = tmp;
                        break;
                    }
                    input = tmp;
                }

            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
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

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String inMessage;
            while((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }
        } catch (IOException e) {
            // unlucky
        }
    }

    /**
     * Pyta gracza o nazwie nick czy chce grac.
     * @param nick nazwa gracza
     */
    public void playWith(String nick) {
        input = "/playWith " + nick;
    }

    /**
     * Pyta o wszyskich graczy
     * @return tablice graczy (jesli w grze, z dopiskiem inGame)
     * @throws IOException wyjatek
     */
    public ArrayList<String> playersOnline() throws IOException {
        input = "/playersOnline";
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
        input = "/confirm";
    }

    /**
     * Odrzuca zaproszenie, jesli zaproszony. Nie wykonuje akcji w innym wypadku.
     */
    public void reject() {
        input = "/reject";
    }

    /**
     * Wysyła oponentowi wiadomosc. Dziala tylko podczas gry.
     * @param message wiadomosc
     */
    public void messageOpponent(String message) {
        input = "M" + message;
    }

    /**
     * Wysyla ruch gracza.
     * @param move ruch
     */
    public void makeMove(String move) {
        input = "X" + move;
    }
}
