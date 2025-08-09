package com.progetto.ingsw.trukscout24;

public class Messaggi {
    public final static String login_effettuato = "Accesso effettuato con successo! Benvenuto/a, ";
    public final static String returnHome_error = "Errore nel ritorno alla home";
    public final static String recovery_password_error = "Errore nel caricamento della pagina recupero password";
    public final static String load_page_error = "Errore nel caricamento della pagina";
    public final static String wishlist_error = "Errore wishlist";
    public final static String aggiunta_alla_wishlist_ok = "Il camion è stato aggiunta alla tua wishlist con successo!";
    public final static String aggiunta_alla_wishlist_no = "Non è stato possibile aggiungere il camion alla wishlist";
    public final static String rimozione_wishlist_ok = "Il camion è stato rimossa dalla wishlist con successo";
    public final static String rimozione_wishlist_no = "Errore durante la rimozione del camion dalla wishlist";
    public final static String prenotazioni_error = "Errore prenotazioni";
    public final static String rimozione_prenotazione_ok = "La prenotazione è stata rimossa con successo!";
    public final static String rimozione_prenotazione_no = "Errore durante la rimozione della prenotazione";
    public final static String box_page_error = "Errore box page";
    public final static String not_logged_in_error = "Effettuare il login prima di accedere a questa sezione!";
    public final static String privacy_information_error = "Errore nell'apertura di informazioni sulla privacy";
    public final static String general_condition_error = "Errore nell'apertura delle condizioni generali di uso e vendita";
    public final static String add_home_page_camion_error = "Errore nell'aggiunta dei camion alla home";
    public final static String errore_di_registrazione = "Dati non validi. Verifica i campi e riprova";
    public final static String load_camion_view_error = "Errore nel caricamento della pagina del camion";
    public final static String add_wishlist_max_information = "Capienza massima wishlist";
    public final static String add_prenotazioni_max_information = "Capienza massima prenotazioni";
    public final static String add_wishlist_find_information = "Il camion è già presente nella tua wishlist";
    public final static String add_prenotazioni_find_information = "Hai già una prenotazione per questo camion";
    public final static String upgrade_information = "Effettua ora l'upgrade a premium! Stiamo lavorando per inserire nuovi veicoli selezionabili nel tuo garage per un'esperienza personalizzata.";
    public final static String upgrade_information_error = "Errore nell'apertura delle informazioni sull'upgrade";
    public final static String update_password_success = "La password è stata modificata";
    public final static String recovery_password_email_message = "Abbiamo inviato un email all'indirizzo associato al tuo account, segui le istruzioni per proseguire con il reset della password";
    public final static String thread_error = "Errore nell'esecuzione del thread";
    public final static String data_error = "Seleziona giorno, mese e anno per continuare.";
    public final static String data_error2 = "La data selezionata è nel passato.";
    public final static String data_error3 = "La data selezionata non è valida.";
    public final static String conferma_prenotazione = "Prenotazione effettuata per il ";

    // MESSAGGI ADMIN
    public final static String admin_form_validation_error = "Tutti i campi sono obbligatori";
    public final static String admin_numeric_validation_error = "Verifica i campi numerici (potenza, chilometri, prezzo)";
    public final static String admin_camion_aggiunta_error = "Errore durante l'aggiunta del camion";
    public final static String admin_immagine_error = "Impossibile caricare l'immagine selezionata";
    public final static String admin_immagine_save_warning = "Camion aggiunto ma errore nel salvataggio dell'immagine";
    public final static String admin_id_camion_empty_error = "Inserisci l'ID del camion da rimuovere";
    public final static String admin_conferma_rimozione_title = "Conferma rimozione";
    public final static String admin_conferma_rimozione_header = "Sei sicuro di voler rimuovere il camion?";
    public final static String admin_operazione_annullata = "La rimozione del camion è stata annullata";
    public final static String admin_camion_rimosso_success = "Camion rimosso con successo!";
    public final static String admin_camion_non_trovato = "Camion non trovato o già rimosso";
    public final static String admin_rimozione_error = "Errore durante la rimozione del camion";
    public final static String admin_caricamento_prenotazioni_error = "Errore durante il caricamento delle prenotazioni";
    public final static String admin_home_error = "Impossibile tornare alla home";

