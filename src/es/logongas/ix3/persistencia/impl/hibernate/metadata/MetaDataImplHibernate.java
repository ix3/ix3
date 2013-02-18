/*
 * Copyright 2012 alumno.
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

import com.fasterxml.jackson.annotation.JsonManagedReference;
import es.logongas.ix3.persistencia.services.metadata.MetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;

/**
 *
 * @author alumno
 */
public class MetaDataImplHibernate implements MetaData {

    private static Map<Type, MetaData> cache = new ConcurrentHashMap<>();
    private SessionFactory sessionFactory;
    private Class entityType = null;
    private Type type = null;
    
    //No usar directamente esta propiedad sino usar el método getPropertiesMetaData()
    private Map<String, MetaData> metaDatas = null;

    protected MetaDataImplHibernate(Class entityType, SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.entityType = entityType;
    }

    protected MetaDataImplHibernate(Type type, SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.type = type;
    }

    @Override
    public Class getType() {
        if (isCollection()) {
            return ((CollectionType) type).getElementType((SessionFactoryImplementor) this.sessionFactory).getReturnedClass();
        } else if (entityType != null) {
            return entityType;
        } else if (type != null) {
            return type.getReturnedClass();
        } else {
            throw new RuntimeException("No existe el tipo");
        }
    }

    @Override
    public boolean isCollection() {
        if (type != null) {
            return type.isCollectionType();
        } else {
            return false;
        }
    }

    @Override
    @JsonManagedReference
    public Map<String, MetaData> getPropertiesMetaData() {

        if (metaDatas == null) {
            metaDatas = new LinkedHashMap<>();

            ClassMetadata classMetadata = getClassMetadata();

            if (classMetadata != null) {
                String[] propertyNames = classMetadata.getPropertyNames();
                for (String propertyName : propertyNames) {
                    Type propertyType = classMetadata.getPropertyType(propertyName);

                    MetaData metaData = cache.get(propertyType);
                    if (cache.get(propertyType) == null) {
                        metaData = new MetaDataImplHibernate(propertyType, sessionFactory);
                        cache.put(propertyType, metaData);
                    }

                    metaDatas.put(propertyName, metaData);
                }
            }
        }
        
        return metaDatas;
    }

    @Override
    public List<String> getNaturalKeyPropertiesName() {

        List<String> naturalKeyPropertiesName = new ArrayList<>();

        ClassMetadata classMetadata = getClassMetadata();
        if (classMetadata == null) {
            return null;
        }

        if (classMetadata.hasNaturalIdentifier()) {

            int[] positions = classMetadata.getNaturalIdentifierProperties();
            String[] propertyNames = classMetadata.getPropertyNames();

            for (int i = 0; i < positions.length; i++) {
                int position = positions[i];
                String naturalKeyPropertyName = propertyNames[position];

                naturalKeyPropertiesName.add(naturalKeyPropertyName);
            }


        } else {
            //Si no hay clave natural, la lista no tendrá ningún elemento
        }


        return naturalKeyPropertiesName;
    }

    @Override
    public String getPrimaryKeyPropertyName() {
        ClassMetadata classMetadata = getClassMetadata();
        if (classMetadata == null) {
            return null;
        }

        if (classMetadata.hasIdentifierProperty() == true) {
            return classMetadata.getIdentifierPropertyName();
        } else {
            return null;
        }
    }

    private ClassMetadata getClassMetadata() {
        return sessionFactory.getClassMetadata(this.getType());
    }
}
