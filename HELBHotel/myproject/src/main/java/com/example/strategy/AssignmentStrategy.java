package com.example.strategy;

import com.example.models.Reservation;

public interface AssignmentStrategy {
    String assignRoom(Reservation reservation);
}