    // MESSAGGI HOME
    public final static String home_db_connection_error = "Errore nella connessione al DB";
    public final static String home_loading_camions = "Caricamento camion...";
    public final static String home_loading_error = "Errore nel caricamento";
    public final static String home_showing_all_camions = "Visualizzando tutti i camion disponibili";
    public final static String home_product_view_error = "Impossibile aprire i dettagli del camion";
    public final static String home_wishlist_login_required = "Devi essere autenticato per usare la wishlist";
    public final static String home_camion_added_to_wishlist = "aggiunto ai preferiti!";
    public final static String home_camion_removed_from_wishlist = "rimosso dai preferiti!";
    public final static String home_wishlist_add_error = "Impossibile aggiungere ai preferiti.\n• Hai raggiunto il limite massimo (12 camion)\n• Il camion è già presente nella lista";
    public final static String home_wishlist_remove_error = "Impossibile rimuovere dai preferiti";
    public final static String home_wishlist_add_exception = "Errore durante l'aggiunta ai preferiti";
    public final static String home_wishlist_remove_exception = "Errore durante la rimozione dai preferiti";
    public final static String home_search_in_progress = "Ricerca in corso per";
    public final static String home_search_results_found = "Trovati";
    public final static String home_search_no_results = "Nessun risultato trovato per";
    public final static String home_search_error = "Errore durante la ricerca";
    public final static String home_brand_loading = "Caricamento camion";
    public final static String home_brand_showing = "Visualizzando";
    public final static String home_brand_no_results = "Nessun camion trovato per la marca";
    public final static String home_brand_loading_error = "Errore nel caricamento dei camion";
    public static final String advanced_search_no_results_return_home = "Nessun risultato trovato con i filtri applicati. Ritorno alla Home.";

    public static final String errore_utente = "Errore nel caricamento della schermata Utente, sarai reindirizzato nella schermata Home.";
    public static final String errore_wishlist = "Errore nel caricamento della schermata Wishlist sarai reindirizzato nella schermata Home.";
    public static final String errore_login = "Errore nel caricamento della schermata di Login, sarai reindirizzato nella schermata Home.";
    public static final String errore_admin = "Errore nel caricamento della schermata Admin, sarai reindirizzato nella schermata Home.";
    public static final String errore_generico = "Si è verificato un errore imprevisto, sarai reindirizzato nella schermata Home.";
    public static final String errore_recupero_password = "Si è verificato un errore nel caricamento della schermata recupero password, sarai reindirizzato nella schermata Home.";
    public static final String errore_registrazione = "Si è verificato un errore nel caricamento della schermata di registrazione, sarai reindirizzato nella schermata Home.";
    public static final String immagine_non_trovata = "Si è verificato un errore nel caricamento dell'immagine, sarai reindirizzato nella schermata Home.";

    // MESSAGGI LOGIN
    public final static String login_error = "";
    public final static String login_campi_vuoti = "Inserisci email e password";
    public final static String login_email_non_valida = "Formato email non valido";
    public final static String login_errore_credenziali = "Email o password non corrette";
    public final static String login_connessione_error = "Errore di connessione. Riprova.";
    public final static String login_user_data_error = "Errore nel caricamento dei dati utente";

    // MESSAGGI RECOVERY PASSOWRD
    public final static String recovery_password_min_length_error = "La password deve essere di almeno 8 caratteri.";
    public final static String recovery_password_email_not_found = "Email non registrata.";
    public final static String recovery_password_other_user_error = "Puoi cambiare solo la tua password.";
    public final static String recovery_password_exception = "Errore nel reset della password: ";

