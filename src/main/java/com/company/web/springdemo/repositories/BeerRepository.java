package com.company.web.springdemo.repositories;

import com.company.web.springdemo.models.Beer;

import java.util.List;

public interface BeerRepository {

    List<Beer> get(String name, Double minAbv, Double maxAbv, String styleName, String sortBy, String sortOrder);

    Beer get(int id);

    Beer get(String name);

    void create(Beer beer);

    void update(Beer beer);

    void delete(int id);

}
