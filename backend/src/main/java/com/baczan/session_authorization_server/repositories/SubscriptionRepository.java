package com.baczan.session_authorization_server.repositories;

import com.baczan.session_authorization_server.entities.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity,Integer> {

    Optional<SubscriptionEntity> findByUserEmail(String email);
    SubscriptionEntity getBySubscriptionId(String subscriptionId);


    Optional<SubscriptionEntity> findBySubscriptionId(String subscriptionId);

    Integer deleteBySubscriptionId(String subscriptionId);

    boolean existsBySubscriptionId(String subscriptionId);

    boolean existsByUserEmail(String email);

}
