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
import es.logongas.ix3.core.PageRequest;
import es.logongas.ix3.dao.Filter;
import es.logongas.ix3.dao.FilterOperator;
import es.logongas.ix3.dao.SearchResponse;
import es.logongas.ix3.dao.TransactionManager;
import es.logongas.ix3.dao.metadata.MetaData;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.util.ReflectionUtil;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public class GenericDAOImplHibernate<EntityType, PrimaryKeyType extends Serializable> implements GenericDAO<EntityType, PrimaryKeyType> {

    @Autowired
    protected SessionFactory sessionFactory;

    @Autowired
    protected SessionFactory sessionFactory2;

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

    private MetaData getEntityMetaData() {
        return metaDataFactory.getMetaData(entityType);
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

    
    @Override
    final public EntityType readOriginalByNaturalKey(Object naturalKey) throws BusinessException {
        Session session = sessionFactory2.getCurrentSession();
        try {
            session.setCacheMode(CacheMode.IGNORE);
            this.preReadByNaturalKey(session, naturalKey);
            EntityType entity = (EntityType) session.bySimpleNaturalId(getEntityMetaData().getType()).load(naturalKey);
            if (entity != null) {
                session.evict(entity);
            }
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

    @Override
    final public EntityType readOriginal(PrimaryKeyType id) throws BusinessException {
        Session session = sessionFactory2.getCurrentSession();
        try {
            session.setCacheMode(CacheMode.IGNORE);
            this.preRead(session, id);
            EntityType entity = (EntityType) session.get(getEntityMetaData().getType(), id);
            if (entity != null) {
                session.evict(entity);
            }
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
        return search(filters, new SearchResponse(false));
    }

    @Override
    final public List<EntityType> search(List<Filter> filters, List<Order> orders) throws BusinessException {
        return pageableSearch(filters, orders, null, new SearchResponse(false)).getContent();
    }

    @Override
    public Page<EntityType> pageableSearch(List<Filter> filters, PageRequest pageRequest) throws BusinessException {
        return pageableSearch(filters, null, pageRequest, new SearchResponse(false));
    }

    @Override
    public Page<EntityType> pageableSearch(List<Filter> filters, List<Order> orders, PageRequest pageRequest) throws BusinessException {
        return pageableSearch(filters, orders, pageRequest, new SearchResponse(false));
    }

    @Override
    final public List<EntityType> search(List<Filter> filters, SearchResponse searchResponse) throws BusinessException {
        return search(filters, null, searchResponse);
    }

    @Override
    final public List<EntityType> search(List<Filter> filters, List<Order> orders, SearchResponse searchResponse) throws BusinessException {
        return pageableSearch(filters, orders, null, searchResponse).getContent();
    }

    @Override
    public Page<EntityType> pageableSearch(List<Filter> filters, PageRequest pageRequest, SearchResponse searchResponse) throws BusinessException {
        return pageableSearch(filters, null, pageRequest, searchResponse);
    }

    @Override
    public Page<EntityType> pageableSearch(List<Filter> filters, List<Order> orders, PageRequest pageRequest, SearchResponse searchResponse) throws BusinessException {

        if (orders == null) {
            orders = new ArrayList<Order>();
        }

        Session session = sessionFactory.getCurrentSession();
        try {
            String sqlPartFrom = sqlPartFrom(filters);
            String sqlPartWhere = sqlPartWhere(filters);
            String sqlPartOrderBy = sqlPartOrder(orders);
            String sqlPartSelectObject = sqlPartSelectObject(searchResponse);
            String sqlPartSelectCount = sqlPartSelectCount(searchResponse);

            Page page;
            if (pageRequest == null) {
                Query queryDatos = session.createQuery(sqlPartSelectObject + " " + sqlPartFrom + " " + sqlPartWhere + " " + sqlPartOrderBy);
                setFilterParameters(queryDatos, filters);
                List results = queryDatos.list();

                page = new PageImpl(results, Integer.MAX_VALUE, 0, 1);
            } else {
                Query queryDatos = session.createQuery(sqlPartSelectObject + " " + sqlPartFrom + " " + sqlPartWhere + " " + sqlPartOrderBy);
                queryDatos.setMaxResults(pageRequest.getPageSize());
                queryDatos.setFirstResult(pageRequest.getPageSize() * pageRequest.getPageNumber());
                setFilterParameters(queryDatos, filters);
                List results = queryDatos.list();

                //Vamos ahora a calcular el total de páginas
                Query queryCount = session.createQuery(sqlPartSelectCount + " " + sqlPartFrom + " " + sqlPartWhere);
                setFilterParameters(queryCount, filters);
                Long totalCount = (Long) queryCount.uniqueResult();

                int totalPages;
                if (totalCount == 0) {
                    totalPages = 0;
                } else {
                    totalPages = (int) (Math.ceil(((double) totalCount) / ((double) pageRequest.getPageSize())));
                }

                page = new PageImpl(results, pageRequest.getPageSize(), pageRequest.getPageNumber(), totalPages);
            }

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

    private String sqlPartSelectObject(SearchResponse searchResponse) {
        String select;

        if ((searchResponse != null) && (searchResponse.isDistinct() == true)) {
            select = "SELECT DISTINCT e ";
        } else {
            select = "SELECT e ";
        }

        return select;
    }

    private String sqlPartSelectCount(SearchResponse searchResponse) {
        String select;

        if ((searchResponse != null) && (searchResponse.isDistinct() == true)) {
            select = "SELECT COUNT(DISTINCT e) ";
        } else {
            select = "SELECT COUNT(e) ";
        }

        return select;
    }

    /**
     * Obtener la parte de la SQL relativa al ORDER BY
     *
     * @param orders
     * @return
     */
    private String sqlPartFrom(List<Filter> filters) {
        StringBuilder sbFrom = new StringBuilder();

        sbFrom.append(" FROM " + getEntityMetaData().getType().getSimpleName() + " e ");

        if (filters != null) {
            List<JoinProperty> joinsProperties = getJoinsProperties(filters);
            for (int i = 0; i < joinsProperties.size(); i++) {
                JoinProperty joinProperty = joinsProperties.get(i);

                if ((joinProperty.join != null) && (joinProperty.join.isEmpty() == false)) {
                    sbFrom.append(" JOIN e." + joinProperty.join + " j" + i + " ");
                }

            }
        }

        return sbFrom.toString();
    }

    /**
     * Obtener la parte relativa al WHERE
     *
     * @param filters
     * @return
     */
    private String sqlPartWhere(List<Filter> filters) {

        StringBuilder sqlWhere = new StringBuilder();
        sqlWhere.append(" WHERE 1=1 ");

        if (filters != null) {

            List<JoinProperty> joinsProperties = getJoinsProperties(filters);

            for (int i = 0; i < filters.size(); i++) {
                Filter filter = filters.get(i);
                JoinProperty joinProperty = joinsProperties.get(i);

                Object value = filter.getValue();
                String propertyName;
                if ((joinProperty.join != null) && (joinProperty.join.isEmpty() == false)) {
                    propertyName = "j" + i + "." + joinProperty.property;
                } else {
                    propertyName = "e." + filter.getPropertyName();
                }
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
                } else if (filterOperator == FilterOperator.llike) {
                    sqlWhere.append(" like :bind" + i + "");
                } else if (filterOperator == FilterOperator.liker) {
                    sqlWhere.append(" like :bind" + i + "");
                } else if (filterOperator == FilterOperator.lliker) {
                    sqlWhere.append(" like :bind" + i + "");
                } else {
                    throw new RuntimeException("El nombre del operador no es válido:" + filterOperator);
                }
            }
        }

        return sqlWhere.toString();

    }

    private List<JoinProperty> getJoinsProperties(List<Filter> filters) {
        List<JoinProperty> joinsProperties = new ArrayList<JoinProperty>();

        for (int i = 0; i < filters.size(); i++) {
            Filter filter = filters.get(i);

            joinsProperties.add(getJoinProperty(filter));
        }

        return joinsProperties;
    }

    private JoinProperty getJoinProperty(Filter filter) {
        JoinProperty joinProperty = new JoinProperty();

        String[] propertiesName = filter.getPropertyName().split("\\.");

        MetaData currentMetaData = getEntityMetaData();
        int splitIndex = -1;
        for (int i = 0; i < propertiesName.length; i++) {
            currentMetaData = currentMetaData.getPropertyMetaData(propertiesName[i]);
            if (currentMetaData.isCollection() == true) {

                if (splitIndex != -1) {
                    throw new RuntimeException("No se permite mas de una colección en un filtro where: " + filter.getPropertyName() + " la primera es:" + propertiesName[splitIndex] + " y la segunda es:" + propertiesName[i]);
                }

                splitIndex = i;
            }
        }

        if (splitIndex == -1) {
            joinProperty.join = null;
            joinProperty.property = filter.getPropertyName();
        } else {
            joinProperty.join = StringUtils.collectionToDelimitedString(Arrays.asList(Arrays.copyOfRange(propertiesName, 0, splitIndex + 1)), ".");
            joinProperty.property = StringUtils.collectionToDelimitedString(Arrays.asList(Arrays.copyOfRange(propertiesName, splitIndex + 1, propertiesName.length)), ".");
        }

        return joinProperty;
    }

    /**
     * En una HQL contiene los datos del JOIN del FROM y de las propiedades del
     * WHERE Esto se hace pq es necesario para buscar datos en colecciones
     * http://stackoverflow.com/questions/24750754/org-hibernate-queryexception-illegal-attempt-to-dereference-collection
     */
    private class JoinProperty {

        String join;
        String property;
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
     * Establecer los parámetros de las consultas de Query
     *
     * @param query
     * @param filters
     */
    private void setFilterParameters(Query query, List<Filter> filters) {
        if (filters != null) {
            for (int i = 0; i < filters.size(); i++) {
                Filter filter = filters.get(i);
                Object value = filter.getValue();

                if (filter.getFilterOperator() == FilterOperator.llike) {
                    value = "%" + filter.getValue() + "";
                } else if (filter.getFilterOperator() == FilterOperator.liker) {
                    value = "" + filter.getValue() + "%";
                } else if (filter.getFilterOperator() == FilterOperator.lliker) {
                    value = "%" + filter.getValue() + "%";
                } else {
                    value = filter.getValue();
                }

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

    
    
}
