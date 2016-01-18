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
import es.logongas.ix3.core.Order;
import es.logongas.ix3.dao.DAOFactory;
import es.logongas.ix3.dao.GenericDAO;
import es.logongas.ix3.core.Page;
import es.logongas.ix3.core.PageRequest;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.dao.Filter;
import es.logongas.ix3.dao.SearchResponse;
import es.logongas.ix3.dao.TransactionManager;
import es.logongas.ix3.dao.metadata.MetaData;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.rule.RuleContext;
import es.logongas.ix3.rule.RuleEngine;
import es.logongas.ix3.rule.RuleEngineFactory;
import es.logongas.ix3.rule.RuleGroupPredefined;
import es.logongas.ix3.rule.impl.RuleContextImpl;
import es.logongas.ix3.rule.impl.RuleContextImplLazy;
import es.logongas.ix3.service.CRUDService;
import es.logongas.ix3.util.ReflectionUtil;
import es.logongas.ix3.util.UnckeckException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
public class CRUDServiceImpl<EntityType, PrimaryKeyType extends Serializable> implements CRUDService<EntityType, PrimaryKeyType> {

    @Autowired protected DAOFactory daoFactory;
    @Autowired protected TransactionManager transactionManager;
    @Autowired RuleEngineFactory ruleEngineFactory;
    @Autowired MetaDataFactory metaDataFactory;

    Class entityType;

    protected final Log log = LogFactory.getLog(getClass());

    public CRUDServiceImpl() {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            entityType = (Class<EntityType>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }
    }

    public CRUDServiceImpl(Class<EntityType> entityType) {
        this.entityType = entityType;
    }

    protected GenericDAO<EntityType, PrimaryKeyType> getDAO() {
        return daoFactory.getDAO(entityType);
    }

    @Override
    public void setEntityType(Class<EntityType> entityType) {
        this.entityType = entityType;
    }

    @Override
    public Class<EntityType> getEntityType() {
        return this.entityType;
    }

    @Override
    public EntityType create(DataSession dataSession, Map<String, Object> initialProperties) throws BusinessException {
        EntityType entity = getDAO().create(dataSession, initialProperties);
        return entity;
    }

    @Override
    public EntityType read(DataSession dataSession, PrimaryKeyType primaryKey) throws BusinessException {

        EntityType entity = getDAO().read(dataSession, primaryKey);

        RuleContext ruleContext = new RuleContextImpl(entity, null, null);
        fireRules(ruleContext, RuleGroupPredefined.PostRead.class);
        
        return entity;
    }

    @Override
    public EntityType readOriginal(DataSession dataSession, PrimaryKeyType primaryKey) throws BusinessException {

        EntityType entity = getDAO().readOriginal(dataSession, primaryKey);

        RuleContext ruleContext = new RuleContextImpl(entity, null, null);
        fireRules(ruleContext, RuleGroupPredefined.PostRead.class);
        
        return entity;
    }

    @Override
    public EntityType readByNaturalKey(DataSession dataSession, Object value) throws BusinessException {

        EntityType entity = getDAO().readByNaturalKey(dataSession, value);

        RuleContext ruleContext = new RuleContextImpl(entity, null, null);
        fireRules(ruleContext, RuleGroupPredefined.PostRead.class);
        
        return entity;
    }

    @Override
    public EntityType readOriginalByNaturalKey(DataSession dataSession, Object value) throws BusinessException {
        EntityType entity = getDAO().readOriginalByNaturalKey(dataSession, value);
        
        RuleContext ruleContext = new RuleContextImpl(entity, null, null);
        fireRules(ruleContext, RuleGroupPredefined.PostRead.class);
        
        return entity;
    }

    @Override
    public EntityType insert(final DataSession dataSession, EntityType entity) throws BusinessException {
        RuleContext ruleContext = new RuleContextImplLazy(entity, null, new OriginalEntityCallbackImpl(dataSession));
        
        fireRules(ruleContext, RuleGroupPredefined.PreInsert.class, RuleGroupPredefined.PreInsertOrUpdate.class, RuleGroupPredefined.PreInsertOrUpdateOrDelete.class);

        EntityType resultEntity = getDAO().insert(dataSession, entity);
       
        fireRules(ruleContext, RuleGroupPredefined.PostInsert.class, RuleGroupPredefined.PostInsertOrUpdate.class, RuleGroupPredefined.PostInsertOrUpdateOrDelete.class);

        return resultEntity;
    }

