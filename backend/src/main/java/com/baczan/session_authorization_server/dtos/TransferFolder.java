package com.baczan.session_authorization_server.dtos;

import com.baczan.session_authorization_server.entities.Folder;

public class TransferFolder {

    private Folder folder;
    private boolean canBeTransferred = true;

    public TransferFolder() {
    }

    public TransferFolder(Folder folder) {
        this.folder = folder;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public boolean isCanBeTransferred() {
        return canBeTransferred;
    }

    public void setCanBeTransferred(boolean canBeTransferred) {
        this.canBeTransferred = canBeTransferred;
    }

    @Override
    public String toString() {
        return "TransferFolder{" +
                "folder=" + folder +
                ", canBeTransferred=" + canBeTransferred +
                '}';
    }
}
