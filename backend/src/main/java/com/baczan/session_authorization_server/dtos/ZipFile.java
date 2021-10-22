package com.baczan.session_authorization_server.dtos;

import com.baczan.session_authorization_server.entities.FileEntity;

public class ZipFile {

    private FileEntity fileEntity;
    private String ancestry;

    public ZipFile(FileEntity fileEntity, String ancestry) {
        this.fileEntity = fileEntity;
        this.ancestry = ancestry;
    }

    public FileEntity getFileEntity() {
        return fileEntity;
    }

    public void setFileEntity(FileEntity fileEntity) {
        this.fileEntity = fileEntity;
    }

    public String getAncestry() {
        return ancestry;
    }

    public void setAncestry(String ancestry) {
        this.ancestry = ancestry;
    }

    @Override
    public String toString() {
        return "ZipFile{" +
                "fileEntity=" + fileEntity +
                ", ancestry='" + ancestry + '\'' +
                '}';
    }
}
