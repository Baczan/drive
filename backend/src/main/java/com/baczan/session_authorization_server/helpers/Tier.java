package com.baczan.session_authorization_server.helpers;

public class Tier {

    private String tierName;
    private String stripePriceId;
    private long size;

    public Tier() {
    }

    public Tier(String tierName, String stripePriceId, long size) {
        this.tierName = tierName;
        this.stripePriceId = stripePriceId;
        this.size = size;
    }

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public String getStripePriceId() {
        return stripePriceId;
    }

    public void setStripePriceId(String stripePriceId) {
        this.stripePriceId = stripePriceId;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
