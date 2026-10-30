package com.aditya.projects.airBnbApp.service;

import com.aditya.projects.airBnbApp.entity.Booking;

public interface CheckoutService {

    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
