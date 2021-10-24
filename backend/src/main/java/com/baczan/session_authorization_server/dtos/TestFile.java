package com.baczan.session_authorization_server.dtos;

import java.util.UUID;

public class TestFile {
    public String name;
    public UUID parentId;

    public TestFile(String name, UUID parentId) {
        this.name = name;
        this.parentId = parentId;
    }
}
