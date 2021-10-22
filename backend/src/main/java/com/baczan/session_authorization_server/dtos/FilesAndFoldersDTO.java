package com.baczan.session_authorization_server.dtos;

import com.baczan.session_authorization_server.entities.FileEntity;
import com.baczan.session_authorization_server.entities.Folder;

import java.util.List;

public class FilesAndFoldersDTO {

    private List<Folder> folders;
    private List<FileEntity> files;
    private Folder parentFolder;

    public FilesAndFoldersDTO() {
    }

    public FilesAndFoldersDTO(List<Folder> folders, List<FileEntity> files) {
        this.folders = folders;
        this.files = files;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public List<FileEntity> getFiles() {
        return files;
    }

    public void setFiles(List<FileEntity> files) {
        this.files = files;
    }

    public Folder getParentFolder() {
        return parentFolder;
    }

    public void setParentFolder(Folder parentFolder) {
        this.parentFolder = parentFolder;
    }
}
