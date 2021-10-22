package com.baczan.session_authorization_server.entities;

import com.stripe.model.PaymentMethod;

import javax.persistence.*;

@Entity
@Table(name = "card")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "brand",nullable = false)
    private String brand;

    @Column(name = "exp_month",nullable = false)
    private long expMonth;

    @Column(name = "exp_year",nullable = false)
    private long expYear;

    @Column(name = "last_digits",nullable = false)
    private String lastDigits;

    @Column(name = "payment_method_id",nullable = false)
    private String paymentMethodId;

    @Column(name = "name",nullable = false)
    private String name;

    @Column(name = "customer_id",nullable = false)
    private String customerId;

    public Card() {
    }

    public void updateCard(PaymentMethod paymentMethod) {

        this.brand = paymentMethod.getCard().getBrand();
        this.expMonth = paymentMethod.getCard().getExpMonth();
        this.expYear = paymentMethod.getCard().getExpYear();
        this.lastDigits = paymentMethod.getCard().getLast4();
        this.paymentMethodId = paymentMethod.getId();
        this.name = paymentMethod.getBillingDetails().getName();
        this.customerId = paymentMethod.getCustomer();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getLastDigits() {
        return lastDigits;
    }

    public void setLastDigits(String lastDigits) {
        this.lastDigits = lastDigits;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
