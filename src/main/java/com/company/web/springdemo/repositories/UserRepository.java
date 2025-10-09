package com.company.web.springdemo.repositories;

import com.company.web.springdemo.models.Beer;
import com.company.web.springdemo.models.User;

import java.util.List;
import java.util.Set;

public interface UserRepository {

    List<User> get();

    User get(int id);

    User getByEmail(String email);

    User getByUsername(String username);

    void create(User user);

    void update(User user);

    void delete(int id);

    void addBeerToWishList(User user, Beer beer);

    void removeFromWishList(User user, Beer beer);

    Set<Beer> getWishList(int userId);
}
