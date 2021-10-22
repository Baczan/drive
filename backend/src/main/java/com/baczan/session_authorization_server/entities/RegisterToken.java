package com.baczan.session_authorization_server.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "register_token")
public class RegisterToken {

  @Id
  @Column(unique = true, nullable = false)
  private UUID id;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  public RegisterToken() {}

  public RegisterToken(UUID id, String email, String password) {
    this.id = id;
    this.email = email;
    this.password = password;
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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String toString() {
    return "Register_token{"
        + "id="
        + id
        + ", email='"
        + email
        + '\''
        + ", password='"
        + password
        + '\''
        + '}';
  }
}
