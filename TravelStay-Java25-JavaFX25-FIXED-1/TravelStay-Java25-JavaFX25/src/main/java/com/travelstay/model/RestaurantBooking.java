package com.travelstay.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record RestaurantBooking(int reservationId, String restaurantName, String tableNumber, LocalDate date,
                                 LocalTime time, int guests, String status) {}
