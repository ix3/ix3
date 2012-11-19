/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.logongas.ix3.persistencia.hibernate;

import es.logongas.ix3.persistencia.dao.BussinessException;
import es.logongas.ix3.persistencia.dao.Criteria;
import es.logongas.ix3.persistencia.dao.GenericDAO;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class GenericDAOImplHibernate<EntityType, PrimaryKeyType extends Serializable> implements GenericDAO<EntityType, PrimaryKeyType> {

    @Autowired
    SessionFactory sessionFactory;
    
    Class<EntityType> entityType;
    
    protected final Log log = LogFactory.getLog(getClass());
    
    public GenericDAOImplHibernate() {
        this.entityType=(Class<EntityType>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
    
    public GenericDAOImplHibernate(Class<EntityType> entityType) {
        this.entityType=entityType;
    }    
    
    
    @Override
    public EntityType create() throws BussinessException {
        try {
            return getEntityClass().newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    
    @Override
    public void insert(EntityType entity) throws BussinessException {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            session.save(entity);
            session.getTransaction().commit();
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);           
        } catch (RuntimeException ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }    
    
    @Override
    public boolean update(EntityType entity) throws BussinessException {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            
            EntityType entity2 = (EntityType) session.get(getEntityClass(), session.getIdentifier(entity));
            if (entity == null) {
                session.getTransaction().commit();
                return false;
            } else {
                session.evict(entity2);
                session.update(entity);
                session.getTransaction().commit();
                return true;
            }            
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);           
        } catch (RuntimeException ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    public EntityType read(PrimaryKeyType id) throws BussinessException {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            EntityType entity = (EntityType) session.get(getEntityClass(), id);           
            session.getTransaction().commit();

            return entity;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);           
        } catch (RuntimeException ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean delete(PrimaryKeyType id) throws BussinessException {
        Session session = sessionFactory.getCurrentSession();
        try {
            session.beginTransaction();
            EntityType entity = (EntityType) session.get(getEntityClass(), id);
            if (entity == null) {
                session.getTransaction().commit();
                return false;
            } else {
                session.delete(entity);
                session.getTransaction().commit();
                return true;
            } 
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (RuntimeException ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<EntityType> search(List<Criteria> criterias) throws BussinessException {
        Session session = sessionFactory.getCurrentSession();
        try {

            Query query = session.createQuery("SELECT e FROM " + getEntityClass().getName() + " e");
            List<EntityType> entities = query.list();

            return entities;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (RuntimeException ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    public EntityType readByNaturalKey(Object value) throws BussinessException {
        Session session = sessionFactory.getCurrentSession();
        try {
            EntityType entity=(EntityType)session.bySimpleNaturalId(entityType).load(value);

            return entity;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BussinessException(cve);            
        } catch (RuntimeException ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }

    }    
    
    private Class<EntityType> getEntityClass() {
        return entityType;
    }


}
