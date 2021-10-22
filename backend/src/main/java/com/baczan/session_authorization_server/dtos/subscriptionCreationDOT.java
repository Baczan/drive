package com.baczan.session_authorization_server.dtos;

public class subscriptionCreationDOT {

    private String subscriptionId;
    private String clientSecret;

    public subscriptionCreationDOT(String subscriptionId, String clientSecret) {
        this.subscriptionId = subscriptionId;
        this.clientSecret = clientSecret;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
