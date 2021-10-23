package com.baczan.session_authorization_server.repositories;

import com.baczan.session_authorization_server.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  @Modifying
  @Query("update User u set u.storageSpace = u.storageSpace+:amount where u.email = :email")
  void addStorageSpace(@Param("email") String email,@Param("amount") long amount);
}