    // MESSAGGI PRODUCT VIEW
    public final static String productview_no_camion_selected = "Nessun camion selezionato";
    public final static String productview_login_required = "Devi essere autenticato per usare la wishlist";
    public final static String productview_no_date_selected = "Seleziona una data per la prenotazione";
    public final static String productview_invalid_date = "Seleziona una data futura (almeno da domani)";
    public final static String productview_booking_failed = "Errore durante la prenotazione: ";
    public final static String wishlist_added = "aggiunto ai preferiti!";
    public final static String wishlist_removed = "rimosso dai preferiti!";
    public final static String wishlist_limit_or_duplicate = "Impossibile aggiungere ai preferiti.\n• Hai raggiunto il limite massimo (12 camion)\n• Il camion è già presente nella lista";
    public final static String wishlist_add_failed = "Errore durante l'aggiunta ai preferiti: ";
    public final static String wishlist_remove_failed = "Impossibile rimuovere dai preferiti";
    public final static String wishlist_remove_error = "Errore durante la rimozione dai preferiti: ";

    // MESSAGGI REGISTRAZIONE
    public final static String registrazione_completata = "La tua registrazione è stata completata con successo! Puoi ora effettuare il login";
    public final static String registratione_field_empty_error = "Compilare tutti i campi per proseguire con la registrazione";
    public final static String registratione_password_error = "Le password inserite non coincidono";
    public final static String registratione_password_length_error = "La password deve contenere almeno 6 caratteri, una maiuscola, una minuscola, un numero e un carattere speciale (!@#$%^&*)";
    public final static String registratione_email_exist_error = "L'indirizzo email inserito è associato ad un altro account, effettuare il login";
    public final static String registratione_nome_error = "Il nome deve contenere almeno 3 caratteri e solo lettere";
    public final static String registrazione_cognome_error = "Il nome deve contenere almeno 3 caratteri e solo lettere";
    public final static String registrazione_telefono_error = "Il numero di telefono deve contenere tra 9 e 12 cifre";
    public final static String registrazione_email_error = "Formato email non valido (es. nome1@dominio.it)";

    // MESSAGGI UTENTE
    public final static String UTENTE_NON_AUTENTICATO = "Errore Autenticazione: Utente non autenticato.";
    public final static String ERRORE_GENERICO = "Errore durante l'inizializzazione: ";
    public final static String CARICAMENTO_UTENTE_FALLITO = "Errore nel caricamento delle informazioni utente: ";
    public final static String PRENOTAZIONI_ERROR = "Errore nel caricamento delle prenotazioni: ";
    public final static String PRENOTAZIONE_CANCELLATA = "La prenotazione è stata rimossa con successo!";
    public final static String CAMPI_PASSWORD_OBBLIGATORI = "Tutti i campi password sono obbligatori.";
    public final static String PASSWORD_NON_COINCIDONO = "Le password non corrispondono.";
    public final static String PASSWORD_TROPPO_CORTA = "La password deve contenere almeno 6 caratteri, una maiuscola, una minuscola, un numero e un carattere speciale (!@#$%^&*)";
    public final static String UTENTE_NON_IDENTIFICATO = "Utente non identificato. Impossibile cambiare la password.";
    public final static String PASSWORD_CAMBIATA = "La password è stata modificata con successo!";
    public final static String LOGOUT = "Logout effettuato con successo.";
    public final static String EMAIL_NON_DISPONIBILE = "Email utente non disponibile.";
    public final static String UTENTE_NON_TROVATO = "Utente non trovato.";


