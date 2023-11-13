package chess_server_package;

/**
 * Klasy implementujące interfejs chess_server_package.MyListener pozwalają na utworzenie obiektu klasy {@link Client}.
 */
public interface MyListener {
    /**
     * Funkcja performed wykonuje się jeśli obiekt ją wykonujący otrzymuje wiadomość z serwera.
     * @param message wiadomość
     * @param type typ wiadomości
     */
    public void performed(String message, MessType type);
}