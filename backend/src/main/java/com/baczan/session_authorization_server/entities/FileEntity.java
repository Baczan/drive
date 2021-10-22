package com.baczan.session_authorization_server.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "file")
public class FileEntity {

    @Id
    @Column(unique = true, nullable = false)
    private UUID id;

    @Column(name = "folder_id")
    private UUID folderId;

    @Column(name="user",nullable = false)
    private String user;

    @Column(name="filename",nullable = false)
    private String filename;

    @Column(name="size",nullable = false)
    private long size;

    @Column(name="date",nullable = false)
    private Date date;

    @Column(name="has_thumbnail",nullable = false)
    private boolean hasThumbnail;

    public FileEntity() {
    }



    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getFolderId() {
        return folderId;
    }

    public void setFolderId(UUID folderId) {
        this.folderId = folderId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isHasThumbnail() {
        return hasThumbnail;
    }

    public void setHasThumbnail(boolean hasThumbnail) {
        this.hasThumbnail = hasThumbnail;
    }
}
