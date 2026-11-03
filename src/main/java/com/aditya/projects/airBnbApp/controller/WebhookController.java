package com.aditya.projects.airBnbApp.controller;

import com.aditya.projects.airBnbApp.service.BookingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final BookingService bookingService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/payment")
    public ResponseEntity<Void> capturePayment(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {
            Event event = Webhook.constructEvent(
                    payload, sigHeader, endpointSecret
            );

            log.info("Stripe webhook received: {}", event.getType());

            bookingService.capturePayment(event);

            return ResponseEntity.noContent().build();

        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature", e);
            return ResponseEntity.badRequest().build();
        }
    }
}