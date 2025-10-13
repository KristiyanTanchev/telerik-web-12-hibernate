package com.company.web.springdemo.repositories;

import com.company.web.springdemo.models.Style;
import com.company.web.springdemo.models.User;

import java.util.List;

public interface StyleRepository {

    List<Style> get();

    Style get(int id);

    Style update(Style style);

    void delete(int id);

}
