package com.bookmyshow.service;

/**
 * Strategy Pattern - Interface for discount calculation strategies
 * Open/Closed Principle - Open for extension, closed for modification
 */
public interface DiscountStrategy {
    
    /**
     * Calculate discount based on specific strategy
     * @param totalAmount Original amount
     * @param numberOfSeats Number of seats booked
     * @param isAfternoonShow Whether it's an afternoon show
     * @return Discount amount
     */
    double calculateDiscount(double totalAmount, int numberOfSeats, boolean isAfternoonShow);
}
