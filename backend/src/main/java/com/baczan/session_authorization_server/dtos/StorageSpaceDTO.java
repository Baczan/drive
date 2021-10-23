package com.baczan.session_authorization_server.dtos;

public class StorageSpaceDTO {

    private long usedSpace;
    private long availableSpace;

    public StorageSpaceDTO() {
    }

    public StorageSpaceDTO(long usedSpace, long availableSpace) {
        this.usedSpace = usedSpace;
        this.availableSpace = availableSpace;
    }

    public long getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(long usedSpace) {
        this.usedSpace = usedSpace;
    }

    public long getAvailableSpace() {
        return availableSpace;
    }

    public void setAvailableSpace(long availableSpace) {
        this.availableSpace = availableSpace;
    }
}
