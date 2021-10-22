package com.baczan.session_authorization_server.repositories;

import com.baczan.session_authorization_server.entities.Card;
import com.baczan.session_authorization_server.entities.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventEntity,Integer> {

    boolean existsByEventId(String eventId);

    Optional<EventEntity> findFirstByOrderByIdDesc();

}
