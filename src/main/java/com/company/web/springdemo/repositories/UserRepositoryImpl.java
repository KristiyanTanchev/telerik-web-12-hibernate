package com.company.web.springdemo.repositories;

import com.company.web.springdemo.exceptions.EntityNotFoundException;
import com.company.web.springdemo.helpers.DbHelper;
import com.company.web.springdemo.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final DbHelper dbHelper;

    @Autowired
    public UserRepositoryImpl(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public List<User> get() {
        String query = "select * " +
                "from users";
        try (
                Connection connection = dbHelper.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)
        ) {
            return getUsers(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User get(int id) {
        String query = "select * " +
                "from users " +
                "where user_id = ?";
        try (
                Connection connection = dbHelper.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<User> result = getUsers(resultSet);
                if (result.size() == 0) {
                    throw new EntityNotFoundException("User", id);
                }
                return result.get(0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User getByEmail(String email) {
        String query = "select * " +
                "from users " +
                "where email = ?";
        try (
                Connection connection = dbHelper.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<User> result = getUsers(resultSet);
                if (result.size() == 0) {
                    throw new EntityNotFoundException("User", "email", email);
                }
                return result.get(0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User getByUsername(String username) {
        String query = "select *" +
                "from users " +
                "where username = ?";
        try (
                Connection connection = dbHelper.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<User> result = getUsers(resultSet);
                if (result.size() == 0) {
                    throw new EntityNotFoundException("User", "username", username);
                }
                return result.get(0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void create(User user) {
        String query = "insert into users (username, password, first_name, last_name, email, is_admin) " +
                "values (?, ?, ?, ?, ?, ?)";
        try (
                Connection connection = dbHelper.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getFirstName());
            statement.setString(4, user.getLastName());
            statement.setString(5, user.getEmail());
            statement.setBoolean(6, user.isAdmin());
            statement.executeUpdate();

            User newUser = getByEmail(user.getEmail());
            user.setId(newUser.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(User user) {
        String query = "update users set " +
                "username = ?, " +
                "password = ?, " +
                "first_name = ?, " +
                "last_name = ?, " +
                "email = ?, " +
                "is_admin = ? " +
                "where user_id = ?";
        try (
                Connection connection = dbHelper.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getFirstName());
            statement.setString(4, user.getLastName());
            statement.setString(5, user.getEmail());
            statement.setBoolean(6, user.isAdmin());
            statement.setInt(7, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        String query = "delete from users " +
                "where user_id = ?";
        try (
                Connection connection = dbHelper.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<User> getUsers(ResultSet userData) throws SQLException {
        List<User> users = new ArrayList<>();
        while (userData.next()) {
            User user = new User();
            user.setId(userData.getInt("user_id"));
            user.setUsername(userData.getString("username"));
            user.setPassword(userData.getString("password"));
            user.setFirstName(userData.getString("first_name"));
            user.setLastName(userData.getString("last_name"));
            user.setEmail(userData.getString("email"));
            user.setAdmin(userData.getBoolean("is_admin"));
            users.add(user);
        }
        return users;
    }
}
