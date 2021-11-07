package com.baczan.session_authorization_server.dtos;

import com.baczan.session_authorization_server.entities.FileEntity;
import com.baczan.session_authorization_server.entities.Folder;

import java.util.List;
import java.util.UUID;

public class FolderTransferRequestBody {
    private List<UUID> folders;
    private List<UUID> files;

    public FolderTransferRequestBody() {
    }

    public List<UUID> getFolders() {
        return folders;
    }

    public void setFolders(List<UUID> folders) {
        this.folders = folders;
    }

    public List<UUID> getFiles() {
        return files;
    }

    public void setFiles(List<UUID> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "FolderTransferRequestBody{" +
                "folders=" + folders +
                ", files=" + files +
                '}';
    }
}
