package org.rate.ratearestaurant;

import java.sql.SQLException;

public class ReviewsListed {
    private int restaurantID, rating;
    private String restaurantType, restaurantName, username, comment;

    public ReviewsListed(Review review) throws SQLException {
        DatabaseDriver databaseDriver = new DatabaseDriver("appTable.sqlite");

        this.restaurantID = review.getRestaurantID();
        this.username = review.getUsername();
        this.comment = review.getComment();
        this.rating = review.getRating();

        databaseDriver.connect();
        Restaurant restaurant = databaseDriver.getRestaurantById(restaurantID);
        databaseDriver.disconnect();

        this.restaurantType = restaurant.getRestaurantType();
        this.restaurantName = restaurant.getRestaurantName();
    }

    public int getRestaurantID() {
        return restaurantID;
    }

    public int getRating() {
        return rating;
    }

    public String getRestaurantType() {
        return restaurantType;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getUsername() {
        return username;
    }

    public String getComment() {
        return comment;
    }

}