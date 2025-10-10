package com.company.web.springdemo.repositories;

import com.company.web.springdemo.exceptions.EntityNotFoundException;
import com.company.web.springdemo.helpers.DbHelper;
import com.company.web.springdemo.models.Beer;
import com.company.web.springdemo.models.Style;
import com.company.web.springdemo.models.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
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

    private final SessionFactory sessionFactory;

    @Autowired
    public BeerRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Beer> get(String name, Double minAbv, Double maxAbv, Integer styleId, String sortBy, String sortOrder) {
        try(Session session = sessionFactory.openSession())
        {
            Query<Beer> query = session.createQuery("from Beer", Beer.class);
            return query.list();
        }
    }

    @Override
    public Beer get(int id) {
        try(Session session = sessionFactory.openSession()){
            Beer beer = session.get(Beer.class, id);
            if (beer == null){
                throw new EntityNotFoundException("Beer", "id", String.valueOf(id));
            }
            return beer;
        }
    }

    @Override
    public Beer get(String name) {
        try (Session session = sessionFactory.openSession()) {
            Query<Beer> query = session.createQuery("from Beer where name = :name", Beer.class);
            query.setParameter("name", name);
            List<Beer> result = query.list();
            if (result.isEmpty()){
                throw new EntityNotFoundException("Beer", "name", name);
            }
            return result.get(0);
        }
    }

    @Override
    public void create(Beer beer) {
        try(Session session = sessionFactory.openSession()){
            session.beginTransaction();
            session.persist(beer);
            session.getTransaction().commit();
        }
    }

    @Override
    public void update(Beer beer) {
        Transaction tx = null;
        try(Session session = sessionFactory.openSession()){
            tx = session.beginTransaction();

            Beer managed = session.get(Beer.class, beer.getId());
            if (managed == null) {
                throw new EntityNotFoundException("Beer", "id", String.valueOf(beer.getId()));
            }
            managed.setName(beer.getName());
            managed.setAbv(beer.getAbv());
            if (beer.getStyle() != null) {
                managed.setStyle(session.getReference(Style.class, beer.getStyle().getId()));
            }
            tx.commit();
        }catch (Exception e){
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public void delete(int id) {
        try(Session session = sessionFactory.openSession()){
            Transaction tx = session.beginTransaction();
            try {
                Beer beer = session.get(Beer.class, id);
                if (beer == null) {
                    throw new EntityNotFoundException("Beer", "id", String.valueOf(id));
                }
                session.remove(beer);
                tx.commit();
            }catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }
}
