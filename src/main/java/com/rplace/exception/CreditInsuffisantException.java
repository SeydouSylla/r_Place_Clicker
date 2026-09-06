package com.rplace.exception;

// Exception metier levee quand le joueur n'a pas assez de credits.
public class CreditInsuffisantException extends RuntimeException {
    public CreditInsuffisantException(String message) { super(message); }
}
