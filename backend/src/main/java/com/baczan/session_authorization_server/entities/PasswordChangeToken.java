package com.baczan.session_authorization_server.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "password_change_token")
public class PasswordChangeToken {

  @Id
  @Column(unique = true, nullable = false)
  private UUID id;

  @Column(nullable = false)
  private String email;

  public PasswordChangeToken() {}

  public PasswordChangeToken(UUID id, String email) {
    this.id = id;
    this.email = email;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String toString() {
    return "PasswordChangeToken{" + "id=" + id + ", email='" + email + '\'' + '}';
  }
}
