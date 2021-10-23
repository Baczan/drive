package com.baczan.session_authorization_server.service;

import com.baczan.session_authorization_server.dtos.StorageSpaceDTO;
import com.baczan.session_authorization_server.entities.Card;
import com.baczan.session_authorization_server.entities.CustomerEntity;
import com.baczan.session_authorization_server.entities.SubscriptionEntity;
import com.baczan.session_authorization_server.entities.User;
import com.baczan.session_authorization_server.exceptions.TierNotFoundException;
import com.baczan.session_authorization_server.repositories.CardRepository;
import com.baczan.session_authorization_server.repositories.CustomerRepository;
import com.baczan.session_authorization_server.repositories.SubscriptionRepository;
import com.baczan.session_authorization_server.repositories.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class StripeService {

    private final ReentrantLock lock = new ReentrantLock();
    private final ReentrantLock paymentLock = new ReentrantLock();


    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TierService tierService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public Customer getCustomer(Authentication authentication) throws StripeException {

        Optional<CustomerEntity> optionalCustomerEntity = customerRepository.findByUserEmail(authentication.getName());

        Customer customer;

        if (optionalCustomerEntity.isPresent()) {

            customer = Customer.retrieve(optionalCustomerEntity.get().getCustomerId());

        } else {
            CustomerCreateParams customerCreateParams = CustomerCreateParams.builder()
                    .setEmail(authentication.getName())
                    .build();

            customer = Customer.create(customerCreateParams);

            CustomerEntity customerEntity = new CustomerEntity(authentication.getName(),customer.getId());
            customerRepository.save(customerEntity);
        }

        return customer;
    }

    //Disable transaction because they mess with lock
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public SubscriptionEntity updateSubscription(Subscription subscription, Invoice invoice,String email) throws TierNotFoundException {
        lock.lock();
        try {

            return updateSubscriptionPrivate(subscription, invoice, email);

        } finally {
            lock.unlock();
        }
    }


    private SubscriptionEntity updateSubscriptionPrivate(Subscription subscription, Invoice invoice, String email) throws TierNotFoundException {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findBySubscriptionId(subscription.getId()).orElse(new SubscriptionEntity());

        System.out.println(subscriptionEntity);

        subscriptionEntity.update(
                subscription,
                invoice,
                email,
                tierService.getTierByPrice(subscription.getItems().getData().get(0).getPrice().getId())
        );

        subscriptionEntity = subscriptionRepository.save(subscriptionEntity);

        simpMessagingTemplate.convertAndSendToUser(email, "/queue/storageSpace", getStorageSpace(email));

        return subscriptionEntity;
    }

    //Disable transaction because they mess with lock
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void deleteSubscription(String subscriptionId) throws TierNotFoundException {
        lock.lock();
        try {

            subscriptionRepository.deleteBySubscriptionId(subscriptionId);

        } finally {
            lock.unlock();
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updatePaymentMethod(PaymentMethod paymentMethod){

        paymentLock.lock();
        try {

            updatePaymentMethodPrivate(paymentMethod);

        } finally {
            paymentLock.unlock();
        }

    }

    private void updatePaymentMethodPrivate(PaymentMethod paymentMethod){

        if(!paymentMethod.getType().equals("card")){
            return;
        }

        Card card = cardRepository.findByPaymentMethodId(paymentMethod.getId()).orElse(new Card());

        card.updateCard(paymentMethod);

        cardRepository.save(card);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void deletePaymentMethod(String paymentMethodId){

        paymentLock.lock();
        try {

            cardRepository.deleteByPaymentMethodId(paymentMethodId);

        } finally {
            paymentLock.unlock();
        }

    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public StorageSpaceDTO getStorageSpace(String email) throws TierNotFoundException {

        StorageSpaceDTO storageSpaceDTO = new StorageSpaceDTO();

        Optional<SubscriptionEntity> optionalSubscriptionEntity = subscriptionRepository.findByUserEmail(email);

        if(optionalSubscriptionEntity.isEmpty()){
            storageSpaceDTO.setAvailableSpace(tierService.getTierByName("free").getSize());
        }else {
            SubscriptionEntity subscriptionEntity = optionalSubscriptionEntity.get();
            storageSpaceDTO.setAvailableSpace(tierService.getTierByName(subscriptionEntity.getTier()).getSize());
        }

        User user = userRepository.getUserByEmail(email);

        storageSpaceDTO.setUsedSpace(user.getStorageSpace());

        return storageSpaceDTO;
    }

}