    // MESSAGGI WISHLIST
    public final static String WISHLIST_CARICAMENTO_ERRORE = "Impossibile caricare la wishlist dal database.";
    public final static String WISHLIST_RIMOZIONE_ERRORE = "Impossibile rimuovere il camion dai preferiti.";
    public final static String WISHLIST_SVUOTAMENTO_ERRORE = "Impossibile svuotare la wishlist.";
    public final static String WISHLIST_RIMOZIONE_CAMION = "Sei sicuro di voler rimuovere questo camion dai preferiti?";
    public final static String WISHLIST_CARICAMENTO_CAMION_ERRORE = "Errore nel caricamento delle informazioni del camion.";
    public final static String WISHLIST_CAMION_AGGIUNTO = "Camion aggiunto ai preferiti con successo.";
    public final static String WISHLIST_CAMION_RIMOSSO = "Camion rimosso dai preferiti con successo.";
    public final static String WISHLIST_LIMITE_RAGGIUNTO = "Puoi avere massimo 6 camion nei preferiti.";
    public final static String WISHLIST_CAMION_GIA_PRESENTE = "Questo camion è già nei tuoi preferiti.";
    public final static String WISHLIST_ERRORE = "Errore durante l'aggiunta al database.";
    public final static String WISHLIST_RIMOZIONE_CAMION_ERRORE = "Errore durante la rimozione dal database.";
    public final static String WISHLIST_RIMOZIONE_SUCCESSO = "La rimozione del camion è stata completata con successo!";
    public final static String WISHLIST_SVUOTAMENTO_SUCCESSO = "La wishlist è stata svuotata con successo!";
    public final static String WISHLIST_CAMION_ERRORE_APERTURA = "Impossibile aprire i dettagli del camion.";
    public final static String WISHLIST_CAMION_ERRORE_AGGIUNTA = "Impossibile aggiungere il camion ai preferiti.";


    public final static String app_information = """
        TruckScout24 è un' applicazione per la vendita di Camion usati del concessionario TruckScout24. Sviluppato in Java per il corso di Ingegneria del Software.
        Sviluppatori:
        
            Marco Picerno 223723
            """;
    public final static String app_information_error = "Errore nell'apertura delle informazioni sull'app";

    public final static String privacy_information = """
            1) Raccolta dei dati personali:
            Raccogliamo i tuoi dati personali, come nome, cognome, 
            indirizzo email, al fine di elaborare le prenotazioni,
            fornire assistenza e personalizzare la tua
            esperienza.
                        
            2) Utilizzo dei dati personali:
            Utilizziamo i tuoi dati personali per elaborare le prenotazioni,
            fornire assistenza clienti, personalizzare l'esperienza e 
            migliorare i nostri servizi.
                        
            3) Condivisione dei dati personali:
            Non condividiamo i tuoi dati personali con terze parti senza il
            tuo consenso, tranne per fornitori di servizi di fiducia che ci
            assistono nell'elaborazione delle prenotazioni e per adempiere
            a obblighi legali.
                        
            4) Sicurezza dei dati:
            Implementiamo misure di sicurezza per proteggere i tuoi dati
            personali da accessi non autorizzati o divulgazioni.
            """;

    public final static String general_condition = """
            1) Accettazione delle condizioni:
            Utilizzando la nostra applicazione,
            accetti di essere vincolato dalle presenti Condizioni Generali
            di Uso e Vendita. Ti invitiamo a leggerle attentamente prima
            di procedere con eventuali azioni.
                        
            2) Utilizzo del sito:
            La nostra applicazione è destinata esclusivamente all'utilizzo
            personale e non commerciale. Non è consentito modificare,
            riprodurre, duplicare o distribuire il contenuto dell'
            applicazione senza il nostro consenso scritto.
                        
            3) Proprietà intellettuale:
            Tutti i diritti di proprietà intellettuale relativi al
            contenuto della nostra applicazione, inclusi testi, 
            grafiche, loghi, immagini, sono di nostra proprietà o di
            terze parti autorizzate. È vietata la riproduzione o
            l'utilizzo non autorizzato di tali materiali.
                        
            4) Prodotti e prezzi:
            Forniamo descrizioni accurate dei camion disponibili
            nella nostra applicazione. Tuttavia, non possiamo garantire
            che le informazioni siano sempre complete, accurate o
            aggiornate. I prezzi dei camion sono indicati in valuta
            locale e possono essere soggetti a modifiche senza preavviso.
            """;
}
