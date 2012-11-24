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
package es.logongas.ix3.persistencia.impl.hibernate.util;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;
import javax.naming.NamingException;
import javax.naming.Reference;
import org.hibernate.Cache;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.StatelessSessionBuilder;
import org.hibernate.TypeHelper;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;

class SessionFactoryImplThreadLocal implements SessionFactory {

    private HibernateUtilInternalState state;
    
    public SessionFactoryImplThreadLocal(HibernateUtilInternalState state) {
        this.state=state;
    }

    @Override
    public SessionFactoryOptions getSessionFactoryOptions() {
        return state.sessionFactory.getSessionFactoryOptions();
    }

    @Override
    public SessionBuilder withOptions() {
        return state.sessionFactory.withOptions();
    }

    @Override
    public Session openSession() throws HibernateException {
        return state.sessionFactory.openSession();
    }

    @Override
    public Session getCurrentSession() throws HibernateException {
        return state.threadLocalSession.get();
    }

    @Override
    public StatelessSessionBuilder withStatelessOptions() {
        return state.sessionFactory.withStatelessOptions();
    }

    @Override
    public StatelessSession openStatelessSession() {
        return state.sessionFactory.openStatelessSession();
    }

    @Override
    public StatelessSession openStatelessSession(Connection cnctn) {
        return state.sessionFactory.openStatelessSession(cnctn);
    }

    @Override
    public ClassMetadata getClassMetadata(Class type) {
        return state.sessionFactory.getClassMetadata(type);
    }

    @Override
    public ClassMetadata getClassMetadata(String string) {
        return state.sessionFactory.getClassMetadata(string);
    }

    @Override
    public CollectionMetadata getCollectionMetadata(String string) {
        return state.sessionFactory.getCollectionMetadata(string);
    }

    @Override
    public Map<String, ClassMetadata> getAllClassMetadata() {
        return state.sessionFactory.getAllClassMetadata();
    }

    @Override
    public Map getAllCollectionMetadata() {
        return state.sessionFactory.getAllCollectionMetadata();
    }

    @Override
    public Statistics getStatistics() {
        return state.sessionFactory.getStatistics();
    }

    @Override
    public void close() throws HibernateException {
        state.sessionFactory.close();
    }

    @Override
    public boolean isClosed() {
        return state.sessionFactory.isClosed();
    }

    @Override
    public Cache getCache() {
        return state.sessionFactory.getCache();
    }

    @Override
    public void evict(Class type) throws HibernateException {
        state.sessionFactory.evict(type);
    }

    @Override
    public void evict(Class type, Serializable srlzbl) throws HibernateException {
        state.sessionFactory.evict(type,srlzbl);
    }

    @Override
    public void evictEntity(String string) throws HibernateException {
        state.sessionFactory.evictEntity(string);
    }

    @Override
    public void evictEntity(String string, Serializable srlzbl) throws HibernateException {
        state.sessionFactory.evictEntity(string,srlzbl);
    }

    @Override
    public void evictCollection(String string) throws HibernateException {
        state.sessionFactory.evictCollection(string);
    }

    @Override
    public void evictCollection(String string, Serializable srlzbl) throws HibernateException {
        state.sessionFactory.evictCollection(string,srlzbl);
    }

    @Override
    public void evictQueries(String string) throws HibernateException {
        state.sessionFactory.evictQueries(string);
    }

    @Override
    public void evictQueries() throws HibernateException {
        state.sessionFactory.evictQueries();
    }

    @Override
    public Set getDefinedFilterNames() {
        return state.sessionFactory.getDefinedFilterNames();
    }

    @Override
    public FilterDefinition getFilterDefinition(String string) throws HibernateException {
        return state.sessionFactory.getFilterDefinition(string);
    }

    @Override
    public boolean containsFetchProfileDefinition(String string) {
        return state.sessionFactory.containsFetchProfileDefinition(string);
    }

    @Override
    public TypeHelper getTypeHelper() {
        return state.sessionFactory.getTypeHelper();
    }

    @Override
    public Reference getReference() throws NamingException {
        return state.sessionFactory.getReference();
    }
    
}
