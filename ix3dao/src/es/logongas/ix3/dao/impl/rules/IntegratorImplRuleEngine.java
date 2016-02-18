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

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

/**
 * Integracion del sistema de reglas con Hibernate
 * @author logongas
 */
public class IntegratorImplRuleEngine implements Integrator {

    @Override
    public void integrate(Configuration c, SessionFactoryImplementor sfi, SessionFactoryServiceRegistry sfsr) {
        final EventListenerRegistry eventListenerRegistry = sfsr.getService(EventListenerRegistry.class);

        prependListeners(eventListenerRegistry);

    }

    @Override
    public void integrate(MetadataImplementor mi, SessionFactoryImplementor sfi, SessionFactoryServiceRegistry sfsr) {
        final EventListenerRegistry eventListenerRegistry = sfsr.getService(EventListenerRegistry.class);

        prependListeners(eventListenerRegistry);

    }

    @Override
    public void disintegrate(SessionFactoryImplementor sfi, SessionFactoryServiceRegistry sfsr) {
    }

    private void prependListeners(EventListenerRegistry eventListenerRegistry) {
        eventListenerRegistry.prependListeners(EventType.PRE_INSERT, new EventListenerImplRuleEngine());
        eventListenerRegistry.prependListeners(EventType.PRE_LOAD, new EventListenerImplRuleEngine());
        eventListenerRegistry.prependListeners(EventType.PRE_UPDATE, new EventListenerImplRuleEngine());
        eventListenerRegistry.prependListeners(EventType.PRE_DELETE, new EventListenerImplRuleEngine());
        eventListenerRegistry.prependListeners(EventType.POST_INSERT, new EventListenerImplRuleEngine());
        eventListenerRegistry.prependListeners(EventType.POST_LOAD, new EventListenerImplRuleEngine());
        eventListenerRegistry.prependListeners(EventType.POST_UPDATE, new EventListenerImplRuleEngine());
        eventListenerRegistry.prependListeners(EventType.POST_DELETE, new EventListenerImplRuleEngine());
    }
}