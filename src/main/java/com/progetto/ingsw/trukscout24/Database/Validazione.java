package com.progetto.ingsw.trukscout24.Database;

import java.util.regex.Pattern;

public class Validazione {
    private static Validazione instance = null;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9]{9,12}$");

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-zÀ-ÿ\\s'.-]{3,}$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{6,}$");

    private Validazione() {}

    public static Validazione getInstance(){
        if (instance == null){
            instance = new Validazione();
        }
        return instance;
    }

    public boolean isValidName(String name) {
        return name != null && name.length() >= 3 && NAME_PATTERN.matcher(name).matches();
    }

    public boolean isValidSurname(String surname) {
        return surname != null && surname.length() >= 3 && NAME_PATTERN.matcher(surname).matches();
    }

    public boolean isValidEmailFormat(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidPhoneNumber(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

}
