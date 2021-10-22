package com.baczan.session_authorization_server.repositories;

import com.baczan.session_authorization_server.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity,Integer> {

    Optional<CustomerEntity> findByUserEmail(String email);
    Optional<CustomerEntity> findByCustomerId(String customerId);

    boolean existsByCustomerIdAndUserEmail(String customerId,String email);

}
