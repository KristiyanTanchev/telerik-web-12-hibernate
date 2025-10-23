package com.company.web.springdemo.controllers.rest;

import com.company.web.springdemo.exceptions.AuthorizationException;
import com.company.web.springdemo.exceptions.EntityDuplicateException;
import com.company.web.springdemo.exceptions.EntityNotFoundException;
import com.company.web.springdemo.helpers.AuthenticationHelper;
import com.company.web.springdemo.helpers.UserMapper;
import com.company.web.springdemo.models.Beer;
import com.company.web.springdemo.models.User;
import com.company.web.springdemo.models.UserDto;
import com.company.web.springdemo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationHelper authenticationHelper;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService,
                          AuthenticationHelper authenticationHelper,
                          UserMapper userMapper) {
        this.userService = userService;
        this.authenticationHelper = authenticationHelper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public List<User> get() {
        return userService.get();
    }

    @GetMapping("/{id}")
    public User get(@PathVariable int id) {
        try {
            return userService.get(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    @GetMapping("/{id}/wishlist")
    public Set<Beer> getWishList(@RequestHeader HttpHeaders headers,
                                 @PathVariable int id){
        try {
            User requester = authenticationHelper.tryGetUser(headers);
            return userService.getWishList(requester, id);
        }
        catch (AuthorizationException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    @PostMapping("/{userId}/wishlist/{beerId}")
    public void addToWishList(@RequestHeader HttpHeaders headers,
                              @PathVariable int userId,
                              @PathVariable int beerId){
        try {
            User requester = authenticationHelper.tryGetUser(headers);
            userService.addBeerToWishList(requester, beerId, userId);
        }
        catch (AuthorizationException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
        catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/wishlist/{beerId}")
    public void removeFromWishList(@RequestHeader HttpHeaders headers,
                              @PathVariable int userId,
                              @PathVariable int beerId){
        try {
            User requester = authenticationHelper.tryGetUser(headers);
            userService.removeFromWishList(requester, beerId, userId);
        }
        catch (AuthorizationException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
        catch (EntityNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    public User create(@Valid @RequestBody UserDto userDto) {
        User user;
        try {
            user = userMapper.fromDto(userDto);
            userService.create(user);
        } catch (EntityDuplicateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
        return user;
    }

    @PutMapping("/{id}")
    public User update(@PathVariable int id, @Valid @RequestBody UserDto userDto) {
        User user;
        try {
            user = userService.update(id, userMapper.fromDto(userDto));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (EntityDuplicateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
        return user;
    }

    @DeleteMapping("/{id}")
    public void delete(
            @RequestHeader HttpHeaders headers,
            @PathVariable int id) {
        try {
            User requester = authenticationHelper.tryGetUser(headers);
            userService.delete(id, requester);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
        catch (AuthorizationException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
}
