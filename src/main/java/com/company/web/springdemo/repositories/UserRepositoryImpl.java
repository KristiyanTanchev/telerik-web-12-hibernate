package com.company.web.springdemo.repositories;

import com.company.web.springdemo.exceptions.EntityNotFoundException;
import com.company.web.springdemo.models.Beer;
import com.company.web.springdemo.models.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
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
        try(Session session = sessionFactory.openSession())
        {
            Query<User> query = session.createQuery("from User", User.class);
            return query.list();
        }
    }

    @Override
    public User get(int id) {
        try(Session session = sessionFactory.openSession()){
            User user = session.get(User.class, id);
            if (user == null){
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
            if (result.isEmpty()){
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
            if (result.isEmpty()){
                throw new EntityNotFoundException("User", "username", username);
            }
            return result.get(0);
        }
    }

    @Override
    public void create(User user) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(user);
            session.getTransaction().commit();
        }
    }

    @Override
    public User update(User user) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.merge(user);
            session.getTransaction().commit();
            return user;
        }
    }

    @Override
    public void delete(int id) {
        try(Session session = sessionFactory.openSession()){
            Transaction tx = session.beginTransaction();
            try {
                User user = session.get(User.class, id);
                if (user == null) {
                    throw new EntityNotFoundException("User", "id", String.valueOf(id));
                }
                session.remove(user);
                tx.commit();
            }catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public void addBeerToWishList(User user, Beer beer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeFromWishList(User user, Beer beer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Beer> getWishList(int userId) {
        throw new UnsupportedOperationException();
    }

    private List<User> getUsers(ResultSet userData) throws SQLException {
        List<User> users = new ArrayList<>();
        while (userData.next()) {
            User user = new User();
            user.setId(userData.getInt("user_id"));
            user.setUsername(userData.getString("username"));
            user.setPassword(userData.getString("password"));
            user.setFirstName(userData.getString("first_name"));
            user.setLastName(userData.getString("last_name"));
            user.setEmail(userData.getString("email"));
            user.setAdmin(userData.getBoolean("is_admin"));
            users.add(user);
        }
        return users;
    }
}
