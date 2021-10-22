package com.baczan.session_authorization_server.repositories;

import com.baczan.session_authorization_server.entities.PasswordChangeToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PasswordChangeTokenRepository extends JpaRepository<PasswordChangeToken, UUID> {

  void deleteAllByEmail(String email);
}
