package com.example.observer;

public interface RoomObserver {
    void onRoomAssigned(String roomLabel);
    void onRoomReleased(String roomLabel);
}
