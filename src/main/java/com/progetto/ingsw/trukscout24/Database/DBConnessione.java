package com.progetto.ingsw.trukscout24.Database;

import java.sql.*;
import com.progetto.ingsw.trukscout24.Messaggi;
import com.progetto.ingsw.trukscout24.Model.Camion;
import com.progetto.ingsw.trukscout24.Model.Prenotazione;
import com.progetto.ingsw.trukscout24.Model.Utente;
import com.progetto.ingsw.trukscout24.View.SceneHandler;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import java.sql.DriverManager;
import java.sql.Connection;
import org.springframework.security.crypto.bcrypt.BCrypt;
import java.time.LocalDate;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.*;

public class DBConnessione {
    private static DBConnessione instance = null;
    private Connection con = null;
    private ArrayList<Camion> categoryCamion;
    private ArrayList<Camion> similarCamion;
    private final ArrayList<Camion> searchedCamion = new ArrayList<>();
    private Label resultLabel;
    private Camion camion;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private DBConnessione() {}

    public static DBConnessione getInstance() {
        if (instance == null) {
            instance = new DBConnessione();
        }
        return instance;
    }

    public void createConnection() throws SQLException {
        String url = "jdbc:sqlite:Database24.db";
        con = DriverManager.getConnection(url);
        if (con != null && !con.isClosed())
            System.out.println("Connesso!!!");
    }

    public void closeConnection() throws SQLException, IOException {
        if (con != null)
            con.close();
        con = null;
    }

    public void close() {
        executorService.shutdownNow();
    }

