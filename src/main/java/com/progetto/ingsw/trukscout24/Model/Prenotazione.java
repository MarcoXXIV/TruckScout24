package com.progetto.ingsw.trukscout24.Model;

public record Prenotazione(String id_utente, String nome_camion, int giorno, int mese, int anno) {
    @Override
    public String toString() {return id_utente + ";" + nome_camion + ";" + giorno + ";" + mese + ";" + anno;}
}
