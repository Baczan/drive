package com.baczan.session_authorization_server.repositories;

import com.baczan.session_authorization_server.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findUserByEmailAndEmailActivatedIsTrue(String email);

  Optional<User> findUserByEmail(String email);

  Optional<User> findByGoogleId(String id);

  Optional<User> findByFacebookId(String id);

  User getUserByEmail(String email);

  User getByGoogleId(String googleId);

  User getByFacebookId(String facebookId);

  boolean existsByEmail(String email);

  boolean existsByEmailAndEmailActivatedIsTrue(String email);

  boolean existsByGoogleId(String googleId);

  boolean existsByFacebookId(String facebookId);
}
