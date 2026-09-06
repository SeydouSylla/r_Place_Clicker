package com.rplace.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

 // Gestionnaire global des exceptions.
 // Intercepte toutes les exceptions non gerees dans les controleurs.
 //Retourne des reponses JSON propres pour les erreurs API.

@RestControllerAdvice
public class GestionnaireExceptions {

    @ExceptionHandler(CreditInsuffisantException.class)
    public ResponseEntity<Map<String, String>> gererCreditInsuffisant(
            CreditInsuffisantException ex) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(PixelIntrouvableException.class)
    public ResponseEntity<Map<String, String>> gererPixelIntrouvable(
            PixelIntrouvableException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(JoueurIntrouvableException.class)
    public ResponseEntity<Map<String, String>> gererJoueurIntrouvable(
            JoueurIntrouvableException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> gererErreurMetier(
            IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }
}
