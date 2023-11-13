package chess_server_package;

/**
 * Przykładowa klasa implementująca {@link MyListener}
 */
public class Test implements MyListener {
    Client client;

    /**
     * Konstruktor chess_server_package.Test.
     */
    public Test() {}

    /**
     * Pomocnicza funkcja uruchamiająca klienta. 
     */
    public void run() {
        Client client = new Client(this);
        client.run();
    }

    /**
     * Main.
     * @param str .
     */
    public static void main(String[] str) {
        Test t = new Test();
        t.run();
    }

    /**
     * Implementuje funkcję {@link MyListener#performed(String, MessType)} z interface-u {@link MyListener}
     * @param message wiadomość
     * @param type typ wiadomości
     */
    @Override
    public void performed(String message, MessType type) {
        switch (type) {
            case SYSTEM_MESSAGE -> System.out.println("SYSTEM_MESSAGE: " + message);
            case OPPONENT_MESSAGE -> System.out.println("OPPONENT_MESSAGE: " + message);
            case MOVE -> System.out.println("MOVE: " + message);
            case CONFIRM -> System.out.println("CONFIRMED " + message);
            case REJECT -> System.out.println("REJECTED " + message);
            case null, default -> System.out.println("!unhandled! " + message);
        }
    }
}
