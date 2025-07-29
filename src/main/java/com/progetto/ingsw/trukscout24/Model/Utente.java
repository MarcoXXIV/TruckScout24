package com.progetto.ingsw.trukscout24.Model;

public record Utente(String email, String nome, String cognome, Long numero_di_telefono, Boolean isAdmin) {
    @Override
    public String toString() {return email + ";" + nome + ";" + cognome + ";" + numero_di_telefono + ";" + isAdmin;}
}
