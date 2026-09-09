package com.travelstay.model;

public record Room(int roomId, int hotelId, String roomType, double pricePerNight, int capacity, int totalRooms) {}
