/*
 * Copyright 2014 Lorenzo.
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


package es.logongas.ix3.service.impl;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.DAOFactory;
import es.logongas.ix3.dao.GenericDAO;
import es.logongas.ix3.core.Page;
import es.logongas.ix3.security.services.authentication.Principal;
import es.logongas.ix3.service.GenericService;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Lorenzo
 * @param <EntityType>
 * @param <PrimaryKeyType> 
 */
public class GenericServiceImpl<EntityType,PrimaryKeyType extends Serializable> implements GenericService {
    @Autowired
    Principal  principal;
    
    @Autowired 
    DAOFactory daoFactory;

    Class entityType;
    GenericDAO genericDAO=null;

    protected final Log log = LogFactory.getLog(getClass());

    public GenericServiceImpl() {
        entityType = (Class<EntityType>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        
    }

    public GenericServiceImpl(Class<EntityType> entityType) {
        this.entityType = entityType;
    }    
    
    
    private GenericDAO getDAO() {
        if (genericDAO==null) {
            genericDAO=daoFactory.getDAO(entityType);
        }
        return genericDAO;
    }

    
    @Override
    final public Object create() throws BusinessException {
        return getDAO().create();
    }

    @Override
    final public Object create(Map initialProperties) throws BusinessException {
        return getDAO().create(initialProperties);
    }

    @Override
    final public void insert(Object entity) throws BusinessException {
        getDAO().insert(entity);
    }

    @Override
    final public Object read(Serializable primaryKey) throws BusinessException {
        return getDAO().read(primaryKey);
    }

    @Override
    final public boolean update(Object entity) throws BusinessException {
        return getDAO().update(entity);
    }

    @Override
    final public boolean delete(Serializable primaryKey) throws BusinessException {
        return getDAO().delete(primaryKey);
    }

    @Override
    final public Object readByNaturalKey(Object value) throws BusinessException {
        return getDAO().readByNaturalKey(value);
    }

    @Override
    final public List search(Map filter) throws BusinessException {
        return getDAO().search(filter);
    }

    @Override
    final public List search(Map filter, List orders) throws BusinessException {
        return getDAO().search(filter, orders);
    }

    @Override
    public Object namedSearch(String namedSearch, Map filter) throws BusinessException {
        return getDAO().namedSearch(namedSearch, filter);
    }

    @Override
    final public Page pageableSearch(Map filter, int pageNumber, int pageSize) throws BusinessException {
        return getDAO().pageableSearch(filter, pageNumber, pageSize);
    }

    @Override
    final public Page pageableSearch(Map filter, List orders, int pageNumber, int pageSize) throws BusinessException {
        return getDAO().pageableSearch(filter, orders, pageNumber, pageSize);
    }
}
