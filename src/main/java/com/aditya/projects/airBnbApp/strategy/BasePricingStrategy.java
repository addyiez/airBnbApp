package com.aditya.projects.airBnbApp.strategy;

import com.aditya.projects.airBnbApp.entity.Inventory;

import java.math.BigDecimal;


public class BasePricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {

        return inventory.getRoom().getBasePrice();
    }
}