    @Override
    public EntityType update(DataSession dataSession, EntityType entity) throws BusinessException {
        RuleContext ruleContext = new RuleContextImplLazy(entity, null, new OriginalEntityCallbackImpl(dataSession));
        
        fireRules(ruleContext, RuleGroupPredefined.PreUpdate.class, RuleGroupPredefined.PreInsertOrUpdate.class, RuleGroupPredefined.PreUpdateOrDelete.class, RuleGroupPredefined.PreInsertOrUpdateOrDelete.class);

        EntityType resultEntity = getDAO().update(dataSession, entity);

        fireRules(ruleContext, RuleGroupPredefined.PostUpdate.class, RuleGroupPredefined.PostInsertOrUpdate.class, RuleGroupPredefined.PostUpdateOrDelete.class, RuleGroupPredefined.PostInsertOrUpdateOrDelete.class);

        return resultEntity;
    }

    @Override
    public boolean delete(DataSession dataSession, EntityType entity) throws BusinessException {
        RuleContext ruleContext = new RuleContextImplLazy(entity, null, new OriginalEntityCallbackImpl(dataSession));
        
        fireRules(ruleContext, RuleGroupPredefined.PreDelete.class, RuleGroupPredefined.PreInsertOrUpdateOrDelete.class, RuleGroupPredefined.PreUpdateOrDelete.class);

        boolean success = getDAO().delete(dataSession, entity);

        fireRules(ruleContext, RuleGroupPredefined.PostDelete.class, RuleGroupPredefined.PostInsertOrUpdateOrDelete.class, RuleGroupPredefined.PostUpdateOrDelete.class);

        return success;
    }

    @Override
    public List<EntityType> search(DataSession dataSession, List<Filter> filters, List<Order> orders, SearchResponse searchResponse) throws BusinessException {
        List<EntityType> entities = getDAO().search(dataSession, filters, orders, searchResponse);
        
        RuleEngine ruleEngine = ruleEngineFactory.getRuleEngine(this.entityType);
        
        if (entities!=null) {
            for(EntityType entity:entities) {
                RuleContext ruleContext = new RuleContextImpl(entity, null, null);
                fireRules(ruleEngine,ruleContext, RuleGroupPredefined.PostRead.class);      
            }
        }
        
        return entities;
    }

    @Override
    public Page<EntityType> pageableSearch(DataSession dataSession, List<Filter> filters, List<Order> orders, PageRequest pageRequest, SearchResponse searchResponse) throws BusinessException {
        Page<EntityType> page = getDAO().pageableSearch(dataSession, filters, orders, pageRequest, searchResponse);
        
        RuleEngine ruleEngine = ruleEngineFactory.getRuleEngine(this.entityType);
        
        if ((page!=null) && (page.getContent()!=null)) {
            for(EntityType entity:page.getContent()) {
                RuleContext ruleContext = new RuleContextImpl(entity, null, null);
                fireRules(ruleEngine,ruleContext, RuleGroupPredefined.PostRead.class);      
            }
        }
        
        return page;
    }

    private void fireRules(RuleContext ruleContext, Class<?>... groups) throws BusinessException {
        RuleEngine ruleEngine = ruleEngineFactory.getRuleEngine(this.entityType);
        fireRules(ruleEngine, ruleContext, groups);
    }

    private void fireRules(RuleEngine ruleEngine, RuleContext ruleContext, Class<?>... groups) throws BusinessException {
        ruleEngine.fireConstraintRules(this, ruleContext, groups);
        ruleEngine.fireActionRules(this, ruleContext, groups);
    }

    
    /**
     * Permite obtener el valor original de la entidad
     */
    private class OriginalEntityCallbackImpl implements RuleContextImplLazy.OriginalEntityCallback<EntityType> {

        private DataSession dataSession;

        public OriginalEntityCallbackImpl(DataSession dataSession) {
            this.dataSession = dataSession;
        }
        
        @Override
        public EntityType get(EntityType entity) {
                if (entity==null) {
                    return null;
                }

                MetaData metaData = metaDataFactory.getMetaData(entity.getClass());
                GenericDAO genericDAO = daoFactory.getDAO(entity.getClass());

                Serializable primaryKey = (Serializable) ReflectionUtil.getValueFromBean(entity, metaData.getPrimaryKeyPropertyName());

                try {
                    EntityType originalEntity = (EntityType)genericDAO.readOriginal(dataSession,primaryKey);

                    return originalEntity;
                } catch (BusinessException ex) {
                    UnckeckException.throwCkeckedExceptionAsUnckeckedException(ex);

                    return null;
                }
        }
        
    }

    
}
