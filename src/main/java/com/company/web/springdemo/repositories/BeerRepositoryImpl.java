package com.company.web.springdemo.repositories;

import com.company.web.springdemo.exceptions.EntityNotFoundException;
import com.company.web.springdemo.helpers.DbHelper;
import com.company.web.springdemo.models.Beer;
import com.company.web.springdemo.models.Style;
import com.company.web.springdemo.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BeerRepositoryImpl implements BeerRepository {

    private final DbHelper dbHelper;
    private final StyleRepository styleRepository;
    private final UserRepository userRepository;

    @Autowired
    public BeerRepositoryImpl(DbHelper dbHelper, StyleRepository styleRepository, UserRepository userRepository) {
        this.dbHelper = dbHelper;
        this.styleRepository = styleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Beer> get(String name, Double minAbv, Double maxAbv, Integer styleId, String sortBy, String sortOrder) {
        String query = "select beer_id, name, abv, style_id, created_by " +
                "from beers";
        try (
                Connection connection = dbHelper.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)
        ) {
            List<Beer> beers = getBeers(resultSet);
            return filter(beers, name, minAbv, maxAbv, styleId, sortBy, sortOrder);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Beer get(int id) {
        String query = "select beer_id, name, abv, style_id, created_by " +
                "from beers " +
                "where beer_id = ?";
        try (
                Connection connection = dbHelper.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Beer> result = getBeers(resultSet);
                if (result.size() == 0) {
                    throw new EntityNotFoundException("Beer", id);
                }
                return result.get(0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Beer get(String name) {
        String query = "select beer_id, name, abv, style_id, created_by " +
                "from beers " +
                "where name = ?";
        try (
                Connection connection = dbHelper.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Beer> result = getBeers(resultSet);
                if (result.size() == 0) {
                    throw new EntityNotFoundException("Beer", "name", name);
                }
                return result.get(0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void create(Beer beer) {
        String query = "insert into beers (name, abv, style_id, created_by) " +
                "values (?, ?, ?, ?)";
        try (
                Connection connection = dbHelper.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, beer.getName());
            statement.setDouble(2, beer.getAbv());
            statement.setInt(3, beer.getStyle().getId());
            statement.setInt(4, beer.getCreatedBy().getId());
            statement.executeUpdate();

            Beer newBeer = get(beer.getName());
            beer.setId(newBeer.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Beer beer) {
        String query = "update beers set " +
                "name = ?, " +
                "abv = ?, " +
                "style_id = ? " +
                "where beer_id = ?";
        try (
                Connection connection = dbHelper.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, beer.getName());
            statement.setDouble(2, beer.getAbv());
            statement.setInt(3, beer.getStyle().getId());
            statement.setInt(4, beer.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        String query = "delete from beers " +
                "where beer_id = ?";
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

    private List<Beer> getBeers(ResultSet beersData) throws SQLException {
        List<Beer> beers = new ArrayList<>();
        while (beersData.next()) {
            Beer beer = new Beer();
            beer.setId(beersData.getInt("beer_id"));
            beer.setName(beersData.getString("name"));
            beer.setAbv(beersData.getDouble("abv"));
            Style style = styleRepository.get(beersData.getInt("style_id"));
            beer.setStyle(style);
            User createdBy = userRepository.get(beersData.getInt("created_by"));
            beer.setCreatedBy(createdBy);
            beers.add(beer);
        }
        return beers;
    }

    public List<Beer> filter(List<Beer> beers, String name, Double minAbv, Double maxAbv, Integer styleId, String sortBy, String sortOrder) {
        beers = filterByName(beers, name);
        beers = filterByAbv(beers, minAbv, maxAbv);
        beers = filterByStyle(beers, styleId);
        beers = sortBy(beers, sortBy);
        beers = order(beers, sortOrder);
        return beers;
    }

    private static List<Beer> filterByName(List<Beer> beers, String name) {
        if (name != null && !name.isEmpty()) {
            beers = beers.stream()
                    .filter(beer -> containsIgnoreCase(beer.getName(), name))
                    .collect(Collectors.toList());
        }
        return beers;
    }

    private static List<Beer> filterByAbv(List<Beer> beers, Double minAbv, Double maxAbv) {
        if (minAbv != null) {
            beers = beers.stream()
                    .filter(beer -> beer.getAbv() >= minAbv)
                    .collect(Collectors.toList());
        }

        if (maxAbv != null) {
            beers = beers.stream()
                    .filter(beer -> beer.getAbv() <= maxAbv)
                    .collect(Collectors.toList());
        }

        return beers;
    }

    private static List<Beer> filterByStyle(List<Beer> beers, Integer styleId) {
        if (styleId != null) {
            beers = beers.stream()
                    .filter(beer -> beer.getStyle().getId() == styleId)
                    .collect(Collectors.toList());
        }
        return beers;
    }

    private static List<Beer> sortBy(List<Beer> beers, String sortBy) {
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy.toLowerCase()) {
                case "name":
                    beers.sort(Comparator.comparing(Beer::getName));
                    break;
                case "abv":
                    beers.sort(Comparator.comparing(Beer::getAbv));
                case "style":
                    beers.sort(Comparator.comparing(beer -> beer.getStyle().getName()));
                    break;
            }
        }
        return beers;
    }

    private static List<Beer> order(List<Beer> beers, String order) {
        if (order != null && !order.isEmpty()) {
            if (order.equals("desc")) {
                Collections.reverse(beers);
            }
        }
        return beers;
    }

    private static boolean containsIgnoreCase(String value, String sequence) {
        return value.toLowerCase().contains(sequence.toLowerCase());
    }

}
