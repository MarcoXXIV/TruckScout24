package com.progetto.ingsw.trukscout24.Database;

import com.progetto.ingsw.trukscout24.Model.Utente;

// pattern
public class Authenticazione {
    private static volatile Authenticazione instance = null;
    private Utente utente = null;

    private Authenticazione() {}

    public static Authenticazione getInstance() {
        if (instance == null) {
            synchronized (Authenticazione.class) {
                if (instance == null) {
                    instance = new Authenticazione();
                }
            }
        }
        return instance;
    }

    public boolean isAdmin() {
        if (this.utente == null) {
            return false; // O lanciare un'eccezione se necessario
        }
        return this.utente.isAdmin();
    }

    public void login(Utente user) {
        this.utente = user;
    }

    public boolean settedUser() {
        return this.utente != null;
    }

    public void logout() {
        this.utente = null;
    }

    public Utente getUser() {
        return this.utente;
    }
}

