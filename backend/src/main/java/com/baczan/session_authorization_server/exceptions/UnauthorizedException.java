package com.baczan.session_authorization_server.exceptions;

public class UnauthorizedException extends Exception{

    public UnauthorizedException( ) {
        super("unauthorized");
    }
}
