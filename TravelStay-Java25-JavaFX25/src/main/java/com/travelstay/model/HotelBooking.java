package com.travelstay.model;

import java.time.LocalDate;

public record HotelBooking(int bookingId, String hotelName, String roomType, LocalDate checkIn,
                            LocalDate checkOut, int guests, double totalAmount, String status) {}
