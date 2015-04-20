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

