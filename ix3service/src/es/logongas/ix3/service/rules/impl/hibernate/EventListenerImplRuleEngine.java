/*
 * Copyright 2015 Lorenzo Gonzalez.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.logongas.ix3.service.rules.impl.hibernate;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.DAOFactory;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.security.util.PrincipalLocator;
import es.logongas.ix3.service.rules.RuleGroupPredefined;
import es.logongas.ix3.service.rules.RuleContext;
import es.logongas.ix3.service.rules.RuleEngine;
import es.logongas.ix3.service.rules.RuleEngineFactory;
import es.logongas.ix3.service.rules.impl.RuleContextImpl;
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
 * @author logongas
 */
public class EventListenerImplRuleEngine implements PreInsertEventListener, PreLoadEventListener, PreUpdateEventListener, PreDeleteEventListener, PostInsertEventListener, PostLoadEventListener, PostUpdateEventListener, PostDeleteEventListener {

    boolean autowired=false;
    
    @Autowired
    RuleEngineFactory ruleEngineFactory;

    @Autowired
    PrincipalLocator principalLocator;

    @Autowired
    MetaDataFactory metaDataFactory;

    @Autowired
    DAOFactory daoFactory;

    @Override
    public boolean onPreInsert(PreInsertEvent pie) {
        autowired();
        EntityMode entityMode = pie.getPersister().getEntityMode();
        
        RuleContext ruleContext = new RuleContextImpl(pie.getEntity(), null, principalLocator.getPrincipal());

        fireRules(ruleContext, entityMode, RuleGroupPredefined.PreInsert.class, RuleGroupPredefined.PreSave.class);

        return false;
    }

    @Override
    public void onPreLoad(PreLoadEvent ple) {
        autowired();
        EntityMode entityMode = ple.getPersister().getEntityMode();

        RuleContext ruleContext = new RuleContextImpl(ple.getEntity(), null, principalLocator.getPrincipal());


        fireRules(ruleContext, entityMode, RuleGroupPredefined.PreRead.class);
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent pue) {
        autowired();
        EntityMode entityMode = pue.getPersister().getEntityMode();

        RuleContext ruleContext = new RuleContextImplLazy(pue.getEntity(), principalLocator.getPrincipal(),daoFactory, metaDataFactory);
        
        fireRules(ruleContext, entityMode, RuleGroupPredefined.PreUpdate.class, RuleGroupPredefined.PreSave.class);

        return false;
    }

    @Override
    public boolean onPreDelete(PreDeleteEvent pde) {
        autowired();
        EntityMode entityMode = pde.getPersister().getEntityMode();

        RuleContext ruleContext = new RuleContextImplLazy(pde.getEntity(), principalLocator.getPrincipal(),daoFactory, metaDataFactory);

        fireRules(ruleContext, entityMode, RuleGroupPredefined.PreDelete.class);

        return false;
    }

    @Override
    public void onPostInsert(PostInsertEvent pie) {
        autowired();
        EntityMode entityMode = pie.getPersister().getEntityMode();

        RuleContext ruleContext = new RuleContextImpl(pie.getEntity(), null, principalLocator.getPrincipal());


        fireRules(ruleContext, entityMode, RuleGroupPredefined.PostInsert.class, RuleGroupPredefined.PostSave.class);

    }

    @Override
    public void onPostLoad(PostLoadEvent ple) {       
        autowired();
        EntityMode entityMode = ple.getPersister().getEntityMode();

        RuleContext ruleContext = new RuleContextImpl(ple.getEntity(), null, principalLocator.getPrincipal());


        fireRules(ruleContext, entityMode, RuleGroupPredefined.PostRead.class);
        
    }

    @Override
    public void onPostUpdate(PostUpdateEvent pue) {
        autowired();
        EntityMode entityMode = pue.getPersister().getEntityMode();

        RuleContext ruleContext = new RuleContextImplLazy(pue.getEntity(), principalLocator.getPrincipal(),daoFactory, metaDataFactory);

        fireRules(ruleContext, entityMode, RuleGroupPredefined.PostUpdate.class, RuleGroupPredefined.PostSave.class);
    }

    @Override
    public void onPostDelete(PostDeleteEvent pde) {
        autowired();
        EntityMode entityMode = pde.getPersister().getEntityMode();

        RuleContext ruleContext = new RuleContextImplLazy(pde.getEntity(), principalLocator.getPrincipal(),daoFactory, metaDataFactory);

        fireRules(ruleContext, entityMode, RuleGroupPredefined.PostDelete.class);
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister ep) {
        return true;
    }

    private void fireRules(RuleContext ruleContext, EntityMode mode, Class<?>... groups) {
        if (ruleContext.getEntity() == null || mode != EntityMode.POJO) {
            return;
        }

        try {
            RuleEngine ruleEngine = ruleEngineFactory.getRuleEngine(ruleContext.getEntity() .getClass());
            ruleEngine.fireConstraintRules(ruleContext.getEntity(), ruleContext, groups);
            ruleEngine.fireActionRules(ruleContext.getEntity(), ruleContext, groups);
        } catch (BusinessException ex) {
            UnckeckException.throwCkeckedExceptionAsUnckeckedException(ex);
        }
    }


    private void autowired() {
        if (autowired==false) {
              ApplicationContextProvider.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(this);
              autowired=true;
        }
    }

}
