package org.rate.ratearestaurant;

import java.sql.Timestamp;

public class Review {
    private Timestamp timestamp;
    private int ID, restaurantID, rating;
    private String username, comment;

    public Review(Timestamp timestamp, int ID, int restaurantID, int rating, String username, String comment) {
        this.timestamp = timestamp;
        this.ID = ID;
        this.restaurantID = restaurantID;
        this.rating = rating;
        this.username = username;
        this.comment = comment;
    }

    public Review(int ID, int restaurantID, int rating, String username, String comment) {
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.ID = ID;
        this.restaurantID = restaurantID;
        this.rating = rating;
        this.username = username;
        this.comment = comment;
    }

    public Review(int restaurantID, int rating, String username, String comment) {
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.restaurantID = restaurantID;
        this.rating = rating;
        this.username = username;
        this.comment = comment;
    }

    public Review(int restaurantID, int rating, String username) {
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.restaurantID = restaurantID;
        this.rating = rating;
        this.username = username;
        this.comment = " ";
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getRestaurantID() {
        return restaurantID;
    }

    public int getRating() {
        return rating;
    }

    public String getUsername() {
        return username;
    }

    public String getComment() {
        return comment;
    }

}