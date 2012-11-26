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
package es.logongas.ix3.persistencia.impl.hibernate.metadata;

import es.logongas.ix3.persistencia.services.metadata.MetaData;
import es.logongas.ix3.persistencia.services.metadata.EntityMetaDataFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Lorenzo González
 */
public class EntityMetaDataFactoryImplHibernate implements EntityMetaDataFactory {

    @Autowired
    SessionFactory sessionFactory;
    
    @Override
    public MetaData getEntityMetaData(Class entityClass) {        
        return new MetaDataImplHibernate(entityClass,sessionFactory);
    }

    @Override
    public MetaData getEntityMetaData(String entityName) {
        return new MetaDataImplHibernate(sessionFactory.getClassMetadata(entityName).getMappedClass(),sessionFactory);
    }
    
}
