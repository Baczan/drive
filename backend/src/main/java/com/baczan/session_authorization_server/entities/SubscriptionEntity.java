package com.baczan.session_authorization_server.entities;

import com.baczan.session_authorization_server.helpers.Tier;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import org.springframework.security.core.Authentication;

import javax.persistence.*;

@Entity
@Table(name = "subscription")
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_email",nullable = false,unique = true)
    private String userEmail;

    @Column(name = "subscription_id",nullable = false,unique = true)
    private String subscriptionId;

    @Column(name = "customer_id",nullable = false,unique = true)
    private String customerId;

    @Column(name = "tier",nullable = false)
    private String tier;

    @Column(name = "default_payment_method")
    private String defaultPaymentMethod;

    @Column(name = "cancel_at_period_end",nullable = false)
    private boolean cancelAtPeriodEnd;

    @Column(name = "period_end",nullable = false)
    private long periodEnd;

    @Column(name = "total",nullable = false)
    private long total;


    public SubscriptionEntity() {
    }




    public SubscriptionEntity(String userEmail, String subscriptionId, String customerId, String tier) {
        this.userEmail = userEmail;
        this.subscriptionId = subscriptionId;
        this.customerId = customerId;
        this.tier = tier;
    }

    public void update(String userEmail, String subscriptionId, String customerId, String tier) {
        this.userEmail = userEmail;
        this.subscriptionId = subscriptionId;
        this.customerId = customerId;
        this.tier = tier;
    }

    public void update(Subscription subscription, Invoice invoice, String userEmail, Tier tier) {
        this.userEmail = userEmail;
        this.subscriptionId = subscription.getId();
        this.customerId = subscription.getCustomer();
        this.tier = tier.getTierName();
        this.defaultPaymentMethod = subscription.getDefaultPaymentMethod();
        this.cancelAtPeriodEnd = subscription.getCancelAtPeriodEnd();

        if(invoice==null){
            this.periodEnd = subscription.getCurrentPeriodEnd();
            this.total = 0;
        }else {
            this.periodEnd = invoice.getPeriodEnd();
            this.total = invoice.getAmountDue();
        }


    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
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

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getDefaultPaymentMethod() {
        return defaultPaymentMethod;
    }

    public void setDefaultPaymentMethod(String defaultPaymentMethod) {
        this.defaultPaymentMethod = defaultPaymentMethod;
    }

    public boolean isCancelAtPeriodEnd() {
        return cancelAtPeriodEnd;
    }

    public void setCancelAtPeriodEnd(boolean cancelAtPeriodEnd) {
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
    }

    public long getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(long periodEnd) {
        this.periodEnd = periodEnd;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "SubscriptionEntity{" +
                "id=" + id +
                ", userEmail='" + userEmail + '\'' +
                ", subscriptionId='" + subscriptionId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", tier='" + tier + '\'' +
                ", defaultPaymentMethod='" + defaultPaymentMethod + '\'' +
                ", cancelAtPeriodEnd=" + cancelAtPeriodEnd +
                ", periodEnd=" + periodEnd +
                ", total=" + total +
                '}';
    }
}
