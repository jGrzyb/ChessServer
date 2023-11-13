package chess_server_package;

/**
 * Enum chess_server_package.MessType zawiera wszystkie rodzaje wiadomości jakie może otrzymać użytkownik z serwera.
 */
public enum MessType {
    /**
     * wiadomość systemowa
     */
    SYSTEM_MESSAGE,
    /**
     * wiadomość od przeciwnika
     */
    OPPONENT_MESSAGE,
    /**
     * przeciwnik wykonał ruch
     */
    MOVE,
    /**
     * przeciwnik potwierdził zaproszenie do gry
     */
    CONFIRM,
    /**
     * przeciwnik odrzucił zaproszenie do gry
     */
    REJECT
}
