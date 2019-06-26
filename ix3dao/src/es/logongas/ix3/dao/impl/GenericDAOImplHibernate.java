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
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.dao.Filter;
import es.logongas.ix3.dao.FilterOperator;
import es.logongas.ix3.dao.Filters;
import es.logongas.ix3.dao.SearchResponse;
import es.logongas.ix3.dao.TransactionManager;
import es.logongas.ix3.dao.metadata.MetaData;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.util.ReflectionUtil;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public class GenericDAOImplHibernate<EntityType, PrimaryKeyType extends Serializable> implements GenericDAO<EntityType, PrimaryKeyType> {

    @Autowired
    protected MetaDataFactory metaDataFactory;
    @Autowired
    protected TransactionManager transactionManager;

    @Autowired
    protected ExceptionTranslator exceptionTranslator;

    private Class<EntityType> entityType = null;

    protected final Log log = LogFactory.getLog(getClass());

    public GenericDAOImplHibernate() {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            entityType = (Class<EntityType>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }
    }

    public GenericDAOImplHibernate(Class<EntityType> entityType) {
        this.entityType = entityType;
    }

    @Override
    public void setEntityType(Class<EntityType> entityType) {
        this.entityType = entityType;
    }

    @Override
    public Class<EntityType> getEntityType() {
        return this.entityType;
    }

    private MetaData getEntityMetaData() {
        return metaDataFactory.getMetaData(entityType);
    }

    @Override
    final public EntityType create(DataSession dataSession, Map<String, Object> initialProperties) throws BusinessException {
        try {
            EntityType entity;
            entity = (EntityType) getEntityMetaData().getType().newInstance();
            if (initialProperties != null) {
                for (String key : initialProperties.keySet()) {
                    ReflectionUtil.setValueToBean(entity, key, initialProperties.get(key));
                }
            }
            return entity;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public EntityType insert(DataSession dataSession, EntityType entity) throws BusinessException {
        Session session = (Session) dataSession.getDataBaseSessionImpl();
        boolean isActivePreviousTransaction = transactionManager.isActive(dataSession);
        try {
            if (isActivePreviousTransaction == false) {
                transactionManager.begin(dataSession);
            }
            session.save(entity);
            if (isActivePreviousTransaction == false) {
                transactionManager.commit(dataSession);
            }

            return entity;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive(dataSession) == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback(dataSession);
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive(dataSession) == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback(dataSession);
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (RuntimeException ex) {
            try {
                if ((transactionManager.isActive(dataSession) == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback(dataSession);
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if ((transactionManager.isActive(dataSession) == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback(dataSession);
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public EntityType update(DataSession dataSession, EntityType entity) throws BusinessException {
        Session session = (Session) dataSession.getDataBaseSessionImpl();
        boolean isActivePreviousTransaction = transactionManager.isActive(dataSession);
        try {

            if (isActivePreviousTransaction == false) {
                transactionManager.begin(dataSession);
            }
            session.update(entity);
            if (isActivePreviousTransaction == false) {
                transactionManager.commit(dataSession);
            }

            return entity;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive(dataSession) == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback(dataSession);
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive(dataSession) == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback(dataSession);
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (RuntimeException ex) {
            try {
                if ((transactionManager.isActive(dataSession) == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback(dataSession);
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if ((transactionManager.isActive(dataSession) == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback(dataSession);
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public EntityType read(DataSession dataSession, PrimaryKeyType id) throws BusinessException {
        Session session = (Session) dataSession.getDataBaseSessionImpl();
        try {
            EntityType entity = (EntityType) session.get(getEntityMetaData().getType(), id);
            return entity;
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
    final public EntityType readByNaturalKey(DataSession dataSession, Object naturalKey) throws BusinessException {
        Session session = (Session) dataSession.getDataBaseSessionImpl();
        try {
            EntityType entity = (EntityType) session.bySimpleNaturalId(getEntityMetaData().getType()).load(naturalKey);
            return entity;
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
    final public EntityType readOriginalByNaturalKey(DataSession dataSession, Object naturalKey) throws BusinessException {
        Session session = (Session) dataSession.getDataBaseSessionAlternativeImpl();
        try {
            session.setCacheMode(CacheMode.IGNORE);
            EntityType entity = (EntityType) session.bySimpleNaturalId(getEntityMetaData().getType()).load(naturalKey);
            if (entity != null) {
                session.evict(entity);
            }
            return entity;
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
    final public EntityType readOriginal(DataSession dataSession, PrimaryKeyType id) throws BusinessException {
        Session session = (Session) dataSession.getDataBaseSessionAlternativeImpl();
        try {
            session.setCacheMode(CacheMode.IGNORE);
            EntityType entity = (EntityType) session.get(getEntityMetaData().getType(), id);
            if (entity != null) {
                session.evict(entity);
            }
            return entity;
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
    final public boolean delete(DataSession dataSession, EntityType entity) throws BusinessException {
        Session session = (Session) dataSession.getDataBaseSessionImpl();
        boolean isActivePreviousTransaction = transactionManager.isActive(dataSession);
        boolean exists;
        try {
            if (isActivePreviousTransaction == false) {
                transactionManager.begin(dataSession);
            }
            if (entity == null) {
                exists = false;
                if (isActivePreviousTransaction == false) {
                    transactionManager.commit(dataSession);
                }
            } else {
                session.delete(entity);
                exists = true;
                if (isActivePreviousTransaction == false) {
                    transactionManager.commit(dataSession);
                }
            }

            return exists;
        } catch (javax.validation.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive(dataSession) == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback(dataSession);
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            try {
                if ((transactionManager.isActive(dataSession) == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback(dataSession);
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new BusinessException(exceptionTranslator.getBusinessMessages(cve));
        } catch (RuntimeException ex) {
            try {
                if ((transactionManager.isActive(dataSession) == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback(dataSession);
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw ex;
        } catch (Exception ex) {
            try {
                if ((transactionManager.isActive(dataSession) == true) && (isActivePreviousTransaction == false)) {
                    transactionManager.rollback(dataSession);
                }
            } catch (Exception exc) {
                log.error("Falló al hacer un rollback", exc);
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public List<EntityType> search(DataSession dataSession, Filters filters, List<Order> orders, SearchResponse searchResponse) throws BusinessException {
        return pageableSearch(dataSession, filters, orders, null, searchResponse).getContent();
    }

    @Override
    public Page<EntityType> pageableSearch(DataSession dataSession, Filters filters, List<Order> orders, PageRequest pageRequest, SearchResponse searchResponse) throws BusinessException {

        if (searchResponse == null) {
            searchResponse = new SearchResponse(false);
        }

        if (orders == null) {
            orders = new ArrayList<Order>();
        }

        Session session = (Session) dataSession.getDataBaseSessionImpl();
        try {
            String sqlPartFrom = sqlPartFrom(filters);
            String sqlPartWhere = sqlPartWhere(filters);
            String sqlPartOrderBy = sqlPartOrder(orders);
            String sqlPartSelectObject = sqlPartSelectObject(searchResponse);
            String sqlPartSelectCount = sqlPartSelectCount(searchResponse);

            String sqlData = sqlPartSelectObject + " " + sqlPartFrom + " " + sqlPartWhere + " " + sqlPartOrderBy;
            String sqlCount = sqlPartSelectCount + " " + sqlPartFrom + " " + sqlPartWhere;

            Page page;
            if (pageRequest == null) {
                Query queryDatos = session.createQuery(sqlData);
                setParameters(queryDatos, new HashMap<Object, Object>(getParameterFromFilters(filters)));
                List results = queryDatos.list();

                page = new PageImpl(results, Integer.MAX_VALUE, 0, 1);
            } else {
                page = getPaginatedQuery(dataSession, sqlData, sqlCount, pageRequest, getParameterFromFilters(filters));
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
    private String sqlPartFrom(Filters filters) {
        StringBuilder sbFrom = new StringBuilder();

        sbFrom.append(" FROM " + getEntityMetaData().getType().getSimpleName() + " e ");

        if (filters != null) {
            JoinProperties joinsProperties = getJoinsProperties(filters);
            for (int i = 0; i < joinsProperties.size(); i++) {
                JoinProperty joinProperty = joinsProperties.get(i);

                if ((joinProperty.joinTable != null) && (joinProperty.joinTable.isEmpty() == false) && (joinProperty.repeatJoinTable==false)) {
                    sbFrom.append(" JOIN e." + joinProperty.joinTable + " " + joinProperty.tableAlias);
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
    private String sqlPartWhere(Filters filters) {

        StringBuilder sqlWhere = new StringBuilder();
        sqlWhere.append(" WHERE 1=1 ");

        if (filters != null) {

            JoinProperties joinsProperties = getJoinsProperties(filters);

            for (int i = 0; i < filters.size(); i++) {
                Filter filter = filters.get(i);
                JoinProperty joinProperty = joinsProperties.get(i);

                Object value = filter.getValue();
                String propertyName;
                if ((joinProperty.joinTable != null) && (joinProperty.joinTable.isEmpty() == false)) {
                    propertyName = joinProperty.tableAlias + "." + joinProperty.propertyName;
                } else {
                    propertyName = "e." + filter.getPropertyName();
                }
                FilterOperator filterOperator = filter.getFilterOperator();

                sqlWhere.append(" AND ");
                if (filterOperator == FilterOperator.eq) {
                    if (value instanceof Object[]) {
                        sqlWhere.append(propertyName + " in (:bind" + i + ")");
                    } else if (value instanceof Collection) {
                        sqlWhere.append(propertyName + " in (:bind" + i + ")");
                    } else {
                        sqlWhere.append(propertyName + " = :bind" + i + "");
                    }
                } else if (filterOperator == FilterOperator.ne) {
                    sqlWhere.append(propertyName + " != :bind" + i + "");
                } else if (filterOperator == FilterOperator.gt) {
                    sqlWhere.append(propertyName + " > :bind" + i + "");
                } else if (filterOperator == FilterOperator.ge) {
                    sqlWhere.append(propertyName + " >= :bind" + i + "");
                } else if (filterOperator == FilterOperator.lt) {
                    sqlWhere.append(propertyName + " < :bind" + i + "");
                } else if (filterOperator == FilterOperator.le) {
                    sqlWhere.append(propertyName + " <= :bind" + i + "");
                } else if (filterOperator == FilterOperator.like) {
                    sqlWhere.append(propertyName + " like :bind" + i + "");
                } else if (filterOperator == FilterOperator.llike) {
                    sqlWhere.append(propertyName + " like :bind" + i + "");
                } else if (filterOperator == FilterOperator.liker) {
                    sqlWhere.append(propertyName + " like :bind" + i + "");
                } else if (filterOperator == FilterOperator.lliker) {
                    sqlWhere.append(propertyName + " like :bind" + i + "");
                } else if (filterOperator == FilterOperator.isnull) {
                    if (filter.getValue() == Boolean.TRUE) {
                        sqlWhere.append("(" + propertyName + " IS NULL) ");
                    } else {
                        sqlWhere.append("(" + propertyName + " IS NOT NULL) ");
                    }
                } else if (filterOperator == FilterOperator.deq) {
                    sqlWhere.append(toOnlyDate(propertyName )+ " = " + toOnlyDate(":bind" + i) + "");
                } else if (filterOperator == FilterOperator.dne) {
                    sqlWhere.append(toOnlyDate(propertyName )+ " != " + toOnlyDate(":bind" + i) + "");
                } else if (filterOperator == FilterOperator.dgt) {
                    sqlWhere.append(toOnlyDate(propertyName )+ " > " + toOnlyDate(":bind" + i) + "");
                } else if (filterOperator == FilterOperator.dge) {
                    sqlWhere.append(toOnlyDate(propertyName )+ " >= " + toOnlyDate(":bind" + i) + "");
                } else if (filterOperator == FilterOperator.dlt) {
                    sqlWhere.append(toOnlyDate(propertyName )+ " < " + toOnlyDate(":bind" + i) + "");
                } else if (filterOperator == FilterOperator.dle) {
                    sqlWhere.append(toOnlyDate(propertyName )+ " <= " + toOnlyDate(":bind" + i) + "");
                } else {
                    throw new RuntimeException("El nombre del operador no es válido:" + filterOperator);
                }
            }
        }

        return sqlWhere.toString();

    }

    private String toOnlyDate(String parameter) {
        //Es específico de MySQL
        return "DATE_FORMAT(" + parameter + ", '%Y-%m-%d')";
    }
    
    
    private JoinProperties getJoinsProperties(Filters filters) {
        JoinProperties joinsProperties = new JoinProperties();

        for (int i = 0; i < filters.size(); i++) {
            Filter filter = filters.get(i);

            JoinProperty joinProperty=getJoinProperty(filter);
            JoinProperty previousJoinProperty=joinsProperties.findPropertyByJoinTable(joinProperty.joinTable);
            if (previousJoinProperty!=null) {
                joinProperty.repeatJoinTable=true;
                joinProperty.tableAlias=previousJoinProperty.tableAlias;
            } else {
                joinProperty.repeatJoinTable=false;
                joinProperty.tableAlias="j"+i;
            }
            
            
            joinsProperties.add(joinProperty);
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
            joinProperty.joinTable = null;
            joinProperty.propertyName = filter.getPropertyName();
        } else {
            joinProperty.joinTable = StringUtils.collectionToDelimitedString(Arrays.asList(Arrays.copyOfRange(propertiesName, 0, splitIndex + 1)), ".");
            joinProperty.propertyName = StringUtils.collectionToDelimitedString(Arrays.asList(Arrays.copyOfRange(propertiesName, splitIndex + 1, propertiesName.length)), ".");
        }

        return joinProperty;
    }

    /**
     * En una HQL contiene los datos del JOIN del FROM y de las propiedades del WHERE Esto se hace pq es necesario para buscar datos en colecciones
     * http://stackoverflow.com/questions/24750754/org-hibernate-queryexception-illegal-attempt-to-dereference-collection
     */
    private class JoinProperty {

        boolean repeatJoinTable;
        String tableAlias;
        String joinTable;
        String propertyName;
    }
    
    private class JoinProperties extends ArrayList<JoinProperty> {
    
        
        public JoinProperty findPropertyByJoinTable(String joinTable) {
            
            for(JoinProperty joinProperty:this) {
                if ((joinProperty.joinTable!=null) && (joinProperty.joinTable.equalsIgnoreCase(joinTable))) {
                    return joinProperty;
                }
            }
            
            return null;
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

                sbOrder.append(" e."+order.getFieldName());

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
    private Map<String, Object> getParameterFromFilters(Filters filters) {
        Map<String, Object> namedParameters = new HashMap<String, Object>();

        if (filters != null) {
            for (int i = 0; i < filters.size(); i++) {
                Filter filter = filters.get(i);
                Object value = filter.getValue();

                if (filter.getFilterOperator() == FilterOperator.llike) {
                    value = "%" + filter.getValue() + "";
                    namedParameters.put("bind" + i, value);
                } else if (filter.getFilterOperator() == FilterOperator.liker) {
                    value = "" + filter.getValue() + "%";
                    namedParameters.put("bind" + i, value);
                } else if (filter.getFilterOperator() == FilterOperator.lliker) {
                    value = "%" + filter.getValue() + "%";
                    namedParameters.put("bind" + i, value);
                } else if (filter.getFilterOperator() == FilterOperator.isnull) {
                    //No hay que añadir nada pq este operador no necesita parámetro
                } else {
                    value = filter.getValue();
                    namedParameters.put("bind" + i, value);
                }

            }
        }

        return namedParameters;
    }

    /**
     * Crea una consulta pagina retornando la página que se solicita.
     *
     * @param sqlData La SQL para Obtener los datos.
     * @param sqlCount La SQL para Obtener le Nº total de filas que retornaría sqlData. Se pasa para optimizar.
     * @param pageRequest La página que se solicita.
     * @param parameters La lista de parámetros se establecen por la posición en la lista
     * @return La pagina
     */
    protected Page<EntityType> getPaginatedQuery(DataSession dataSession, String sqlData, String sqlCount, PageRequest pageRequest, List<Object> parameters) {
        Map<Object, Object> indexParameters = new HashMap<Object, Object>();
        for (int i = 0; i < parameters.size(); i++) {
            indexParameters.put(i, parameters.get(i));
        }

        return getGenericPaginatedQuery(dataSession, sqlData, sqlCount, pageRequest, indexParameters);
    }

    /**
     * Crea una consulta pagina retornando la página que se solicita.
     *
     * @param sqlData La SQL para Obtener los datos.
     * @param sqlCount La SQL para Obtener le Nº total de filas que retornaría sqlData. Se pasa para optimizar.
     * @param pageRequest La página que se solicita.
     * @param parameters La lista de parámetros se por el nombre del parámetro de la clave del Map.
     * @return La pagina
     */
    protected Page<EntityType> getPaginatedQuery(DataSession dataSession, String sqlData, String sqlCount, PageRequest pageRequest, Map<String, Object> parameters) {
        Map<Object, Object> namedParameters = new HashMap<Object, Object>();
        namedParameters.putAll(parameters);

        return getGenericPaginatedQuery(dataSession, sqlData, sqlCount, pageRequest, namedParameters);
    }

    /**
     * Crea una consulta pagina retornando la página que se solicita.
     *
     * @param sqlData La SQL para Obtener los datos.
     * @param sqlCount La SQL para Obtener le Nº total de filas que retornaría sqlData. Se pasa para optimizar.
     * @param pageRequest La página que se solicita.
     * @param parameters El map debe ser del tipo Map<String,Object> o Map<Integer,Object>. Y se pondrán los parámetros por nombre o por posición.
     * @return La pagina
     */
    private Page<EntityType> getGenericPaginatedQuery(DataSession dataSession, String sqlData, String sqlCount, PageRequest pageRequest, Map<Object, Object> parameters) {
        Session session = (Session) dataSession.getDataBaseSessionImpl();

        Query queryDatos = session.createQuery(sqlData);
        queryDatos.setMaxResults(pageRequest.getPageSize());
        queryDatos.setFirstResult(pageRequest.getPageSize() * pageRequest.getPageNumber());
        setParameters(queryDatos, parameters);
        List<EntityType> results = (List<EntityType>) queryDatos.list();

        //Vamos ahora a calcular el total de páginas
        Query queryCount = session.createQuery(sqlCount);
        setParameters(queryCount, parameters);
        Long totalCount = (Long) queryCount.uniqueResult();

        int totalPages;
        if (totalCount == 0) {
            totalPages = 0;
        } else {
            totalPages = (int) (Math.ceil(((double) totalCount) / ((double) pageRequest.getPageSize())));
        }

        Page<EntityType> page = new PageImpl<EntityType>(results, pageRequest.getPageSize(), pageRequest.getPageNumber(), totalPages);

        return page;
    }

    /**
     * Pone los parámetros en una Query
     *
     * @param query La Query a la que se le pone los parámetros.
     * @param parameters El map debe ser del tipo Map<String,Object> o Map<Integer,Object>. Y se pondrán los parámetros por nombre o por posición.
     */
    private void setParameters(Query query, Map<Object, Object> parameters) {
        if (parameters != null) {
            for (Map.Entry<Object, Object> entry : parameters.entrySet()) {
                Object value = entry.getValue();
                Object parameterKey = entry.getKey();

                if (parameterKey == null) {
                    throw new NullPointerException("El nombre de un parámetro no puede ser null");
                }

                if (parameterKey instanceof Number) {
                    Number parameterIndex = (Number) parameterKey;
                    query.setParameter(parameterIndex.intValue(), value);
                } else if (parameterKey instanceof String) {
                    String parameterName = (String) parameterKey;
                    if (value instanceof Object[]) {
                        query.setParameterList(parameterName, (Object[]) value);
                    } else if (value instanceof Collection) {
                        query.setParameterList(parameterName, (Collection) value);
                    } else {
                        query.setParameter(parameterName, value);
                    }
                } else {
                    throw new RuntimeException("La clave debe ser de tipo String o Number");
                }
            }
        }
    }

}
