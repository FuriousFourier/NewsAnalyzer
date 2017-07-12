package database.DAO;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unchecked")
public class GenericDAOHibernate <T, PK extends Serializable> implements GenericDAO<T,PK> {

    private SessionFactory sessionFactory;
    private Class<T> type;
    private final Session session;

    public GenericDAOHibernate() {
        session = getSession();
        session.beginTransaction();
    }

    public GenericDAOHibernate(Class<T> type){
        this.type = type;
        session = getSession();
        session.beginTransaction();
    }

    public PK create(T newInstance) {
        return (PK) session.save(newInstance);
    }

    public T read(PK id) {
        return session.get(type,id);
    }

    public List executeQuery(String query){
        return session.createQuery(query).getResultList();
    }

    public void update(T transientObject) {
        session.update(transientObject);
    }

    public void delete(T persistentObject) {
        session.delete(persistentObject);
    }

    private Session getSession()
    {
        Configuration configuration = new Configuration().configure();
        sessionFactory = configuration.buildSessionFactory();
        return sessionFactory.openSession();
    }

    public void closeSession(){
        session.getTransaction().commit();
        sessionFactory.close();
    }

    public void flushAndClear(){
        session.flush();
        session.clear();
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
