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
package es.logongas.ix3.businessprocess.impl;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.core.Order;
import es.logongas.ix3.core.Page;
import es.logongas.ix3.core.PageRequest;
import es.logongas.ix3.dao.Filter;
import es.logongas.ix3.dao.SearchResponse;
import es.logongas.ix3.dao.TransactionManager;
import es.logongas.ix3.businessprocess.CRUDBusinessProcess;
import es.logongas.ix3.dao.DAOFactory;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.rule.RuleContext;
import es.logongas.ix3.rule.RuleEngine;
import es.logongas.ix3.rule.RuleEngineFactory;
import es.logongas.ix3.rule.RuleGroupPredefined;
import es.logongas.ix3.rule.impl.RuleContextImpl;
import es.logongas.ix3.core.Principal;
import es.logongas.ix3.service.CRUDService;
import es.logongas.ix3.service.CRUDServiceFactory;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Lorenzo
 * @param <EntityType>
 * @param <PrimaryKeyType>
 */
public class CRUDBusinessProcessImpl<EntityType, PrimaryKeyType extends Serializable> implements CRUDBusinessProcess<EntityType, PrimaryKeyType> {

    @Autowired protected CRUDServiceFactory serviceFactory;
    @Autowired protected DAOFactory daoFactory;    
    @Autowired protected TransactionManager transactionManager;
    @Autowired RuleEngineFactory ruleEngineFactory;
    @Autowired MetaDataFactory metaDataFactory;

    Class entityType;

    protected final Log log = LogFactory.getLog(getClass());

    public CRUDBusinessProcessImpl() {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            entityType = (Class<EntityType>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }
    }

    public CRUDBusinessProcessImpl(Class<EntityType> entityType) {
        this.entityType = entityType;
    }

    protected CRUDService<EntityType, PrimaryKeyType> getCRUDService() {
        return serviceFactory.getService(entityType);
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
    public EntityType create(CreateArguments createArguments) throws BusinessException {
        EntityType entity = getCRUDService().create(createArguments.dataSession, createArguments.initialProperties);
        return entity;
    }

    @Override
    public EntityType read(ReadArguments<PrimaryKeyType> readArguments) throws BusinessException {

        EntityType entity = getCRUDService().read(readArguments.dataSession, readArguments.id);

        RuleContext<EntityType> ruleContext = new RuleContextImpl(entity, null, readArguments.principal);
        fireRules(ruleContext, RuleGroupPredefined.PostRead.class);
        
        return entity;
    }



    @Override
    public EntityType readByNaturalKey(ReadByNaturalKeyArguments readByNaturalKeyArguments) throws BusinessException {

        EntityType entity = getCRUDService().readByNaturalKey(readByNaturalKeyArguments.dataSession, readByNaturalKeyArguments.naturalKey);

        RuleContext<EntityType> ruleContext = new RuleContextImpl(entity, null, readByNaturalKeyArguments.principal);
        fireRules(ruleContext, RuleGroupPredefined.PostRead.class);
        
        return entity;
    }


    @Override
    public EntityType insert(InsertArguments<EntityType> insertArguments) throws BusinessException {
        RuleContext<EntityType> ruleContext = new RuleContextImpl(insertArguments.entity, null, insertArguments.principal);
        fireRules(ruleContext, RuleGroupPredefined.PreInsert.class, RuleGroupPredefined.PreInsertOrUpdate.class, RuleGroupPredefined.PreInsertOrUpdateOrDelete.class);

        EntityType resultEntity = getCRUDService().insert(insertArguments.dataSession, insertArguments.entity);
       
        fireRules(ruleContext, RuleGroupPredefined.PostInsert.class, RuleGroupPredefined.PostInsertOrUpdate.class, RuleGroupPredefined.PostInsertOrUpdateOrDelete.class);

        return resultEntity;
    }

    @Override
    public EntityType update(UpdateArguments<EntityType> updateArguments) throws BusinessException {
        RuleContext<EntityType> ruleContext = new RuleContextImpl(updateArguments.entity, updateArguments.originalEntity, updateArguments.principal);
        fireRules(ruleContext, RuleGroupPredefined.PreUpdate.class, RuleGroupPredefined.PreInsertOrUpdate.class, RuleGroupPredefined.PreUpdateOrDelete.class, RuleGroupPredefined.PreInsertOrUpdateOrDelete.class);

        EntityType resultEntity = getCRUDService().update(updateArguments.dataSession, updateArguments.entity);

        fireRules(ruleContext, RuleGroupPredefined.PostUpdate.class, RuleGroupPredefined.PostInsertOrUpdate.class, RuleGroupPredefined.PostUpdateOrDelete.class, RuleGroupPredefined.PostInsertOrUpdateOrDelete.class);

        return resultEntity;
    }

    @Override
    public boolean delete(DeleteArguments<EntityType> deleteArguments) throws BusinessException {

        RuleContext<EntityType> ruleContext = new RuleContextImpl(deleteArguments.entity, deleteArguments.entity, deleteArguments.principal);

        fireRules(ruleContext, RuleGroupPredefined.PreDelete.class, RuleGroupPredefined.PreInsertOrUpdateOrDelete.class, RuleGroupPredefined.PreUpdateOrDelete.class);

        boolean success = getCRUDService().delete(deleteArguments.dataSession, deleteArguments.entity);

        fireRules(ruleContext, RuleGroupPredefined.PostDelete.class, RuleGroupPredefined.PostInsertOrUpdateOrDelete.class, RuleGroupPredefined.PostUpdateOrDelete.class);

        return success;
    }

    @Override
    public List<EntityType> search(SearchArguments searchArguments) throws BusinessException {
        List<EntityType> entities = getCRUDService().search(searchArguments.dataSession, searchArguments.filters, searchArguments.orders, searchArguments.searchResponse);
        
        RuleEngine ruleEngine = ruleEngineFactory.getRuleEngine(this.entityType);
        
        if (entities!=null) {
            for(EntityType entity:entities) {
                RuleContext<EntityType> ruleContext = new RuleContextImpl(entity, null, searchArguments.principal);
                fireRules(ruleEngine,ruleContext, RuleGroupPredefined.PostRead.class);      
            }
        }
        
        return entities;
    }

    @Override
    public Page<EntityType> pageableSearch(PageableSearchArguments pageableSearchArguments) throws BusinessException {
        Page<EntityType> page = getCRUDService().pageableSearch(pageableSearchArguments.dataSession, pageableSearchArguments.filters, pageableSearchArguments.orders, pageableSearchArguments.pageRequest, pageableSearchArguments.searchResponse);
        
        RuleEngine ruleEngine = ruleEngineFactory.getRuleEngine(this.entityType);
        
        if ((page!=null) && (page.getContent()!=null)) {
            for(EntityType entity:page.getContent()) {
                RuleContext<EntityType> ruleContext = new RuleContextImpl(entity, null, pageableSearchArguments.principal);
                fireRules(ruleEngine,ruleContext, RuleGroupPredefined.PostRead.class);      
            }
        }
        
        return page;
    }

    private void fireRules(RuleContext<EntityType> ruleContext, Class<?>... groups) throws BusinessException {
        RuleEngine ruleEngine = ruleEngineFactory.getRuleEngine(this.entityType);
        fireRules(ruleEngine, ruleContext, groups);
    }

    private void fireRules(RuleEngine ruleEngine, RuleContext<EntityType> ruleContext, Class<?>... groups) throws BusinessException {
        ruleEngine.fireConstraintRules(this, ruleContext, groups);
        ruleEngine.fireActionRules(this, ruleContext, groups);
    }    
    
}
