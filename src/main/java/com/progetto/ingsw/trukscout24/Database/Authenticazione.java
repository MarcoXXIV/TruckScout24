package com.progetto.ingsw.trukscout24.Database;

import com.progetto.ingsw.trukscout24.Model.Utente;

public class Authenticazione {
    private static Authenticazione instance = null;

    private Utente utente = null;

    private Authenticazione() {}

    public static Authenticazione getInstance(){
        if (instance == null){
            instance = new Authenticazione();
        }
        return instance;
    }

    public boolean isAdmin(){
        System.out.println("isAdmin " + this.utente.isAdmin());
        return this.utente.isAdmin();
    }

    public void login(Utente user){
        this.utente = user;
    }

    public boolean settedUser(){
        System.out.println("settedUser " + !(this.utente == null));
        return !(this.utente == null);
    }

    public void logout(){
        this.utente = null;
    }

    public Utente getUser() {
        if (utente != null){
            return utente;
        }else{
            return null;
        }
    }
}

