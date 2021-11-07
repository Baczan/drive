package com.baczan.session_authorization_server.exceptions;

public class FolderNotFoundException extends Exception{

    public FolderNotFoundException() {
        super("not_found");
    }
}
