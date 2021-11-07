package com.baczan.session_authorization_server.dtos;

import com.baczan.session_authorization_server.entities.FileEntity;
import com.baczan.session_authorization_server.entities.Folder;

import java.util.List;

public class TransferFolderResponse {

    private Folder folder;
    private List<TransferFolder> transferFolders;
    private List<FileEntity> files;

    public TransferFolderResponse() {
    }

    public TransferFolderResponse(Folder folder, List<TransferFolder> transferFolders, List<FileEntity> fileEntities) {
        this.folder = folder;
        this.transferFolders = transferFolders;
        this.files = fileEntities;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public List<TransferFolder> getTransferFolders() {
        return transferFolders;
    }

    public void setTransferFolders(List<TransferFolder> transferFolders) {
        this.transferFolders = transferFolders;
    }

    public List<FileEntity> getFiles() {
        return files;
    }

    public void setFiles(List<FileEntity> files) {
        this.files = files;
    }
}
