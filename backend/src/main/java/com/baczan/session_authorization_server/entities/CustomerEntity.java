package com.baczan.session_authorization_server.entities;

import javax.persistence.*;

@Entity
@Table(name = "customer")
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_email",nullable = false,unique = true)
    private String userEmail;

    @Column(name = "customer_id",nullable = false,unique = true)
    private String customerId;

    public CustomerEntity() {
    }

    public CustomerEntity(String userEmail, String customerId) {
        this.userEmail = userEmail;
        this.customerId = customerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
