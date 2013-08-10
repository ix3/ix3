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
package es.logongas.ix3.persistence.impl.hibernate.metadata;

import es.logongas.ix3.persistence.services.metadata.MetaData;
import es.logongas.ix3.persistence.services.metadata.MetaDataFactory;
import java.util.Map;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Lorenzo González
 */
public class MetaDataFactoryImplHibernate implements MetaDataFactory {

    @Autowired
    SessionFactory sessionFactory;

    @Override
    public MetaData getMetaData(Class entityClass) {
        ClassMetadata classMetadata=getClassMetadata(entityClass);
        if (classMetadata==null) {
            return null;
        } else {
            return new MetaDataImplHibernate(classMetadata.getMappedClass(),sessionFactory);
        }
    }
    
    @Override
    public MetaData getMetaData(Object obj) {
        return getMetaData(Hibernate.getClass(obj));
    }
    
    @Override
    public MetaData getMetaData(String entityName) {
        ClassMetadata classMetadata=getClassMetadata(entityName);
        if (classMetadata==null) {
            return null;
        } else {
            return new MetaDataImplHibernate(classMetadata.getMappedClass(),sessionFactory);
        }
    }


    private ClassMetadata getClassMetadata(Class entityClass) {
        return sessionFactory.getClassMetadata(entityClass);
    }

    private ClassMetadata getClassMetadata(String entityName) {
        Map<String,ClassMetadata> classMetadatas=sessionFactory.getAllClassMetadata();
        ClassMetadata classMetadata=null;


        for(String fqcn:classMetadatas.keySet()) {
            if (fqcn.endsWith("."+entityName) || (fqcn.equals(entityName))) {

                if (classMetadata!=null) {
                    throw new RuntimeException("Existen 2 entidades con el mismo nombre:"+ fqcn + " y " + classMetadata.getEntityName() + " para la solicitud de " + entityName);
                }

                classMetadata=classMetadatas.get(fqcn);
            }
        }

        return classMetadata;
    }

}
