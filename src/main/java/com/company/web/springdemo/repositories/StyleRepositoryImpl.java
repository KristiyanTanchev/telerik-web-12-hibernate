package com.company.web.springdemo.repositories;

import com.company.web.springdemo.exceptions.EntityNotFoundException;
import com.company.web.springdemo.models.Style;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StyleRepositoryImpl implements StyleRepository {

    private final SessionFactory sessionFactory;

    @Autowired
    public StyleRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Style> get() {
        try(Session session = sessionFactory.openSession()){
            Query<Style> query = session.createQuery("from Style", Style.class);
            return query.list();
        }
    }

    @Override
    public Style get(int id) {
        try(Session session = sessionFactory.openSession()){
            Style style = session.get(Style.class, id);
            if (style == null){
                throw new EntityNotFoundException("Style", "id", String.valueOf(id));
            }
            return style;
        }
    }

    @Override
    public Style update(Style style) {
        try(Session session = sessionFactory.openSession()){
            Transaction tx = session.beginTransaction();
            try{
                Style styleToUpdate = get(style.getId());
                styleToUpdate.setName(style.getName());
                tx.commit();
                return styleToUpdate;
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public void delete(int id) {
        try (Session session = sessionFactory.openSession()){
            Transaction tx = session.beginTransaction();
            try{
                Style styleToRemove = session.get(Style.class, id);
                if (styleToRemove == null){
                    throw new EntityNotFoundException("Style", "id", String.valueOf(id));
                }
                session.remove(styleToRemove);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }
}