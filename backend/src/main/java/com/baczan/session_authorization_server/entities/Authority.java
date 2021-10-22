package com.baczan.session_authorization_server.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Authority  {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(nullable = false)
  private String role;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "user_id")
  private User user_id;

  public Authority() {}

  public Authority(String role, User user_id) {
    this.role = role;
    this.user_id = user_id;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public User getUser_id() {
    return user_id;
  }

  public void setUser_id(User user_id) {
    this.user_id = user_id;
  }

}
