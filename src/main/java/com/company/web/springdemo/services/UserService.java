package com.company.web.springdemo.services;

import com.company.web.springdemo.models.Beer;
import com.company.web.springdemo.models.User;

import java.util.List;
import java.util.Set;

public interface UserService {

    List<User> get();

    User get(int id);

    User get(String username);

    void create(User user);

    User update(int userId, User user);

    void delete(int id, User requester);

    void addBeerToWishList(User user, int beerId, int userId);

    void removeFromWishList(User user, int beerId, int userId);

    Set<Beer> getWishList(User requester, int userId);
}
