package com.baczan.session_authorization_server.repositories;

import com.baczan.session_authorization_server.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card,Integer> {

    Optional<Card> findByPaymentMethodId(String paymentMethodId);

    Integer deleteByPaymentMethodId(String paymentMethodId);

    List<Card> findAllByCustomerId(String customerId);

}
