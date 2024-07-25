package org.rate.ratearestaurant;

public class Restaurant {
    private String restaurantType, restaurantName, restaurantAddress, avgRating;
    private int ID;

    public Restaurant() {

    }

    public Restaurant(String restaurantType, String restaurantName, String restaurantAddress, String avgRating, int ID) {
        this.restaurantType = restaurantType;
        this.restaurantName = restaurantName;
        this.restaurantAddress = restaurantAddress;
        this.avgRating = avgRating;
        this.ID = ID;
    }

    public Restaurant(String restaurantType, String restaurantAddress, String restaurantName) {
        this.restaurantType = restaurantType;
        this.restaurantAddress = restaurantAddress;
        this.restaurantName = restaurantName;
        this.avgRating = " ";
    }

    public String getRestaurantType() {
        return restaurantType;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public String getAvgRating() {
        return avgRating;
    }

    public int getID() {
        return ID;
    }

    @Override
    public String toString() {
        return restaurantType + restaurantName + restaurantAddress;
    }

    public boolean equals(Restaurant restaurant) {
        return restaurant.restaurantType.equals(this.restaurantType) && restaurant.restaurantName.equals(this.restaurantName) && restaurant.restaurantAddress.equals(this.restaurantAddress);
    }

}