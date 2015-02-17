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
package es.logongas.ix3.dao.impl;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.GenericDAO;
import es.logongas.ix3.core.Order;
import es.logongas.ix3.core.Page;
import es.logongas.ix3.dao.Filter;
import es.logongas.ix3.dao.FilterOperator;
import es.logongas.ix3.dao.TransactionManager;
import es.logongas.ix3.dao.metadata.MetaData;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.util.ReflectionUtil;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.transform.ResultTransformer;
import org.springframework.beans.factory.annotation.Autowired;

public class GenericDAOImplHibernate<EntityType, PrimaryKeyType extends Serializable> implements GenericDAO<EntityType, PrimaryKeyType> {

    @Autowired
    protected SessionFactory sessionFactory;
    @Autowired
    protected MetaDataFactory metaDataFactory;
    @Autowired
    protected TransactionManager transactionManager;

    @Autowired
    protected ExceptionTranslator exceptionTranslator;

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
        return create(null);
    }

    @Override
    final public EntityType create(Map<String, Object> initialProperties) throws BusinessException {
        Session session = sessionFactory.getCurrentSession();

        try {
            EntityType entity;
            entity = (EntityType) getEntityMetaData().getType().newInstance();
            if (initialProperties != null) {
                for (String key : initialProperties.keySet()) {
                    ReflectionUtil.setValueToBean(entity, key, initialProperties.get(key));
                }
            }
            this.postCreate(session, entity);
            return entity;
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public void insert(EntityType entity) throws BusinessException {
        Session session = sessionFactory.getCurrentSession();
        boolean isActivePreviousTransaction = transactionManager.isActive();
        try {
            this.preInsertBeforeTransaction(session, entity);
            if (isActivePreviousTransaction == false) {
                transactionManager.begin();
            }
            this.preInsertInTransaction(session, entity);
            session.save(entity);
            this.postInsertInTransaction(session, entity);
            if (isActivePreviousTransaction == false) {
                transactionManager.commit();
            }
            this.postInsertAfterTransaction(session, entity);
        } catch (BusinessException ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (RuntimeException ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
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
        MetaData metaData = metaDataFactory.getMetaData(entity);
        boolean isActivePreviousTransaction = transactionManager.isActive();
        boolean hasUpdate;
        try {

            String idName = metaData.getPrimaryKeyPropertyName();
            Serializable id = (Serializable) ReflectionUtil.getValueFromBean(entity, idName);
            EntityType entity2;
            if (id == null) {
                entity2 = null;
            } else {
                entity2 = (EntityType) session.get(getEntityMetaData().getType(), id);
            }

            if (entity2 == null) {
                this.preInsertBeforeTransaction(session, entity);
                if (isActivePreviousTransaction == false) {
                    transactionManager.begin();
                }
                this.preInsertInTransaction(session, entity);
                session.save(entity);
                this.postInsertInTransaction(session, entity);
                if (isActivePreviousTransaction == false) {
                    transactionManager.commit();
                }
                this.postInsertAfterTransaction(session, entity);
                hasUpdate = false;
            } else {
                this.preUpdateBeforeTransaction(session, entity);
                if (isActivePreviousTransaction == false) {
                    transactionManager.begin();
                }
                this.preUpdateInTransaction(session, entity);
                session.evict(entity2);
                session.update(entity);
                this.postUpdateInTransaction(session, entity);
                if (isActivePreviousTransaction == false) {
                    transactionManager.commit();
                }
                this.postUpdateAfterTransaction(session, entity);
                hasUpdate = true;
            }
            return hasUpdate;
        } catch (BusinessException ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (RuntimeException ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
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
            this.preRead(session, id);
            EntityType entity = (EntityType) session.get(getEntityMetaData().getType(), id);
            this.postRead(session, id, entity);
            return entity;
        } catch (BusinessException ex) {
            throw ex;
        } catch (javax.validation.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public boolean delete(PrimaryKeyType id) throws BusinessException {
        Session session = sessionFactory.getCurrentSession();
        boolean isActivePreviousTransaction = transactionManager.isActive();
        boolean exists;
        EntityType entity = null;
        try {
            this.preDeleteBeforeTransaction(session, id);
            if (isActivePreviousTransaction == false) {
                transactionManager.begin();
            }
            entity = (EntityType) session.get(getEntityMetaData().getType(), id);
            this.preDeleteInTransaction(session, id, entity);
            if (entity == null) {
                exists = false;
                this.postDeleteInTransaction(session, id, entity);
                if (isActivePreviousTransaction == false) {
                    transactionManager.commit();
                }
            } else {
                session.delete(entity);
                exists = true;
                this.postDeleteInTransaction(session, id, entity);
                if (isActivePreviousTransaction == false) {
                    transactionManager.commit();
                }
            }

            this.postDeleteAfterTransaction(session, id, entity);
            return exists;
        } catch (BusinessException ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (RuntimeException ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if ((transactionManager.isActive() == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback();
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public List<EntityType> search(List<Filter> filters) throws BusinessException {
        return search(filters, null);
    }

    @Override
    final public List<EntityType> search(List<Filter> filters, List<Order> orders) throws BusinessException {
        return pageableSearch(filters, orders, 0, Integer.MAX_VALUE).getContent();
    }

    @Override
    public Page<EntityType> pageableSearch(List<Filter> filters, int pageNumber, int pageSize) throws BusinessException {
        return pageableSearch(filters, null, pageNumber, pageSize);
    }

    @Override
    public Page<EntityType> pageableSearch(List<Filter> filters, List<Order> orders, int pageNumber, int pageSize) throws BusinessException {

        if (pageNumber < 0) {
            throw new RuntimeException("El agumento pageNumber no pude ser negativo");
        }
        if (pageSize < 1) {
            throw new RuntimeException("El agumento pageNumber debe ser mayor que 0");
        }
        if (orders == null) {
            orders = new ArrayList<Order>();
        }

        Session session = sessionFactory.getCurrentSession();
        try {
            String sqlPartFrom = " FROM " + getEntityMetaData().getType().getSimpleName() + " e ";
            String sqlPartWhere=sqlPartWhere(filters);
            String sqlPartOrderBy = sqlPartOrder(orders);

            List results;
            int totalPages;
            if ((pageSize == Integer.MAX_VALUE) && (pageNumber == 0)) {
                //Si el tamaño de página es tan gande (el máximo), seguro que no hace falta paginar
                Query queryDatos = session.createQuery("SELECT  e " + sqlPartFrom + " " + sqlPartWhere + " " + sqlPartOrderBy);
                setFilterParameters(queryDatos, filters);
                results = queryDatos.list();
                
                totalPages = 1;
            } else {
                Query queryDatos = session.createQuery("SELECT  e " + sqlPartFrom + " " + sqlPartWhere + " " + sqlPartOrderBy);
                queryDatos.setMaxResults(pageSize);
                queryDatos.setFirstResult(pageSize * pageNumber);
                setFilterParameters(queryDatos, filters);
                results = queryDatos.list();

                //Vamos ahora a calcular el total de páginas
                Query queryCount = session.createQuery("SELECT  COUNT(e) " + sqlPartFrom + " " + sqlPartWhere.toString());
                setFilterParameters(queryCount, filters);
                Long totalCount = (Long) queryCount.uniqueResult();

                if (totalCount == 0) {
                    totalPages = 0;
                } else {
                    totalPages = (int) (Math.ceil(((double) totalCount) / ((double) pageSize)));
                }
            }

            Page page = new PageImpl(results, pageSize, pageNumber, totalPages);

            return page;
        } catch (javax.validation.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    final public EntityType readByNaturalKey(Object naturalKey) throws BusinessException {
        Session session = sessionFactory.getCurrentSession();
        try {

            this.preReadByNaturalKey(session, naturalKey);
            EntityType entity = (EntityType) session.bySimpleNaturalId(getEntityMetaData().getType()).load(naturalKey);
            this.postReadByNaturalKey(session, naturalKey, entity);
            return entity;
        } catch (BusinessException ex) {
            throw ex;
        } catch (javax.validation.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private MetaData getEntityMetaData() {
        return metaDataFactory.getMetaData(entityType);
    }

    protected void postCreate(Session session, EntityType entity) throws BusinessException {
    }

    protected void preInsertBeforeTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void preInsertInTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void postInsertInTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void postInsertAfterTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void preRead(Session session, PrimaryKeyType id) throws BusinessException {
    }

    protected void postRead(Session session, PrimaryKeyType id, EntityType entity) throws BusinessException {
    }

    protected void preReadByNaturalKey(Session session, Object naturalKey) throws BusinessException {
    }

    protected void postReadByNaturalKey(Session session, Object naturalKey, EntityType entity) throws BusinessException {
    }

    protected void preUpdateBeforeTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void preUpdateInTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void postUpdateInTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void postUpdateAfterTransaction(Session session, EntityType entity) throws BusinessException {
    }

    protected void preDeleteBeforeTransaction(Session session, PrimaryKeyType id) throws BusinessException {
    }

    protected void preDeleteInTransaction(Session session, PrimaryKeyType id, EntityType entity) throws BusinessException {
    }

    protected void postDeleteInTransaction(Session session, PrimaryKeyType id, EntityType entity) throws BusinessException {
    }

    protected void postDeleteAfterTransaction(Session session, PrimaryKeyType id, EntityType entity) throws BusinessException {
    }

    /**
     * Establecer los parámetros de las consultas de Query
     *
     * @param query
     * @param filters
     */
    private void setFilterParameters(Query query, List<Filter> filters) {
        if (filters != null) {
            for (int i = 0; i < filters.size(); i++) {
                Object value = filters.get(i).getValue();
                if (value instanceof Object[]) {
                    query.setParameterList("bind" + i, (Object[]) value);
                } else if (value instanceof Collection) {
                    query.setParameterList("bind" + i, (Collection) value);
                } else {
                    query.setParameter("bind" + i, value);
                }
            }
        }
    }

    /**
     * Obtener la parte de la SQL relativa al ORDER BY
     *
     * @param orders
     * @return
     */
    private String sqlPartOrder(List<Order> orders) {
        StringBuilder sbOrder = new StringBuilder();
        if ((orders != null) && (orders.size() > 0)) {
            sbOrder.append(" ORDER BY ");
            for (int i = 0; i < orders.size(); i++) {
                Order order = orders.get(i);

                if (i > 0) {
                    sbOrder.append(",");
                }

                sbOrder.append(order.getFieldName());

                switch (order.getOrderDirection()) {
                    case Ascending:
                        sbOrder.append(" ASC ");
                        break;
                    case Descending:
                        sbOrder.append(" DESC ");
                        break;
                    default:
                        throw new RuntimeException("orderField.getOrder() desconocido" + order.getOrderDirection());
                }

            }
        }

        return sbOrder.toString();
    }

    /**
     * Obtener la parte relativa al WHERE
     * @param filters
     * @return 
     */
    private String sqlPartWhere(List<Filter> filters) {

        StringBuilder sqlWhere = new StringBuilder();
        sqlWhere.append(" WHERE 1=1 ");

        if (filters != null) {
            for (int i = 0; i < filters.size(); i++) {
                Filter filter = filters.get(i);
                Object value = filter.getValue();
                String propertyName = filter.getPropertyName();
                FilterOperator filterOperator = filter.getFilterOperator();

                sqlWhere.append(" AND " + propertyName + " ");
                if (filterOperator == FilterOperator.eq) {
                    if (value instanceof Object[]) {
                        sqlWhere.append(" in (:bind" + i + ")");
                    } else if (value instanceof Collection) {
                        sqlWhere.append(" in (:bind" + i + ")");
                    } else {
                        sqlWhere.append(" = :bind" + i + "");
                    }
                } else if (filterOperator == FilterOperator.ne) {
                    sqlWhere.append(" != :bind" + i + "");
                } else if (filterOperator == FilterOperator.gt) {
                    sqlWhere.append(" > :bind" + i + "");
                } else if (filterOperator == FilterOperator.ge) {
                    sqlWhere.append(" >= :bind" + i + "");
                } else if (filterOperator == FilterOperator.lt) {
                    sqlWhere.append(" < :bind" + i + "");
                } else if (filterOperator == FilterOperator.le) {
                    sqlWhere.append(" <= :bind" + i + "");
                } else if (filterOperator == FilterOperator.like) {
                    sqlWhere.append(" like :bind" + i + "");
                } else {
                    throw new RuntimeException("El nombre del operador no es válido:" + filterOperator);
                }
            }
        }

        return sqlWhere.toString();

    }

}
