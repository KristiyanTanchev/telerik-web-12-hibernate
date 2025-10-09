package com.company.web.springdemo.repositories;

import com.company.web.springdemo.exceptions.EntityNotFoundException;
import com.company.web.springdemo.models.Style;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StyleRepositoryImpl implements StyleRepository {

    private final String dbUrl, dbUsername, dbPassword;

    @Autowired
    public StyleRepositoryImpl(Environment env) {
        dbUrl = env.getProperty("database.url");
        dbUsername = env.getProperty("database.username");
        dbPassword = env.getProperty("database.password");
    }

    @Override
    public List<Style> get() {
        String query = "select style_id, name " +
                "from styles";
        try (
                Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)
        ) {
            return getStyles(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Style get(int id) {
        String query = "select style_id, name " +
                "from styles " +
                "where style_id = ?";
        try (
                Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Style> result = getStyles(resultSet);
                if (result.size() == 0) {
                    throw new EntityNotFoundException("Style", id);
                }
                return result.get(0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Style> getStyles(ResultSet styleData) throws SQLException {
        List<Style> styles = new ArrayList<>();
        while (styleData.next()) {
            Style style = new Style();
            style.setId(styleData.getInt("style_id"));
            style.setName(styleData.getString("name"));
            styles.add(style);
        }
        return styles;
    }

}