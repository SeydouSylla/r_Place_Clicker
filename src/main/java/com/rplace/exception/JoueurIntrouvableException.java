package com.rplace.exception;

// Exception levee quand un joueur demande n'existe pas.
public class JoueurIntrouvableException extends RuntimeException {
    public JoueurIntrouvableException(String message) { super(message); }
}
