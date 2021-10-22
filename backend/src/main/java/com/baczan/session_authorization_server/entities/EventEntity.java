package com.baczan.session_authorization_server.entities;

import com.stripe.model.PaymentMethod;

import javax.persistence.*;

@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "event_id",nullable = false)
    private String eventId;


    public EventEntity() {
    }

    public EventEntity(String eventId) {
        this.eventId = eventId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
