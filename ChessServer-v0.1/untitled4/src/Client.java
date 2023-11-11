import java.net.*;
import java.io.*;

public class Client  implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    public volatile String input;
    private  boolean done = false;
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
            // TODO: handle
        }
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
}
