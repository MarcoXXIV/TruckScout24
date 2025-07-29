package com.progetto.ingsw.trukscout24.Model;

public record Prenotazione(String id_utente, String id_Camion, int giorno, int mese, int anno) {
    @Override
    public String toString() {return id_utente + ";" + id_Camion + ";" + giorno + ";" + mese + ";" + anno;}
}
