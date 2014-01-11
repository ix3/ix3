/*
 * Copyright 2012 Lorenzo González.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.logongas.ix3.persistence.impl.hibernate.dao;

import es.logongas.ix3.persistence.services.dao.BusinessException;
import es.logongas.ix3.persistence.services.dao.BusinessMessage;
import es.logongas.ix3.persistence.services.dao.GenericDAO;
import es.logongas.ix3.persistence.services.dao.NamedSearch;
import es.logongas.ix3.persistence.services.dao.Order;
import es.logongas.ix3.persistence.services.metadata.MetaData;
import es.logongas.ix3.persistence.services.metadata.MetaDataFactory;
import es.logongas.ix3.util.ReflectionUtil;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;

public class GenericDAOImplHibernate<EntityType, PrimaryKeyType extends Serializable> implements GenericDAO<EntityType, PrimaryKeyType> {

    @Autowired
    protected SessionFactory sessionFactory;
    @Autowired
    protected MetaDataFactory metaDataFactory;

    Class entityType;

    protected final Log log = LogFactory.getLog(getClass());

    public GenericDAOImplHibernate() {
        entityType = (Class<EntityType>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public GenericDAOImplHibernate(Class<EntityType> entityType) {
        this.entityType = entityType;
    }

    @Override
    final public EntityType create() throws BusinessException {
        Session session = sessionFactory.getCurrentSession();

        try {
            EntityType entity;
            entity = (EntityType) getEntityMetaData().getType().newInstance();
            this.postCreate(session, entity);
            return entity;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public void insert(EntityType entity) throws BusinessException {
        Session session = sessionFactory.getCurrentSession();
        try {
            this.preInsertBeforeTransaction(session, entity);
            session.beginTransaction();
            this.preInsertInTransaction(session, entity);
            session.save(entity);
            this.postInsertInTransaction(session, entity);
            session.getTransaction().commit();
            this.postInsertAfterTransaction(session, entity);
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(cve);
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(cve);
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
    final public boolean update(EntityType entity) throws BusinessException {
        Session session = sessionFactory.getCurrentSession();
        boolean exists;
        try {
            this.preUpdateBeforeTransaction(session, entity);
            session.beginTransaction();

            EntityType entity2 = (EntityType) session.get(getEntityMetaData().getType(), session.getIdentifier(entity));

            if (entity == null) {
                exists = false;
                this.preUpdateInTransaction(session, entity, exists);
                this.postUpdateInTransaction(session, entity, exists);
                session.getTransaction().commit();
            } else {
                exists = true;
                this.preUpdateInTransaction(session, entity, exists);
                session.evict(entity2);
                session.update(entity);
                this.postUpdateInTransaction(session, entity, exists);
                session.getTransaction().commit();

            }
            this.postUpdateAfterTransaction(session, entity, exists);
            return exists;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(cve);
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(cve);
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
    final public EntityType read(PrimaryKeyType id) throws BusinessException {
        Session session = sessionFactory.getCurrentSession();
        try {
            this.preReadBeforeTransaction(session, id);
            session.beginTransaction();
            this.preReadInTransaction(session, id);
            EntityType entity = (EntityType) session.get(getEntityMetaData().getType(), id);
            this.postReadInTransaction(session, id, entity);
            session.getTransaction().commit();
            this.postReadAfterTransaction(session, id, entity);
            return entity;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(cve);
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(cve);
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
    final public boolean delete(PrimaryKeyType id) throws BusinessException {
        Session session = sessionFactory.getCurrentSession();
        boolean exists;
        EntityType entity = null;
        try {
            this.preDeleteBeforeTransaction(session, id);
            session.beginTransaction();
            entity = (EntityType) session.get(getEntityMetaData().getType(), id);
            this.preDeleteInTransaction(session, id, entity);
            if (entity == null) {
                exists = false;
                this.postDeleteInTransaction(session, id, entity);
                session.getTransaction().commit();
            } else {
                session.delete(entity);
                exists = true;
                this.postDeleteInTransaction(session, id, entity);
                session.getTransaction().commit();
            }

            this.postDeleteAfterTransaction(session, id, entity);
            return exists;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(cve);
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(cve);
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
    final public List<EntityType> search(Map<String, Object> filter) throws BusinessException {
        return search(filter, null);
    }

    @Override
    final public List<EntityType> search(Map<String, Object> filter, List<Order> orders) throws BusinessException {

        if (orders == null) {
            orders = new ArrayList<Order>();
        }

        Session session = sessionFactory.getCurrentSession();
        try {
            Criteria criteria = session.createCriteria(getEntityMetaData().getType());
            if (filter != null) {
                for (String propertyName : filter.keySet()) {
                    Object value = filter.get(propertyName);

                    if (getEntityMetaData().getPropertiesMetaData().get(propertyName).getType().isAssignableFrom(String.class)) {
                        if ((value != null) && (((String) value).trim().equals("") == false)) {
                            criteria.add(Restrictions.like(propertyName, "%" + value + "%"));
                        }
                    } else {
                        if (value != null) {
                            criteria.add(Restrictions.eq(propertyName, value));
                        }
                    }
                }
            }

            if (orders != null) {
                for (Order order : orders) {
                    org.hibernate.criterion.Order criteriaOrder;

                    switch (order.getOrderDirection()) {
                        case Ascending:
                            criteriaOrder = org.hibernate.criterion.Order.asc(order.getFieldName());
                            break;
                        case Descending:
                            criteriaOrder = org.hibernate.criterion.Order.desc(order.getFieldName());
                            break;
                        default:
                            throw new RuntimeException("orderField.getOrder() desconocido" + order.getOrderDirection());
                    }
                    criteria.addOrder(criteriaOrder);
                }
            }

            List<EntityType> entities = criteria.list();

            return entities;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(cve);
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(cve);
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
    final public Object namedSearch(String namedSearch, Map<String, Object> filter) throws BusinessException {
        try {
            if (filter == null) {
                filter = new HashMap<String, Object>();
            }

            Method method = ReflectionUtil.getMethod(this.getClass(), namedSearch);
            if (method == null) {
                throw new BusinessException(new BusinessMessage(null, "No existe el método " + namedSearch + " en la clase DAO: " + this.getClass().getName()));
            }

            NamedSearch namedSearchAnnotation = ReflectionUtil.getAnnotation(this.getClass(), namedSearch, NamedSearch.class);
            if (namedSearchAnnotation == null) {
                throw new RuntimeException("No es posible llamar al método '" + this.getClass().getName() + "." + namedSearch + "' si no contiene la anotacion NamedSearch");
            }

            String[] parameterNames = namedSearchAnnotation.parameterNames();
            if ((parameterNames == null) && (method.getParameterTypes().length > 0)) {
                throw new RuntimeException("Es necesario la lista de nombre de parametros para la anotación NameSearch del método:" + this.getClass().getName() + "." + namedSearch);
            }

            if (method.getParameterTypes().length != parameterNames.length) {
                throw new RuntimeException("La lista de nombre de parametros para la anotación NameSearch debe coincidir con el nº de parámetro del método: " + this.getClass().getName() + "." + namedSearch);
            }

            List args = new ArrayList();
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                Object parameterValue = filter.get(parameterNames[i]);

                args.add(parameterValue);
            }

            Object result = method.invoke(this, args.toArray());

            return result;

        } catch (RuntimeException ex) {
            throw ex;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public EntityType readByNaturalKey(Object naturalKey) throws BusinessException {
        Session session = sessionFactory.getCurrentSession();
        try {

            this.preReadByNaturalKeyBeforeTransaction(session, naturalKey);
            session.beginTransaction();
            this.preReadByNaturalKeyInTransaction(session, naturalKey);
            EntityType entity = (EntityType) session.bySimpleNaturalId(getEntityMetaData().getType()).load(naturalKey);
            this.postReadByNaturalKeyInTransaction(session, naturalKey, entity);
            session.getTransaction().commit();
            this.postReadByNaturalKeyAfterTransaction(session, naturalKey, entity);
            return entity;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(cve);
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if (session.getTransaction().isActive()) {
                    session.getTransaction().rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(cve);
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

    private MetaData getEntityMetaData() {
        return metaDataFactory.getMetaData(entityType);
    }

    protected void postCreate(Session session, EntityType entity) {
    }

    
    
    protected void preInsertBeforeTransaction(Session session, EntityType entity) {
    }

    protected void preInsertInTransaction(Session session, EntityType entity) {
    }

    protected void postInsertInTransaction(Session session, EntityType entity) {
    }

    protected void postInsertAfterTransaction(Session session, EntityType entity) {
    }

    
    
    protected void preReadBeforeTransaction(Session session, PrimaryKeyType id) {
    }

    protected void preReadInTransaction(Session session, PrimaryKeyType id) {
    }

    protected void postReadInTransaction(Session session, PrimaryKeyType id, EntityType entity) {
    }

    protected void postReadAfterTransaction(Session session, PrimaryKeyType id, EntityType entity) {
    }

    
    
    protected void preReadByNaturalKeyBeforeTransaction(Session session, Object naturalKey) {
    }

    protected void preReadByNaturalKeyInTransaction(Session session, Object naturalKey) {
    }

    protected void postReadByNaturalKeyInTransaction(Session session, Object naturalKey, EntityType entity) {
    }

    protected void postReadByNaturalKeyAfterTransaction(Session session, Object naturalKey, EntityType entity) {
    }

    
    
    protected void preUpdateBeforeTransaction(Session session, EntityType entity) {
    }

    protected void preUpdateInTransaction(Session session, EntityType entity, boolean exists) {
    }

    protected void postUpdateInTransaction(Session session, EntityType entity, boolean exists) {
    }

    protected void postUpdateAfterTransaction(Session session, EntityType entity, boolean exists) {
    }

    
    
    protected void preDeleteBeforeTransaction(Session session, PrimaryKeyType id) {
    }

    protected void preDeleteInTransaction(Session session, PrimaryKeyType id, EntityType entity) {
    }

    protected void postDeleteInTransaction(Session session, PrimaryKeyType id, EntityType entity) {
    }

    protected void postDeleteAfterTransaction(Session session, PrimaryKeyType id, EntityType entity) {
    }

}
