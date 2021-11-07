package com.baczan.session_authorization_server.controllers;


import com.baczan.session_authorization_server.dtos.CardDTO;
import com.baczan.session_authorization_server.dtos.subscriptionCreationDOT;
import com.baczan.session_authorization_server.entities.Card;
import com.baczan.session_authorization_server.entities.CustomerEntity;
import com.baczan.session_authorization_server.entities.EventEntity;
import com.baczan.session_authorization_server.entities.SubscriptionEntity;
import com.baczan.session_authorization_server.exceptions.SubscriptionUpdateError;
import com.baczan.session_authorization_server.exceptions.TierNotFoundException;
import com.baczan.session_authorization_server.helpers.Tier;
import com.baczan.session_authorization_server.repositories.CardRepository;
import com.baczan.session_authorization_server.repositories.CustomerRepository;
import com.baczan.session_authorization_server.repositories.EventRepository;
import com.baczan.session_authorization_server.repositories.SubscriptionRepository;
import com.baczan.session_authorization_server.service.StripeService;
import com.baczan.session_authorization_server.service.TierService;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.stripe.param.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;


import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class StripeController {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TierService tierService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private Environment environment;

    @PostConstruct
    private void setStripeKey() {
        Stripe.apiKey = environment.getProperty("app.stripe.key");
        try {
            getEvents();

        }catch (Exception e){
            System.out.println(e);
        }

    }


    @GetMapping("/api/stripe/getCurrentSubscription")
    public ResponseEntity<?> getCurrentSubscription(Authentication authentication) {

        Optional<SubscriptionEntity> optionalSubscriptionEntity = subscriptionRepository.findByUserEmail(authentication.getName());
        return new ResponseEntity<>(optionalSubscriptionEntity.orElse(null), HttpStatus.OK);
    }

    @PostMapping("/api/stripe/updateSubscription")
    public ResponseEntity<?> updateSubscription(Authentication authentication, @RequestParam() String subscriptionId, @RequestParam() String paymentMethodId) throws StripeException, TierNotFoundException {

        Subscription subscription = Subscription.retrieve(subscriptionId);

        Customer customer = stripeService.getCustomer(authentication);

        if (!subscription.getCustomer().equals(customer.getId())) {
            return new ResponseEntity<>("authentication_error", HttpStatus.UNAUTHORIZED);
        }

        SubscriptionUpdateParams subscriptionUpdateParams = SubscriptionUpdateParams.builder()
                .setDefaultPaymentMethod(paymentMethodId)
                .build();

        subscription = subscription.update(subscriptionUpdateParams);


        try {
            SubscriptionEntity subscriptionEntity = updateSubscription(subscription);

            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

            stripeService.updatePaymentMethod(paymentMethod);

            return new ResponseEntity<>(subscriptionEntity, HttpStatus.OK);

        } catch (SubscriptionUpdateError subscriptionUpdateError) {
            subscriptionUpdateError.printStackTrace();
        }


        return new ResponseEntity<>("error", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/api/stripe/paymentMethods")
    public ResponseEntity<?> getPaymentMethods(Authentication authentication) throws StripeException {

        Customer customer = stripeService.getCustomer(authentication);
        return new ResponseEntity<>(cardRepository.findAllByCustomerId(customer.getId()), HttpStatus.OK);
    }


    @GetMapping("/api/stripe/createSubscription")
    public ResponseEntity<?> subscription(@RequestParam String tierName, Authentication authentication) throws TierNotFoundException, StripeException {

        if (subscriptionRepository.existsByUserEmail(authentication.getName())) {
            return new ResponseEntity<>("already_have", HttpStatus.BAD_REQUEST);
        }


        Tier tier = tierService.getTierByName(tierName);
        Customer customer = stripeService.getCustomer(authentication);

        SubscriptionCreateParams subscriptionCreateParams = SubscriptionCreateParams.builder()
                .setCustomer(customer.getId())
                .addItem(SubscriptionCreateParams.Item.builder()
                        .setPrice(tier.getStripePriceId())
                        .build())
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                .build();

        Subscription subscription = Subscription.create(subscriptionCreateParams);

        Invoice invoice = Invoice.retrieve(subscription.getLatestInvoice());
        PaymentIntent paymentIntent = PaymentIntent.retrieve(invoice.getPaymentIntent());

        return new ResponseEntity<>(new subscriptionCreationDOT(subscription.getId(), paymentIntent.getClientSecret()), HttpStatus.OK);

    }

    @PostMapping("/api/stripe/subscriptionChangeTier")
    public ResponseEntity<?> changeTier(Authentication authentication,@RequestParam String tierName) throws StripeException, TierNotFoundException {

        Optional<SubscriptionEntity> optionalSubscriptionEntity = subscriptionRepository.findByUserEmail(authentication.getName());

        if (optionalSubscriptionEntity.isEmpty()) {
            return new ResponseEntity<>("user_dont_have_a_subscription", HttpStatus.BAD_REQUEST);
        }

        SubscriptionEntity subscriptionEntity = optionalSubscriptionEntity.get();

        Tier tier = tierService.getTierByName(tierName);

        SubscriptionUpdateParams subscriptionUpdateParams = SubscriptionUpdateParams.builder()
                .addItem(
                        SubscriptionUpdateParams.Item.builder()
                                .setPrice(tier.getStripePriceId())
                                .build()
                )
                .build();


        Subscription subscription = Subscription.retrieve(subscriptionEntity.getSubscriptionId());
        subscription = subscription.update(subscriptionUpdateParams);

        for (SubscriptionItem item:subscription.getItems().autoPagingIterable()) {

            if(!item.getPrice().getId().equals(tier.getStripePriceId())){
                item.delete();
            }

        }


        subscription = Subscription.retrieve(subscriptionEntity.getSubscriptionId());

        try {

            subscriptionEntity = updateSubscription(subscription);
            return new ResponseEntity<>(subscriptionEntity, HttpStatus.OK);

        }catch (SubscriptionUpdateError subscriptionUpdateError){
            return new ResponseEntity<>("subscription_error", HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("/api/stripe/subscriptionState")
    public ResponseEntity<?> subscriptionState(Authentication authentication,@RequestParam boolean state) throws StripeException, TierNotFoundException {

        Optional<SubscriptionEntity> optionalSubscriptionEntity = subscriptionRepository.findByUserEmail(authentication.getName());

        if (optionalSubscriptionEntity.isEmpty()) {
            return new ResponseEntity<>("user_dont_have_a_subscription", HttpStatus.BAD_REQUEST);
        }

        SubscriptionEntity subscriptionEntity = optionalSubscriptionEntity.get();

        SubscriptionUpdateParams subscriptionUpdateParams = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(!state)
                .build();

        Subscription subscription = Subscription.retrieve(subscriptionEntity.getSubscriptionId());

        subscription = subscription.update(subscriptionUpdateParams);

        try {

            subscriptionEntity = updateSubscription(subscription);
            return new ResponseEntity<>(subscriptionEntity, HttpStatus.OK);

        }catch (SubscriptionUpdateError subscriptionUpdateError){
            return new ResponseEntity<>("subscription_error", HttpStatus.BAD_REQUEST);
        }

    }


    @PostMapping("/api/stripe/events")
    @ResponseBody
    public ResponseEntity<?> events(@RequestBody String payload, HttpServletRequest request) {


        try {
            Event event = Webhook.constructEvent(payload, request.getHeader("Stripe-Signature"), environment.getProperty("whsec_LJS5TKgvjfJ2b7tRPnaDQ1R5R3dyKopj"));

            handleEvent(event);
            return new ResponseEntity<>("ok", HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("error", HttpStatus.BAD_REQUEST);
        }

    }


    private void getEvents() throws StripeException {

        Optional<EventEntity> optionalEventEntity = eventRepository.findFirstByOrderByIdDesc();

        if(optionalEventEntity.isEmpty()){
            return;
        }

        String eventId = optionalEventEntity.get().getEventId();

        while (eventId!=null){


            EventListParams eventListParams = EventListParams.builder()
                    .setLimit(100L)
                    .setStartingAfter(eventId)
                    .build();

            EventCollection eventCollection = Event.list(eventListParams);

            eventId = null;

            List<Event> eventList = eventCollection.getData();

            for (int i = 0; i < eventList.size(); i++) {

                Event event = eventList.get(i);

                try {
                    handleEvent(event);
                } catch (SubscriptionUpdateError | TierNotFoundException | StripeException | DataIntegrityViolationException ignored) {

                }


                eventId = event.getId();
            }
        }

    }

    private void handleEvent(Event event) throws TierNotFoundException, StripeException, SubscriptionUpdateError {

        if(eventRepository.existsByEventId(event.getId())){
            return;
        }

        System.out.println(event.getType());


        switch (event.getType()) {

            case "invoice.payment_succeeded":

                Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().get();

                if (invoice.getBillingReason().equals("subscription_create")) {
                    Subscription subscription = Subscription.retrieve(invoice.getSubscription());
                    PaymentIntent paymentIntent = PaymentIntent.retrieve(invoice.getPaymentIntent());

                    SubscriptionUpdateParams subscriptionUpdateParams = SubscriptionUpdateParams.builder()
                            .setDefaultPaymentMethod(paymentIntent.getPaymentMethod())
                            .build();

                    subscription = subscription.update(subscriptionUpdateParams);
                    updateSubscription(subscription);
                }

                break;
            case "customer.subscription.deleted": {

                Subscription subscription = (Subscription) event.getDataObjectDeserializer().getObject().get();
                stripeService.deleteSubscription(subscription.getId());

                break;
            }
            case "customer.subscription.updated": {

                Subscription subscription = (Subscription) event.getDataObjectDeserializer().getObject().get();

                if (subscription.getStatus().equals("active")) {
                    updateSubscription(subscription);
                }


                break;
            }
            case "payment_method.attached":
            case "payment_method.automatically_updated":
            case "payment_method.updated": {

                PaymentMethod paymentMethod = (PaymentMethod) event.getDataObjectDeserializer().getObject().get();

                stripeService.updatePaymentMethod(paymentMethod);

                break;
            }
            case "payment_method.detached": {

                PaymentMethod paymentMethod = (PaymentMethod) event.getDataObjectDeserializer().getObject().get();
                stripeService.deletePaymentMethod(paymentMethod.getId());
                break;
            }
        }

        EventEntity eventEntity = new EventEntity(event.getId());
        eventRepository.save(eventEntity);
    }


    private SubscriptionEntity updateSubscription(Subscription subscription) throws StripeException, TierNotFoundException, SubscriptionUpdateError {

        Optional<CustomerEntity> optionalCustomerEntity = customerRepository.findByCustomerId(subscription.getCustomer());


        if (optionalCustomerEntity.isEmpty()) {
            throw new SubscriptionUpdateError("1");
        }

        CustomerEntity customerEntity = optionalCustomerEntity.get();


        if (subscription.getDefaultPaymentMethod() == null) {

            throw new SubscriptionUpdateError("2");
        }

        if (!subscription.getStatus().equals("active")) {
            throw new SubscriptionUpdateError("3");
        }

        Invoice invoice;

        if(subscription.getCancelAtPeriodEnd()){

            invoice = null;

        }else {
            InvoiceUpcomingParams invoiceUpcomingParams = InvoiceUpcomingParams.builder()
                    .setSubscription(subscription.getId())
                    .build();

            invoice = Invoice.upcoming(invoiceUpcomingParams);
        }
        

        return stripeService.updateSubscription(subscription, invoice, customerEntity.getUserEmail());
    }


}
