package com.company.web.springdemo.helpers;

import com.company.web.springdemo.models.User;
import com.company.web.springdemo.models.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserMapper() {
    }

    public User fromDto(UserDto userDto){
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        return user;
    }
}
