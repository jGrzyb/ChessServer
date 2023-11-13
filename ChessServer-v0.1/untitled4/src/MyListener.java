/**
 * Klasy implementujące interfejs MyListener pozwalają na utworzenie obiektu klasy {@link Client}.
 */
interface MyListener {
    /**
     * Funkcja performed wykonuje się jeśli obiekt ją wykonujący otrzymuje wiadomość z serwera.
     * @param message wiadomość
     * @param type typ wiadomości
     */
    public void performed(String message, MessType type);
}