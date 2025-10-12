package com.company.web.springdemo.repositories;

import com.company.web.springdemo.exceptions.EntityNotFoundException;
import com.company.web.springdemo.models.Beer;
import com.company.web.springdemo.models.User;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final SessionFactory sessionFactory;

    @Autowired
    public UserRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<User> get() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from User", User.class).list();
        }
    }

    @Override
    public User get(int id) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.get(User.class, id);
            if (user == null) {
                throw new EntityNotFoundException("User", "id", String.valueOf(id));
            }
            return user;
        }
    }

    @Override
    public User getByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery("from User where email = :email", User.class);
            query.setParameter("email", email);
            List<User> result = query.list();
            if (result.isEmpty()) {
                throw new EntityNotFoundException("User", "email", email);
            }
            return result.get(0);
        }
    }

    @Override
    public User getByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery("from User where username = :name", User.class);
            query.setParameter("name", username);
            List<User> result = query.list();
            if (result.isEmpty()) {
                throw new EntityNotFoundException("User", "username", username);
            }
            return result.get(0);
        }
    }

    @Override
    public void create(User user) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.persist(user);
                tx.commit();
            }catch (Exception e){
                tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public User update(User user) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.merge(user);
                tx.commit();
                return user;
            }catch (Exception e){
                tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public void delete(int id) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.get(User.class, id);
            if (user == null) {
                throw new EntityNotFoundException("User", "id", String.valueOf(id));
            }
            Transaction tx = session.beginTransaction();
            try {
                session.remove(user);
                tx.commit();
            }catch (Exception e){
                tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public void addBeerToWishList(User user, int beerId) {
        try (Session session = sessionFactory.openSession()) {
            User userToUpdate = session.get(User.class, user.getId());
            Beer beerToAdd = session.get(Beer.class, beerId);
            if (userToUpdate == null) {
                throw new EntityNotFoundException("User", "id", String.valueOf(user.getId()));
            }
            if (beerToAdd == null) {
                throw new EntityNotFoundException("Beer", "id", String.valueOf(beerId));
            }
            Transaction tx = session.beginTransaction();
            try {
                userToUpdate.getWishlist().add(beerToAdd);
                tx.commit();
            }catch (Exception e){
                tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public void removeFromWishList(int userId, int beerId) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.get(User.class, userId);
            Beer beer = session.get(Beer.class, beerId);
            if (!user.getWishlist().contains(beer)) {
                throw new EntityNotFoundException("Beer", "id", String.valueOf(beerId));
            }
            Transaction tx = session.beginTransaction();
            try {
                user.getWishlist().remove(beer);
                tx.commit();
            }catch (Exception e){
                tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public Set<Beer> getWishList(int userId) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.get(User.class, userId);
            if (user == null) {
                throw new EntityNotFoundException("User", "id", String.valueOf(userId));
            }
            Hibernate.initialize(user.getWishlist());
            return new HashSet<>(user.getWishlist());
        }
    }
}
