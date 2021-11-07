package com.baczan.session_authorization_server.exceptions;

public class FileNotFoundException extends Exception{

    public FileNotFoundException() {
        super("not_found");
    }
}
