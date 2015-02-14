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
import es.logongas.ix3.dao.TransactionManager;
import es.logongas.ix3.security.authentication.Principal;
import es.logongas.ix3.service.CRUDService;
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
public class CRUDServiceImpl<EntityType,PrimaryKeyType extends Serializable> implements CRUDService<EntityType,PrimaryKeyType> {
    @Autowired
    protected Principal  principal;
    
    @Autowired 
    protected DAOFactory daoFactory;

    @Autowired
    protected TransactionManager transactionManager;
    
    Class entityType;

    protected final Log log = LogFactory.getLog(getClass());

    public CRUDServiceImpl() {
        entityType = (Class<EntityType>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        
    }

    public CRUDServiceImpl(Class<EntityType> entityType) {
        this.entityType = entityType;
    }    
    
    
    protected GenericDAO<EntityType,PrimaryKeyType> getDAO() {
        return daoFactory.getDAO(entityType);
    }

    
    @Override
    public EntityType create() throws BusinessException {
        return getDAO().create();
    }

    @Override
    public EntityType create(Map<String,Object> initialProperties) throws BusinessException {
        return getDAO().create(initialProperties);
    }

    @Override
    public void insert(EntityType entity) throws BusinessException {
        getDAO().insert(entity);
    }

    @Override
    public EntityType read(PrimaryKeyType primaryKey) throws BusinessException {
        return getDAO().read(primaryKey);
    }

    @Override
    public boolean update(EntityType entity) throws BusinessException {
        return getDAO().update(entity);
    }

    @Override
    public boolean delete(PrimaryKeyType primaryKey) throws BusinessException {
        return getDAO().delete(primaryKey);
    }

    @Override
    public EntityType readByNaturalKey(Object value) throws BusinessException {
        return getDAO().readByNaturalKey(value);
    }

    @Override
    public List search(Map filter) throws BusinessException {
        return getDAO().search(filter);
    }

    @Override
    public List search(Map filter, List orders) throws BusinessException {
        return getDAO().search(filter, orders);
    }

    @Override
    public Page pageableSearch(Map filter, int pageNumber, int pageSize) throws BusinessException {
        return getDAO().pageableSearch(filter, pageNumber, pageSize);
    }

    @Override
    public Page pageableSearch(Map filter, List orders, int pageNumber, int pageSize) throws BusinessException {
        return getDAO().pageableSearch(filter, orders, pageNumber, pageSize);
    }
}
