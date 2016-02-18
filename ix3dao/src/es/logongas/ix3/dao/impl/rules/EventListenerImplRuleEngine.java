/*
 * Copyright 2016 logongas.
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
package es.logongas.ix3.dao.impl.rules;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.rule.RuleContext;
import es.logongas.ix3.rule.RuleEngine;
import es.logongas.ix3.rule.RuleEngineFactory;
import es.logongas.ix3.rule.RuleGroupPredefined;
import es.logongas.ix3.util.ReflectionUtil;
import es.logongas.ix3.util.UnckeckException;
import org.hibernate.EntityMode;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Lanza el sistema de reglas en las entidades de Hibernate
 *
 * @author logongas
 */
public class EventListenerImplRuleEngine implements PreInsertEventListener, PreLoadEventListener, PreUpdateEventListener, PreDeleteEventListener, PostInsertEventListener, PostLoadEventListener, PostUpdateEventListener, PostDeleteEventListener {

    boolean autowired = false;

    @Autowired
    RuleEngineFactory ruleEngineFactory;

    @Autowired
    MetaDataFactory metaDataFactory;


    @Override
    public boolean onPreInsert(PreInsertEvent pie) {
        autowired();
        EntityMode entityMode = pie.getPersister().getEntityMode();

        
        RuleContext ruleContext = new RuleContextImplNoPrincipal(pie.getEntity(), null);

        fireRules(ruleContext, pie.getPersister(), pie.getState(), entityMode, RuleGroupPredefined.PreInsert.class, RuleGroupPredefined.PreInsertOrUpdate.class, RuleGroupPredefined.PreInsertOrUpdateOrDelete.class);

        return false;
    }

    @Override
    public void onPreLoad(PreLoadEvent ple) {
        autowired();
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent pue) {
        autowired();
        EntityMode entityMode = pue.getPersister().getEntityMode();


        
        RuleContext ruleContext = new RuleContextImplNoPrincipal(pue.getEntity(), getOriginalEntity(pue.getOldState(), pue.getPersister()));

        fireRules(ruleContext, pue.getPersister(), pue.getState(), entityMode, RuleGroupPredefined.PreUpdate.class, RuleGroupPredefined.PreInsertOrUpdate.class, RuleGroupPredefined.PreUpdateOrDelete.class, RuleGroupPredefined.PreInsertOrUpdateOrDelete.class);

        return false;
    }

    @Override
    public boolean onPreDelete(PreDeleteEvent pde) {
        autowired();
        EntityMode entityMode = pde.getPersister().getEntityMode();

        RuleContext ruleContext = new RuleContextImplNoPrincipal(pde.getEntity(), pde.getEntity());

        fireRules(ruleContext, pde.getPersister(), null, entityMode, RuleGroupPredefined.PreDelete.class, RuleGroupPredefined.PreInsertOrUpdateOrDelete.class, RuleGroupPredefined.PreUpdateOrDelete.class);

        return false;
    }

    @Override
    public void onPostInsert(PostInsertEvent pie) {
        autowired();
        EntityMode entityMode = pie.getPersister().getEntityMode();

        RuleContext ruleContext = new RuleContextImplNoPrincipal(pie.getEntity(), null);

        fireRules(ruleContext, pie.getPersister(), pie.getState(), entityMode, RuleGroupPredefined.PostInsert.class, RuleGroupPredefined.PostInsertOrUpdate.class, RuleGroupPredefined.PostInsertOrUpdateOrDelete.class);

    }

    @Override
    public void onPostLoad(PostLoadEvent ple) {
        autowired();
        EntityMode entityMode = ple.getPersister().getEntityMode();

        RuleContext ruleContext = new RuleContextImplNoPrincipal(ple.getEntity(), null);

        fireRules(ruleContext, ple.getPersister(), null, entityMode, RuleGroupPredefined.PostRead.class);

    }

    @Override
    public void onPostUpdate(PostUpdateEvent pue) {
        autowired();
        EntityMode entityMode = pue.getPersister().getEntityMode();

        RuleContext ruleContext = new RuleContextImplNoPrincipal(pue.getEntity(), getOriginalEntity(pue.getOldState(), pue.getPersister()));

        fireRules(ruleContext, pue.getPersister(), pue.getState(), entityMode, RuleGroupPredefined.PostUpdate.class, RuleGroupPredefined.PostInsertOrUpdate.class, RuleGroupPredefined.PostUpdateOrDelete.class, RuleGroupPredefined.PostInsertOrUpdateOrDelete.class);
    }

    @Override
    public void onPostDelete(PostDeleteEvent pde) {
        autowired();
        EntityMode entityMode = pde.getPersister().getEntityMode();

        RuleContext ruleContext = new RuleContextImplNoPrincipal(pde.getEntity(), null);

        fireRules(ruleContext, pde.getPersister(), null, entityMode, RuleGroupPredefined.PostDelete.class, RuleGroupPredefined.PostInsertOrUpdateOrDelete.class, RuleGroupPredefined.PostUpdateOrDelete.class);
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister ep) {
        return true;
    }

    private void fireRules(RuleContext ruleContext, EntityPersister entityPersister, Object[] state, EntityMode mode, Class<?>... groups) {
        if (ruleContext.getEntity() == null || mode != EntityMode.POJO) {
            return;
        }

        try {
            stateToEntity(state, ruleContext.getEntity(), entityPersister);

            RuleEngine ruleEngine = ruleEngineFactory.getRuleEngine(ruleContext.getEntity().getClass());
            ruleEngine.fireConstraintRules(ruleContext.getEntity(), ruleContext, groups);
            ruleEngine.fireActionRules(ruleContext.getEntity(), ruleContext, groups);
            
            entityToState(ruleContext.getEntity(), state, entityPersister);

        } catch (BusinessException ex) {
            UnckeckException.throwCkeckedExceptionAsUnckeckedException(ex);
        }
    }

    private void autowired() {
        if (autowired == false) {
            ApplicationContextProvider.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(this);
            autowired = true;
        }
    }

    
    Object getOriginalEntity(Object[] oldState,EntityPersister entityPersister) {
        try {
            Object originalEntity=entityPersister.getMappedClass().newInstance();
            stateToEntity(oldState, originalEntity, entityPersister);
            
            return originalEntity;
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
    

    private void stateToEntity(Object[] state, Object entity, EntityPersister entityPersister) {
        
        if (state==null) {
            return;
        }
        
        String[] propertyNames=entityPersister.getPropertyNames();
        
        for(int i=0;i<propertyNames.length;i++) {
            Object value=state[i];
            
            ReflectionUtil.setValueToBean(entity, propertyNames[i], value);
            
        }
        
    }

    private void entityToState(Object entity, Object[] state, EntityPersister entityPersister) {
        
        if (state==null) {
            return;
        }        
        
        String[] propertyNames=entityPersister.getPropertyNames();
        
        for(int i=0;i<propertyNames.length;i++) {
            Object value=ReflectionUtil.getValueFromBean(entity, propertyNames[i]);
            
            state[i]=value;
            
        }
    }    



}
