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
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class BeerRepositoryImpl implements BeerRepository {

    private final SessionFactory sessionFactory;

    @Autowired
    public BeerRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Beer> get(String name, Double minAbv, Double maxAbv, String styleName, String sortBy, String sortOrder) {
        try (Session session = sessionFactory.openSession()) {

            // Normalize ABV bounds if swapped
            if (minAbv != null && maxAbv != null && minAbv > maxAbv) {
                double t = minAbv; minAbv = maxAbv; maxAbv = t;
            }

            StringBuilder hql = new StringBuilder("select b from Beer b");
            List<String> where = new ArrayList<>();
            Map<String, Object> params = new HashMap<>();

            if (name != null && !name.isBlank()) {
                where.add("lower(b.name) like :name");
                params.put("name", "%" + name.toLowerCase() + "%");
            }
            if (minAbv != null) {
                where.add("b.abv >= :minAbv");
                params.put("minAbv", minAbv);
            }
            if (maxAbv != null) {
                where.add("b.abv <= :maxAbv");
                params.put("maxAbv", maxAbv);
            }
            if (styleName != null && !styleName.isBlank()) {
                where.add("b.style.name = :styleName");
                params.put("styleName", "%" + styleName.toLowerCase() + "%");
            }

            if (!where.isEmpty()) {
                hql.append(" where ").append(String.join(" and ", where));
            }

            // --- Sorting (whitelist) ---
            String sortKey = (sortBy == null || sortBy.isBlank()) ? "name" : sortBy;
            String sortExpr = switch (sortKey) {
                case "id"   -> "b.id";
                case "abv"  -> "b.abv";
                case "name" -> "b.name";
                // add more allowed props here, e.g. case "ibu" -> "b.ibu";
                default     -> "b.name";
            };
            boolean desc = sortOrder != null && sortOrder.equalsIgnoreCase("desc");
            hql.append(" order by ").append(sortExpr).append(desc ? " desc" : " asc");

            Query<Beer> query = session.createQuery(hql.toString(), Beer.class);
            params.forEach(query::setParameter);

            return query.list(); // or query.getResultList() in newer APIs
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
            Transaction tx = session.beginTransaction();
            try {
                session.persist(beer);
                tx.commit();
            }catch (Exception e){
                tx.rollback();
            }
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
