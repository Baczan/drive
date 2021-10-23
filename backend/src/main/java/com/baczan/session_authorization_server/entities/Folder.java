package com.baczan.session_authorization_server.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "folder")
public class Folder {

    @Id
    @Column(unique = true, nullable = false)
    private UUID id;

    @Column(name="user",nullable = false)
    private String user;

    @Column(name="folder_name",nullable = false)
    private String folderName;

    @Column(name="ancestry")
    private String ancestry;

    @Column(name="parent_id")
    private UUID parentId;

    @Column(name = "favorite")
    private boolean favorite = false;

    public Folder() {
    }

    public Folder(UUID id, String user, String folderName, String ancestry, UUID parentId) {
        this.id = id;
        this.user = user;
        this.folderName = folderName;
        this.ancestry = ancestry;
        this.parentId = parentId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getAncestry() {
        return ancestry;
    }

    public void setAncestry(String ancestry) {
        this.ancestry = ancestry;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public String toString() {
        return "Folder{" +
                "id=" + id +
                ", user='" + user + '\'' +
                ", folderName='" + folderName + '\'' +
                ", ancestry='" + ancestry + '\'' +
                ", parentId=" + parentId +
                '}';
    }
}
