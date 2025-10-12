package com.company.web.springdemo.services;

import com.company.web.springdemo.exceptions.AuthorizationException;
import com.company.web.springdemo.exceptions.EntityDuplicateException;
import com.company.web.springdemo.exceptions.EntityNotFoundException;
import com.company.web.springdemo.models.Beer;
import com.company.web.springdemo.models.User;
import com.company.web.springdemo.repositories.BeerRepository;
import com.company.web.springdemo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BeerRepository beerRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BeerRepository beerRepository) {
        this.userRepository = userRepository;
        this.beerRepository = beerRepository;
    }

    @Override
    public List<User> get() {
        return userRepository.get();
    }

    @Override
    public User get(int id) {
        return userRepository.get(id);
    }

    @Override
    public User get(String username) {
        return userRepository.getByUsername(username);
    }

    @Override
    public void create(User user) {
        validateUniqueEmail(user);
        validateUniqueUsername(user);

        userRepository.create(user);
    }

    @Override
    public User update(int userId, User user) {
        validateUniqueEmail(user, userId);
        validateUniqueUsername(user, userId);

        return userRepository.update(user);
    }

    @Override
    public void delete(int id, User requester) {
        if (!isUserAuthorized(requester, id)){
            throw new AuthorizationException("You are not authorized to complete this operation.");
        }
        userRepository.delete(id);
    }

    @Override
    public void addBeerToWishList(User user, int beerId, int userId) {
        if (!isUserAuthorized(user, userId)){
            throw new AuthorizationException("You are not authorized to add to this wishlist");
        }
        userRepository.addBeerToWishList(user, beerId);
    }

    @Override
    public void removeFromWishList(User requester, int beerId, int userId) {
        if (!isUserAuthorized(requester, userId)){
            throw new AuthorizationException("You are not authorized to remove from this wishlist.");
        }
        User user = userRepository.get(userId);
        Beer beer = beerRepository.get(beerId);
        if (beer == null){
            throw new EntityNotFoundException("Beer", "id", String.valueOf(beerId));
        }
        userRepository.removeFromWishList(userId, beerId);
    }

    @Override
    public Set<Beer> getWishList(User requester, int userId) {
        if (!isUserAuthorized(requester, userId)){
            throw new AuthorizationException("You are not authorized to browse user information.");
        }
        return userRepository.getWishList(userId);
    }

    private boolean isUserAuthorized(User requester, int userId) {
        return (requester.isAdmin() || requester.getId() == userId);
    }

    private void validateUniqueEmail(User user){
        try {
            userRepository.getByEmail(user.getEmail());
            throw new EntityDuplicateException("User", "email", user.getEmail());
        } catch (EntityNotFoundException ignored){ }
    }

    private void validateUniqueUsername(User user){
        try {
            userRepository.getByUsername(user.getUsername());
            throw new EntityDuplicateException("User", "username", user.getUsername());
        } catch (EntityNotFoundException ignored) { }
    }

    private void validateUniqueEmail(User user, int excludeId){
        try {
            User testUser = userRepository.getByEmail(user.getEmail());
            if (testUser.getId() != excludeId){
                throw new EntityDuplicateException("User", "email", user.getEmail());
            }
        } catch (EntityNotFoundException ignored){ }
    }

    private void validateUniqueUsername(User user, int excludeId){
        try {
            User testUser = userRepository.getByUsername(user.getUsername());
            if (testUser.getId() != excludeId){
                throw new EntityDuplicateException("User", "username", user.getUsername());
            }
        } catch (EntityNotFoundException ignored) { }
    }
}
