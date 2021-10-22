package com.baczan.session_authorization_server.dtos;

public class CardDTO {

    private String brand;
    private long expMonth;
    private long expYear;
    private String last4;
    private String paymentMethodId;
    private String name;

    public CardDTO() {
    }

    public CardDTO(String brand, long expMonth, long expYear, String last4, String paymentMethodId, String name) {
        this.brand = brand;
        this.expMonth = expMonth;
        this.expYear = expYear;
        this.last4 = last4;
        this.paymentMethodId = paymentMethodId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public long getExpMonth() {
        return expMonth;
    }

    public void setExpMonth(long expMonth) {
        this.expMonth = expMonth;
    }

    public long getExpYear() {
        return expYear;
    }

    public void setExpYear(long expYear) {
        this.expYear = expYear;
    }

    public String getLast4() {
        return last4;
    }

    public void setLast4(String last4) {
        this.last4 = last4;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
}
