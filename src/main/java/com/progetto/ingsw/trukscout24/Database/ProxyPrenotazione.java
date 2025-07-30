package com.progetto.ingsw.trukscout24.Database;

import com.progetto.ingsw.trukscout24.Model.Prenotazione;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class ProxyPrenotazione {
    private static ProxyPrenotazione instance = null;
    public static ProxyPrenotazione getInstance(){
        if (instance == null){
            instance = new ProxyPrenotazione();
        }
        return instance;
    }

    public CompletableFuture<ArrayList<Prenotazione>> getPrenotazioni() {
        if (Authenticazione.getInstance().isAdmin()) {
            return DBConnessione.getInstance().getPrenotazioniAdmin();
        }
        else{
            return DBConnessione.getInstance().getPrenotazione(Authenticazione.getInstance().getUser().email());
        }
    }

}

