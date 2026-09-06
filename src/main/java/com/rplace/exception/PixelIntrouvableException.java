package com.rplace.exception;

// Exception levee quand un pixel demande n'existe pas.
public class PixelIntrouvableException extends RuntimeException {
    public PixelIntrouvableException(String message) { super(message); }
}
