package com.example.models;

public class Reservation {
    private final String lastName;
    private final String firstName;
    private final int numberOfPeople;
    private final boolean smoker;
    private final String stayPurpose;
    private final int numberOfChildren;

    public Reservation(String lastName, String firstName, int numberOfPeople, boolean smoker, String stayPurpose, int numberOfChildren) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.numberOfPeople = numberOfPeople;
        this.smoker = smoker;
        this.stayPurpose = stayPurpose;
        this.numberOfChildren = numberOfChildren;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public boolean isSmoker() {
        return smoker;
    }

    public String getStayPurpose() {
        return stayPurpose;
    }

    public int getNumberOfChildren() {
        return numberOfChildren;
    }

    public boolean hasChildren() {
        return numberOfChildren > 0;
    }

    public String getKey() {
        return lastName + ":" + firstName;
    }

    @Override
    public String toString() {
        return lastName + " " + firstName + " - " + numberOfPeople + " pers. " +
            (smoker ? "Fumeur" : "Non fumeur") +
            " - " + stayPurpose + " - " + numberOfChildren + " enfant(s)";
    }
}
