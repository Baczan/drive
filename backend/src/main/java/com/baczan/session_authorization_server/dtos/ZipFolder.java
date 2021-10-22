package com.baczan.session_authorization_server.dtos;

import java.util.List;
import java.util.UUID;

public class ZipFolder {

    public String folderName;
    private UUID id;
    private List<String> ancestryList;

    public ZipFolder(String folderName, UUID id, List<String> ancestryList) {
        this.folderName = folderName;
        this.id = id;
        this.ancestryList = ancestryList;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<String> getAncestryList() {
        return ancestryList;
    }

    public void setAncestryList(List<String> ancestryList) {
        this.ancestryList = ancestryList;
    }

    @Override
    public String toString() {
        return "ZipFolder{" +
                "folderName='" + folderName + '\'' +
                ", id=" + id +
                ", ancestryList=" + ancestryList +
                '}';
    }
}
