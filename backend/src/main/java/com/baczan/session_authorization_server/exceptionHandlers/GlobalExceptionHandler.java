package com.baczan.session_authorization_server.exceptionHandlers;

import com.baczan.session_authorization_server.exceptions.FileNotFoundException;
import com.baczan.session_authorization_server.exceptions.TierNotFoundException;

import com.baczan.session_authorization_server.exceptions.UnauthorizedException;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {StripeException.class})
    public ResponseEntity<?> handleStripeException(Exception exception){
        System.out.println(exception.getMessage());
        return new ResponseEntity<>("stripeException", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {TierNotFoundException.class})
    public ResponseEntity<?> handleTierException(Exception exception){
        return new ResponseEntity<>("tier_exception", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {java.lang.IllegalArgumentException.class})
    public ResponseEntity<?> handleIllegalArgument(Exception exception){
        return new ResponseEntity<>("illegal_argument", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {FileNotFoundException.class})
    public ResponseEntity<?> handleBadRequestException(Exception exception){
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    public ResponseEntity<?> handleUnauthorized(Exception exception){
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }






}
