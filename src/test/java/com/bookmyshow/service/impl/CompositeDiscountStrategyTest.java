package com.bookmyshow.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CompositeDiscountStrategy
 * Tests the Strategy Pattern implementation
 */
class CompositeDiscountStrategyTest {

    private final CompositeDiscountStrategy discountStrategy = new CompositeDiscountStrategy();

    @Test
    void testCalculateDiscount_ThreeSeats_FiftyPercentOnThird() {
        // Arrange
        double totalAmount = 600.0; // 3 seats @ 200 each
        int numberOfSeats = 3;
        boolean isAfternoonShow = false;

        // Act
        double discount = discountStrategy.calculateDiscount(totalAmount, numberOfSeats, isAfternoonShow);

        // Assert
        assertEquals(100.0, discount, 0.01); // 50% of 200 (third ticket)
    }

    @Test
    void testCalculateDiscount_AfternoonShow_TwentyPercent() {
        // Arrange
        double totalAmount = 300.0; // 2 seats @ 150 each
        int numberOfSeats = 2;
        boolean isAfternoonShow = true;

        // Act
        double discount = discountStrategy.calculateDiscount(totalAmount, numberOfSeats, isAfternoonShow);

        // Assert
        assertEquals(60.0, discount, 0.01); // 20% of 300
    }

    @Test
    void testCalculateDiscount_ThreeSeats_AfternoonShow_BothDiscounts() {
        // Arrange
        double totalAmount = 450.0; // 3 seats @ 150 each
        int numberOfSeats = 3;
        boolean isAfternoonShow = true;

        // Act
        double discount = discountStrategy.calculateDiscount(totalAmount, numberOfSeats, isAfternoonShow);

        // Assert
        // 50% on third ticket: 150 * 0.50 = 75
        // 20% afternoon discount: 450 * 0.20 = 90
        // Total discount: 75 + 90 = 165
        assertEquals(165.0, discount, 0.01);
    }

    @Test
    void testCalculateDiscount_TwoSeats_NoAfternoonShow_NoDiscount() {
        // Arrange
        double totalAmount = 400.0; // 2 seats @ 200 each
        int numberOfSeats = 2;
        boolean isAfternoonShow = false;

        // Act
        double discount = discountStrategy.calculateDiscount(totalAmount, numberOfSeats, isAfternoonShow);

        // Assert
        assertEquals(0.0, discount, 0.01); // No discount applicable
    }

    @Test
    void testCalculateDiscount_OneSeat_NoDiscount() {
        // Arrange
        double totalAmount = 200.0; // 1 seat @ 200
        int numberOfSeats = 1;
        boolean isAfternoonShow = false;

        // Act
        double discount = discountStrategy.calculateDiscount(totalAmount, numberOfSeats, isAfternoonShow);

        // Assert
        assertEquals(0.0, discount, 0.01); // No discount applicable
    }

    @Test
    void testCalculateDiscount_OneSeat_AfternoonShow_OnlyAfternoonDiscount() {
        // Arrange
        double totalAmount = 150.0; // 1 seat @ 150
        int numberOfSeats = 1;
        boolean isAfternoonShow = true;

        // Act
        double discount = discountStrategy.calculateDiscount(totalAmount, numberOfSeats, isAfternoonShow);

        // Assert
        assertEquals(30.0, discount, 0.01); // 20% of 150
    }

    @Test
    void testCalculateDiscount_FourSeats_OnlyThirdTicketDiscount() {
        // Arrange
        double totalAmount = 800.0; // 4 seats @ 200 each
        int numberOfSeats = 4;
        boolean isAfternoonShow = false;

        // Act
        double discount = discountStrategy.calculateDiscount(totalAmount, numberOfSeats, isAfternoonShow);

        // Assert
        assertEquals(100.0, discount, 0.01); // 50% on third ticket only
    }

    @Test
    void testCalculateDiscount_PremiumSeats_ThreeSeats() {
        // Arrange
        double totalAmount = 900.0; // 3 premium seats @ 300 each
        int numberOfSeats = 3;
        boolean isAfternoonShow = false;

        // Act
        double discount = discountStrategy.calculateDiscount(totalAmount, numberOfSeats, isAfternoonShow);

        // Assert
        assertEquals(150.0, discount, 0.01); // 50% of 300 (third ticket)
    }

    @Test
    void testCalculateDiscount_MixedSeats_Afternoon() {
        // Arrange
        double totalAmount = 700.0; // Mixed seats (200 + 300 + 200)
        int numberOfSeats = 3;
        boolean isAfternoonShow = true;

        // Act
        double discount = discountStrategy.calculateDiscount(totalAmount, numberOfSeats, isAfternoonShow);

        // Assert
        // Third ticket discount: 700/3 * 0.50 = 116.67
        // Afternoon discount: 700 * 0.20 = 140
        // Total: 256.67
        assertEquals(256.67, discount, 0.01);
    }
}
