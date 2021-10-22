package com.baczan.session_authorization_server.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "zip_info")
public class ZipInfo {

    @Id
    @Column(unique = true, nullable = false)
    private UUID id;

    @Column(name="user",nullable = false)
    private String user;

    @Column(name="size",nullable = false)
    private long size;

    @Column(name="date",nullable = false)
    private Date date;

    @Column(name="completed",nullable = false)
    private boolean completed = false;

    @Column(name="error",nullable = false)
    private boolean error = false;

    @Column(name = "progress",nullable = false)
    private float progress = 0;

    public ZipInfo() {
    }

    public ZipInfo(UUID id, String user, long size) {
        this.id = id;
        this.user = user;
        this.size = size;
        this.date = new Date();
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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }
}
