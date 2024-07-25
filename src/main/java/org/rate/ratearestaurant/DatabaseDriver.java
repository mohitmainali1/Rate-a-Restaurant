package org.rate.ratearestaurant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseDriver {
    private final String dbFileName;
    private Connection connection;

    public DatabaseDriver(String dbFileName) {
        this.dbFileName = dbFileName;
    }

    public void connect() throws SQLException {
        if(connection != null && !connection.isClosed()) {
            throw new IllegalStateException("Connection already opened.");
        }

        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        connection.setAutoCommit(false);
    }

    public void commit() throws SQLException {
        connection.commit();
    }

    public void rollback() throws SQLException {
        connection.rollback();
    }

    public void disconnect() throws SQLException {
        connection.close();
    }

    final String createUsers = """
                create table if not exists Users
                (
                    Username TEXT primary key,
                    Password TEXT NOT NULL
                );
            """;

    final String createRestaurants = """
                create table if not exists Restaurants
                (
                    ID INTEGER primary key,
                    RestaurantType TEXT NOT NULL,
                    RestaurantName TEXT NOT NULL,
                    RestaurantAddress TEXT NOT NULL,
                    AverageRating TEXT
                );
            """;

    final String createReviews = """
            create table if not exists Reviews
            (
                ID INTEGER primary key,
                RestaurantID INTEGER NOT NULL,
                Username TEXT NOT NULL,
                Rating INTEGER NOT NULL,
                Comment TEXT,
                Timestamp TIMESTAMP NOT NULL,
                FOREIGN KEY (RestaurantID) REFERENCES Restaurants(ID) ON DELETE CASCADE,
                FOREIGN KEY (Username) REFERENCES Users(Username) ON DELETE CASCADE
            );
            """;

    public void createTables() throws SQLException {
        if(connection.isClosed()) {
            throw new IllegalStateException("Connection is not open.");
        }

        var createTables = List.of(createRestaurants, createUsers, createReviews);

        for(var query : createTables) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        }
    }

    public void addRestaurant(Restaurant restaurant) throws SQLException {
        if(connection.isClosed()) {
            throw new IllegalStateException("Connection is not open.");
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            INSERT INTO Restaurants(RestaurantType, RestaurantName, RestaurantAddress, AverageRating) values
                            (?, ?, ?, ?)
                        """);
            preparedStatement.setString(1, restaurant.getRestaurantType());
            preparedStatement.setString(2, restaurant.getRestaurantName());
            preparedStatement.setString(3, restaurant.getRestaurantAddress());
            preparedStatement.setString(4, restaurant.getAvgRating());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        }
        catch(SQLException e) {
            rollback();
            throw e;
        }
    }

    private Restaurant getRestaurant(ResultSet resultSet) throws SQLException {
        int ID = resultSet.getInt("ID");
        String restaurantType = resultSet.getString("RestaurantType");
        String restuarantName = resultSet.getString("RestaurantName");
        String restaurantAddress = resultSet.getString("RestaurantAddress");
        String averageRating = resultSet.getString("AverageRating");

        return new Restaurant(restaurantType, restuarantName, restaurantAddress, averageRating, ID);
    }

    private static boolean isEmpty(ResultSet resultSet) throws SQLException {
        return !resultSet.isBeforeFirst() && resultSet.getRow() == 0;
    }

    public List<Restaurant> getAllRestaurants() throws SQLException {
        if(connection.isClosed()) {
            throw new IllegalStateException("Connection is not open.");
        }

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from Restaurants");
        var restaurants = new ArrayList<Restaurant>();

        ResultSet resultSet = preparedStatement.executeQuery();

        while(resultSet.next()) {
            var restaurant = getRestaurant(resultSet);
            restaurants.add(restaurant);
        }

        preparedStatement.close();

        return restaurants;
    }

    public Restaurant getRestaurantById(int restaurantId) throws SQLException {
        if(connection.isClosed()) {
            throw new IllegalStateException("Connection is not open.");
        }

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from Restaurants WHERE ID = ?");
        preparedStatement.setInt(1, restaurantId);

        ResultSet resultSet = preparedStatement.executeQuery();

        var restaurant = getRestaurant(resultSet);
        preparedStatement.close();

        return restaurant;
    }

    public List<Restaurant> getRestaurantsByAll(String type, String name, String address) throws SQLException {
        if(connection.isClosed()) {
            throw new IllegalStateException("Connection is not open.");
        }

        List<Restaurant> restaurants = new ArrayList<>();

        String query = "SELECT * FROM Restaurants WHERE 1=1";

        if(address != null && !address.isEmpty()) {
            query += " AND LOWER(RestaurantAddress) LIKE LOWER(?)";
        }

        if(name != null && !name.isEmpty()) {
            query += " AND RestaurantName = ?";
        }

        if(type != null && !type.isEmpty()) {
            query += " AND LOWER(RestaurantType) = LOWER(?)";
        }

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        int parameterIndex = 1;

        if(address != null && !address.isEmpty()) {
            preparedStatement.setString(parameterIndex++, "%" + address + "%");
        }

        if(name != null && !name.isEmpty()) {
            preparedStatement.setString(parameterIndex++, name);
        }

        if(type != null && !type.isEmpty()) {
            preparedStatement.setString(parameterIndex++, type);
        }

        ResultSet resultSet = preparedStatement.executeQuery();

        while(resultSet.next()) {
            Restaurant restaurant = getRestaurant(resultSet);
            restaurants.add(restaurant);
        }

        resultSet.close();
        preparedStatement.close();

        return restaurants;
    }

    public void addUser(User user) throws SQLException {
        if(connection.isClosed()) {
            throw new IllegalStateException("Connection is not open.");
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                        INSERT INTO Users(Username, Password) values 
                        (?, ?)
                        """);

            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        }
        catch(SQLException e) {
            rollback();
            throw e;
        }
    }

    private User getUser(ResultSet resultSet) throws SQLException {
        String username = resultSet.getString("Username");
        String password = resultSet.getString("Password");

        return new User(username, password);
    }

    public Optional<User> getUserById(String username) throws SQLException {
        if(connection.isClosed()) {
            throw new IllegalStateException("Connection is not open.");
        }

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from Users WHERE Username = ?");
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();

        if(!isEmpty(resultSet)) {
            var user = getUser(resultSet);
            preparedStatement.close();
            return Optional.of(user);
        }
        else {
            preparedStatement.close();
            return Optional.empty();
        }
    }

    public void addReview(Review review) throws SQLException {
        if(connection.isClosed()) {
            throw new IllegalStateException("Connection is not open.");
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                        INSERT INTO Reviews(RestaurantID, Username, Rating, Comment, Timestamp) values
                        (?, ?, ?, ?, ?)
                        """);

            preparedStatement.setInt(1, review.getRestaurantID());
            preparedStatement.setString(2, review.getUsername());
            preparedStatement.setInt(3, review.getRating());
            preparedStatement.setString(4, review.getComment());
            preparedStatement.setTimestamp(5, review.getTimestamp());
            preparedStatement.executeUpdate();
            preparedStatement.close();

            connection.commit();
        }
        catch(SQLException e) {
            rollback();
            throw e;
        }
    }

    public Review getReview(ResultSet resultSet) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp("Timestamp");
        int ID = resultSet.getInt("ID");
        int restaurantID = resultSet.getInt("RestaurantID");
        int rating = resultSet.getInt("Rating");
        String username = resultSet.getString("Username");
        String comment = resultSet.getString("Comment");

        return new Review(timestamp, ID, restaurantID, rating, username, comment);
    }

    public Optional<Review> getReviewById(int reviewID) throws SQLException {
        if(connection.isClosed()) {
            throw new IllegalStateException("Connection is not open.");
        }

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from Reviews WHERE ID = ?");
        preparedStatement.setInt(1, reviewID);
        ResultSet resultSet = preparedStatement.executeQuery();

        if(!isEmpty(resultSet)) {
            var review = getReview(resultSet);
            preparedStatement.close();
            return Optional.of(review);
        }
        else {
            preparedStatement.close();
            return Optional.empty();
        }
    }

    public ArrayList<Review> getReviewsForRestaurant(Restaurant restaurant) throws SQLException {
        if(connection.isClosed()) {
            throw new IllegalStateException("Connection is not open.");
        }

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from Reviews WHERE RestaurantID = ?");
        preparedStatement.setInt(1, restaurant.getID());

        var reviews = new ArrayList<Review>();

        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()) {
            Optional<Review> review = getReviewById(resultSet.getInt("ID"));
            review.ifPresent(reviews::add);
        }

        return reviews;
    }

    public ArrayList<Review> getReviewsForUser(User user) throws SQLException {
        if(connection.isClosed()) {
            throw new IllegalStateException("Connection is not open.");
        }

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from Reviews WHERE Username = ?");
        preparedStatement.setString(1, user.getUsername());

        var reviews = new ArrayList<Review>();

        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()) {
            Optional<Review> review = getReviewById(resultSet.getInt("ID"));
            review.ifPresent(reviews::add);
        }

        return reviews;
    }

    public boolean deleteReviewForRestaurant(User user, Restaurant restaurant) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("DELETE from Reviews WHERE Username = ? AND RestaurantID = ?");
        preparedStatement.setString(1, user.getUsername());
        preparedStatement.setInt(2, restaurant.getID());

        var reviews = new ArrayList<Review>();

        int rowsDeleted = preparedStatement.executeUpdate();
        connection.commit();
        return rowsDeleted > 0;
    }

    public void updateRestaurantAvg(Restaurant restaurant) throws SQLException {
        ArrayList<Review> allReviews = getReviewsForRestaurant(restaurant);

        double total = 0;
        for(Review review : allReviews) {
            total += review.getRating();
        }

        double average = 0;
        if(!allReviews.isEmpty()) {
            average = total / (double) allReviews.size();
        }

        String parsed = " ";
        if(average != 0) {
            parsed = String.format("%.2f", average);
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Restaurants SET AverageRating = ? WHERE RestaurantType = ? AND RestaurantName = ? AND RestaurantAddress = ?");
            preparedStatement.setString(1, parsed);
            preparedStatement.setString(2, restaurant.getRestaurantType());
            preparedStatement.setString(3, restaurant.getRestaurantName());
            preparedStatement.setString(4, restaurant.getRestaurantAddress());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.commit();
        }
        catch(SQLException e) {
            rollback();
            throw e;
        }
    }

    public void clearTables() throws SQLException {
        if(connection.isClosed()) {
            throw new IllegalStateException("Connection is not open.");
        }

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Reviews");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            preparedStatement = connection.prepareStatement("DELETE FROM Users");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            preparedStatement = connection.prepareStatement("DELETE FROM Restaurants");
            preparedStatement.executeUpdate();
            preparedStatement.close();
        }
        catch(SQLException e) {
            rollback();
            throw e;
        }
    }

}