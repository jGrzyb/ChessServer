import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Test implements MyListener {
    Client client;
    public void run() {
        Client client = new Client(this);
        client.run();
    }

    public static void main(String[] str) {
        Test t = new Test();
        t.run();
    }

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