    public void insertUsers(String nome, String cognome, String email, Long numero_di_telefono, String password, Boolean isAdmin) {
        executorService.submit(createDaemonThread(() -> {
            try {
                if (con == null || con.isClosed())
                    return;
                PreparedStatement stmt = con.prepareStatement("INSERT INTO utenti VALUES(?, ?, ?, ?, ?, ?);");
                stmt.setString(1, email);
                stmt.setString(2, nome);
                stmt.setString(3, cognome);
                stmt.setLong(4, numero_di_telefono);
                stmt.setString(5, password);
                stmt.setBoolean(6, isAdmin);
                stmt.execute();
                stmt.close();
            } catch (SQLException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
    }

    public Future<?> checkLogin(String email, String password) throws SQLException {
        return executorService.submit(createDaemonThread(() -> {
            try {
                if (con == null || con.isClosed())
                    return;
                String query = "select * from utenti where email=?";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    boolean check = BCrypt.checkpw(password, rs.getString("password"));
                    if (check)
                        System.out.println("Password OK");
                    else
                        throw new SQLException();
                } else {
                    throw new SQLException();
                }
                stmt.close();
            } catch (SQLException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
    }

    public CompletableFuture<Utente> setUser(String email) {
        CompletableFuture<Utente> future = new CompletableFuture<>();
        executorService.submit(createDaemonThread(() -> {
            try {
                Utente utente;
                if (con == null || con.isClosed())
                    future.complete(null);
                String query = "select * from utenti where email=?;";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    utente = new Utente(rs.getString("email"), rs.getString("nome"), rs.getString("cognome"),
                            rs.getLong("numero_di_telefono"), rs.getBoolean("isAdmin"));
                    future.complete(utente);
                }
                stmt.close();
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        }));
        return future;
    }

    public String encryptedPassword(String password) {
        String generatedSecuredPasswordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        return generatedSecuredPasswordHash;
    }

    public CompletableFuture<ArrayList<Camion>> addHomePageCamion() {
        CompletableFuture<ArrayList<Camion>> future = new CompletableFuture<>();
        executorService.submit(createDaemonThread(() -> {
            try {
                ArrayList<Camion> camion = new ArrayList<>();
                if (this.con != null && !this.con.isClosed()) {
                    String query = "select * from camion;";
                    PreparedStatement stmt = this.con.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        Camion c = new Camion(rs.getString("id"), rs.getString("nome"), rs.getString("modello"),
                                rs.getInt("potenza"), rs.getDouble("kilometri"), rs.getString("carburante"),
                                rs.getString("cambio"), rs.getInt("classeEmissioni"), rs.getString("anno"),
                                rs.getDouble("prezzo"), rs.getString("descrizione"), rs.getString("categoria"));
                        camion.add(c);
                    }
                    future.complete(camion);
                    stmt.close();
                }
            } catch (SQLException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
        return future;
    }

    public void addCategoryCamion(String category) {
        executorService.submit(createDaemonThread(() -> {
            try {
                categoryCamion = new ArrayList<>();
                if (this.con != null && !this.con.isClosed()) {
                    String query = "select * from camion where categoria=?;";
                    PreparedStatement stmt = this.con.prepareStatement(query);
                    stmt.setString(1, category);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        Camion c = new Camion(rs.getString("id"), rs.getString("nome"), rs.getString("modello"),
                                rs.getInt("potenza"), rs.getDouble("kilometri"), rs.getString("carburante"),
                                rs.getString("cambio"), rs.getInt("classeEmissioni"), rs.getString("anno"),
                                rs.getDouble("prezzo"), rs.getString("descrizione"), rs.getString("categoria"));
                        categoryCamion.add(c);
                    }
                }
            } catch (SQLException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
    }

    public ArrayList<Camion> getCategoryCamion() {
        return categoryCamion;
    }

    public void clearCategoryCamionList() {
        if (categoryCamion != null) {
            categoryCamion.clear();
        }
    }

    public CompletableFuture<Boolean> checkExistEmail(String email) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        executorService.submit(createDaemonThread(() -> {
            try {
                if (this.con != null && !this.con.isClosed()) {
                    String query = "SELECT * from utenti WHERE email =?;";
                    PreparedStatement stmt = this.con.prepareStatement(query);
                    stmt.setString(1, email);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        future.complete(true);
                    } else {
                        future.complete(false);
                    }
                }
            } catch (SQLException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
        return future;
    }

    public CompletableFuture<Camion> getCamion(String id) {
        CompletableFuture<Camion> future = new CompletableFuture<>();
        executorService.submit(createDaemonThread(() -> {
            try {
                if (this.con != null && !this.con.isClosed()) {
                    String query = "select * from camion where id=?;";
                    PreparedStatement stmt = this.con.prepareStatement(query);
                    stmt.setString(1, id);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        camion = new Camion(rs.getString("id"), rs.getString("nome"), rs.getString("modello"),
                                rs.getInt("potenza"), rs.getDouble("kilometri"), rs.getString("carburante"),
                                rs.getString("cambio"), rs.getInt("classeEmissioni"), rs.getString("anno"),
                                rs.getDouble("prezzo"), rs.getString("descrizione"), rs.getString("categoria"));
                        future.complete(camion);
                    }
                }
            } catch (SQLException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
        return future;
    }

    public void addSimilarCamion(String id, String category) {
        executorService.submit(createDaemonThread(() -> {
            try {
                similarCamion = new ArrayList<>();
                if (this.con != null && !this.con.isClosed()) {
                    String query = "select * from camion where categoria=?;";
                    PreparedStatement stmt = this.con.prepareStatement(query);
                    stmt.setString(1, category);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next() && similarCamion.size() < 5) {
                        if (!rs.getString("id").equals(id)) {
                            camion = new Camion(rs.getString("id"), rs.getString("nome"), rs.getString("modello"),
                                    rs.getInt("potenza"), rs.getDouble("kilometri"), rs.getString("carburante"),
                                    rs.getString("cambio"), rs.getInt("classeEmissioni"), rs.getString("anno"),
                                    rs.getDouble("prezzo"), rs.getString("descrizione"), rs.getString("categoria"));
                            similarCamion.add(camion);
                        }
                    }
                }
            } catch (SQLException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
    }

    public ArrayList<Camion> getSimilarCamion() {
        return similarCamion;
    }

    public CompletableFuture<Label> searchCamion(String searchText) {
        CompletableFuture<Label> future = new CompletableFuture<>();
        executorService.submit(createDaemonThread(() -> {
            try {
                resultLabel = new Label();
                if (this.con != null && !this.con.isClosed()) {
                    String query = "SELECT * FROM camion WHERE chiavi LIKE ?;";
                    PreparedStatement stmt = con.prepareStatement(query);
                    stmt.setString(1, "%" + searchText + "%");

                    ResultSet resultSet = stmt.executeQuery();
                    StringBuilder resultBuilder = new StringBuilder();
                    while (resultSet.next()) {
                        String columnValue = resultSet.getString("id");
                        resultBuilder.append(columnValue + ";");
                    }

                    resultLabel.setText(resultBuilder.toString());
                    future.complete(resultLabel);
                }
            } catch (SQLException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
        return future;
    }

    public void addSearchedCamion(String[] products) {
        executorService.submit(createDaemonThread(() -> {
            try {
                if (this.con != null && !this.con.isClosed()) {
                    for (String id : products) {
                        String query = "select * from camion where id= ?;";
                        PreparedStatement stmt = this.con.prepareStatement(query);
                        stmt.setString(1, id);

                        ResultSet rs = stmt.executeQuery();

                        while (rs.next()) {
                            Camion c = new Camion(rs.getString("id"), rs.getString("nome"), rs.getString("modello"),
                                    rs.getInt("potenza"), rs.getDouble("kilometri"), rs.getString("carburante"),
                                    rs.getString("cambio"), rs.getInt("classeEmissioni"), rs.getString("anno"),
                                    rs.getDouble("prezzo"), rs.getString("descrizione"), rs.getString("categoria"));
                            searchedCamion.add(c);
                        }
                    }
                }
            } catch (SQLException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
    }

    public ArrayList<Camion> getSearchedCamion() {
        return searchedCamion;
    }

    public void clearSearchedList() {
        if (searchedCamion != null) {
            searchedCamion.clear();
        }
    }

    public CompletableFuture<ArrayList<Camion>> getWishlist(String email) {
        CompletableFuture<ArrayList<Camion>> future = new CompletableFuture<>();
        executorService.submit(createDaemonThread(() -> {
            try {
                ArrayList<Camion> cam = new ArrayList<>();
                if (con == null || con.isClosed())
                    future.complete(cam);
                String query = "SELECT * from wishlist where id_utente=?";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    CompletableFuture<Camion> future1 = getCamion(rs.getString(2));
                    Camion c = future1.get(10, TimeUnit.SECONDS);
                    cam.add(c);
                }
                future.complete(cam);
            } catch (SQLException | ExecutionException | InterruptedException | TimeoutException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
        return future;
    }

    public boolean insertWishlistCamionIntoDB(String email, String id_camion) {
        try {
            CompletableFuture<ArrayList<Camion>> future = getWishlist(email);
            ArrayList<Camion> nCam = future.get(10, TimeUnit.SECONDS);

            boolean find = false;
            for (Camion id : nCam) {
                if (id.id().equals(id_camion)) {
                    find = true;
                }
            }

            if (nCam.size() < 6 && !find) {
                if (con == null || con.isClosed())
                    return false;

                PreparedStatement stmt = con.prepareStatement("INSERT INTO wishlist VALUES(?, ?);");
                stmt.setString(1, email);
                stmt.setString(2, id_camion);
                stmt.execute();
                stmt.close();
                return true;
            }

            if (nCam.size() >= 6 || find) {
                return false;
            }
        } catch (SQLException | ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public CompletableFuture<ArrayList<Prenotazione>> getPrenotazione(String email) {
        CompletableFuture<ArrayList<Prenotazione>> future = new CompletableFuture<>();
        executorService.submit(createDaemonThread(() -> {
            try {
                ArrayList<Prenotazione> pre = new ArrayList<>();
                if (con == null || con.isClosed())
                    future.complete(pre);
                String query = "SELECT * from prenotazioni where id_utente=?";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Prenotazione p = new Prenotazione(rs.getString("id_utente"), rs.getString("id_Camion"),
                            rs.getInt("giorno"), rs.getInt("mese"), rs.getInt("anno"));
                    pre.add(p);
                }
                future.complete(pre);
            } catch (SQLException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
        return future;
    }

    public CompletableFuture<ArrayList<Prenotazione>> getPrenotazioniAdmin() {
        CompletableFuture<ArrayList<Prenotazione>> future = new CompletableFuture<>();
        executorService.submit(createDaemonThread(() -> {
            try {
                ArrayList<Prenotazione> pre = new ArrayList<>();
                if (con == null || con.isClosed())
                    future.complete(pre);
                String query = "SELECT * from prenotazioni ORDER BY anno, mese, giorno;";
                PreparedStatement stmt = con.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Prenotazione p = new Prenotazione(rs.getString("id_utente"), rs.getString("id_Camion"),
                            rs.getInt("giorno"), rs.getInt("mese"), rs.getInt("anno"));
                    pre.add(p);
                }
                future.complete(pre);
            } catch (SQLException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
        return future;
    }

    public String getCamionName(String id) {
        try {
            CompletableFuture<Camion> future = getCamion(id);
            Camion c = future.get(10, TimeUnit.SECONDS);
            return c.nome();
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
        }
        return null;
    }

    public void insertPrenotazioneIntoDB(String email, String id_camion, int giorno, int mese, int anno) {
        executorService.submit(createDaemonThread(() -> {
            try {
                CompletableFuture<ArrayList<Prenotazione>> future = getPrenotazione(email);
                ArrayList<Prenotazione> nPre = future.get(10, TimeUnit.SECONDS);
                boolean find = false;
                for (Prenotazione id : nPre) {
                    if (id.id_Camion().equals(id_camion)) {
                        find = true;
                        break;
                    }
                }

                if (find) {
                    Platform.runLater(() -> SceneHandler.getInstance().showAlert("Attenzione", Messaggi.add_prenotazioni_find_information, 1));
                    return;
                }

                if (nPre.size() < 6) {
                    if (con == null || con.isClosed())
                        return;
                    PreparedStatement stmt = con.prepareStatement("INSERT INTO prenotazioni VALUES(?, ?, ?, ?, ?);");
                    stmt.setString(1, email);
                    stmt.setString(2, id_camion);
                    stmt.setInt(3, giorno);
                    stmt.setInt(4, mese);
                    stmt.setInt(5, anno);
                    stmt.execute();
                    stmt.close();

                    Platform.runLater(() -> SceneHandler.getInstance().showAlert("Conferma", Messaggi.conferma_prenotazione + LocalDate.of(anno, mese, giorno), 1));
                } else {
                    Platform.runLater(() -> SceneHandler.getInstance().showAlert("Attenzione", Messaggi.add_prenotazioni_max_information, 1));
                }
            } catch (SQLException | ExecutionException | InterruptedException | TimeoutException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
    }

    public void removeSelectedPrenotazioniItem(String id, String id_utente) {
        executorService.submit(createDaemonThread(() -> {
            try {
                if (con == null || con.isClosed())
                    return;
                String query = "DELETE FROM prenotazioni where id_Camion = ? and id_utente = ?;";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, id);
                stmt.setString(2, id_utente);
                stmt.execute();
                stmt.close();
            } catch (SQLException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
    }

    public void removeSelectedWishlistItem(String id, String email) {
        executorService.submit(createDaemonThread(() -> {
            try {
                if (con == null || con.isClosed())
                    return;
                String query = "DELETE FROM wishlist where id_utente=? and id_camion = ?;";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, email);
                stmt.setString(2, id);
                stmt.execute();
                stmt.close();
            } catch (SQLException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
    }

    public void updatePassword(String email, String password) {
        executorService.submit(createDaemonThread(() -> {
            try {
                if (this.con != null && !this.con.isClosed()) {
                    String query = "update utenti set password = ? where email=?;";
                    PreparedStatement stmt = this.con.prepareStatement(query);
                    stmt.setString(1, password);
                    stmt.setString(2, email);
                    stmt.execute();
                    stmt.close();
                }
            } catch (SQLException e) {
                SceneHandler.getInstance().showAlert("Errore thread", Messaggi.thread_error, 0);
            }
        }));
    }

    public boolean aggiungiCamion(String idCamion, String nomeCamion, String modelloCamion, Integer potenzaCamion,
                                  Double kilometriCamion, String carburanteCamion, String cambioCamion,
                                  Integer classeEmissioniCamion, String annoCamion, Double prezzoCamion,
                                  String descrizioneCamion, String categoriaCamion, String chiaviCamion) {
        try {
            if (con == null || con.isClosed()) {
                Platform.runLater(() -> SceneHandler.getInstance().showAlert("Errore Database", "Connessione al database non disponibile.", 0));
                return false;
            }

            PreparedStatement stmt = con.prepareStatement("INSERT INTO camion (id, nome, modello, potenza, kilometri, carburante, cambio, classeEmissioni, anno, prezzo, descrizione, categoria, chiavi) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
            stmt.setString(1, idCamion);
            stmt.setString(2, nomeCamion);
            stmt.setString(3, modelloCamion);
            stmt.setInt(4, potenzaCamion);
            stmt.setDouble(5, kilometriCamion);
            stmt.setString(6, carburanteCamion);
            stmt.setString(7, cambioCamion);
            stmt.setInt(8, classeEmissioniCamion);
            stmt.setString(9, annoCamion);
            stmt.setDouble(10, prezzoCamion);
            stmt.setString(11, descrizioneCamion);
            stmt.setString(12, categoriaCamion);
            stmt.setString(13, chiaviCamion);

            stmt.execute();
            stmt.close();

            Platform.runLater(() -> SceneHandler.getInstance().showAlert("Operazione riuscita", "Camion aggiunto con successo. L'annuncio completo del camion sarà visibile solo dopo il riavvio dell'applicazione.", 1));
            return true;
        } catch (SQLException e) {
            Platform.runLater(() -> SceneHandler.getInstance().showAlert("Errore Database", "Impossibile aggiungere il camion: " + e.getMessage(), 0));
            return false;
        }
    }

    public boolean rimuoviCamion(String idCamion) {
        try {
            if (con == null || con.isClosed()) {
                Platform.runLater(() ->
                        SceneHandler.getInstance().showAlert("Errore Database", "Connessione al database non disponibile.", 0)
                );
                return false;
            }

            // Creazione dell'alert di conferma
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Conferma rimozione");
            alert.setHeaderText("Sei sicuro di voler rimuovere il camion?");
            alert.setContentText("ID Camion: " + idCamion);

            // Mostra l'alert e attendi la risposta dell'utente
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Rimuovi le prenotazioni associate al camion
                PreparedStatement deletePrenotazioniStmt = con.prepareStatement("DELETE FROM prenotazioni WHERE id_Camion = ?;");
                deletePrenotazioniStmt.setString(1, idCamion);
                int prenotazioniEliminate = deletePrenotazioniStmt.executeUpdate();
                deletePrenotazioniStmt.close();

                // Rimuovi il camion dalle wishlist
                PreparedStatement deleteWishlistStmt = con.prepareStatement("DELETE FROM wishlist WHERE id_camion = ?;");
                deleteWishlistStmt.setString(1, idCamion);
                int wishlistEliminate = deleteWishlistStmt.executeUpdate();
                deleteWishlistStmt.close();

                // Esegui la rimozione del camion
                PreparedStatement deleteCamionStmt = con.prepareStatement("DELETE FROM camion WHERE id = ?;");
                deleteCamionStmt.setString(1, idCamion);
                int rowsAffected = deleteCamionStmt.executeUpdate();
                deleteCamionStmt.close();

                if (rowsAffected > 0) {
                    Platform.runLater(() -> {
                        String message = "Camion rimosso con successo.";
                        if (prenotazioniEliminate > 0) {
                            message += " Rimosse anche " + prenotazioniEliminate + " prenotazioni associate.";
                        }
                        SceneHandler.getInstance().showAlert("Operazione riuscita", message, 1);
                    });
                    return true;
                } else {
                    Platform.runLater(() ->
                            SceneHandler.getInstance().showAlert("Errore", "Nessun camion trovato con l'ID fornito.", 0)
                    );
                    return false;
                }
            } else {
                // L'utente ha annullato l'operazione
                Platform.runLater(() ->
                        SceneHandler.getInstance().showAlert("Operazione annullata", "La rimozione del camion è stata annullata.", 0)
                );
                return false;
            }
        } catch (SQLException e) {
            Platform.runLater(() ->
                    SceneHandler.getInstance().showAlert("Errore Database", "Impossibile rimuovere il camion: " + e.getMessage(), 0)
            );
            return false;
        }
    }

    private Thread createDaemonThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t;
    }
}

