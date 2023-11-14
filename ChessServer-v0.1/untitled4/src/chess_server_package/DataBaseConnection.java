package chess_server_package;

import java.util.ArrayList;

/**
 * Klasa DataBaseConnection obsługuje połączenie z bazą danych oraz pobieranie i konwertowanie z niej wartości.
 */
public class DataBaseConnection {
    /**
     * Tworzy obiekt DataBaseConnection.
     */
    public DataBaseConnection() {}

    /**
     * Łączy z bazą danych.
     */
    public void connectToDataBase() {
        return;
        //TODO DBConnection
    }
    /**
     * Pobiera z bazy danych informacje o partiach gracza i zapisuje je w wygodnej postaci.
     * @param name nazwa użytkownika którego informacje nas interesują
     * @return lista list z grami
     */
    public ArrayList<ArrayList<String>> getPlayerGamesMessage(String name) {
        return null;
        //TODO getPlayerGamesMessage
    }
    /**
     * Pobiera z bazy danych informacje o statystykach wszystkich graczy i zapisuje je w wygodnej postaci.
     * @return lista list z statystykami graczy
     */
    public ArrayList<ArrayList<String>> getAllPlayersStatistics() {
        return null;
        //TODO getAllPlayersStatistics
    }
    /**
     * Pobiera z bazy danych informacje o statystykach gracza o nazwie name i zapisuje je w wygodnej postaci.
     * @param name nazwa użytkownika
     * @return statystyki gracza (lista)
     */
    public ArrayList<String> getPlayerStatistics(String name) {
        return null;
        //TODO getPlayerStatistics
    }
}
