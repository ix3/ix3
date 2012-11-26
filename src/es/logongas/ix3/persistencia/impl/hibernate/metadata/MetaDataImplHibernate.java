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

import es.logongas.ix3.persistencia.services.metadata.ClientValidations;
import es.logongas.ix3.persistencia.services.metadata.MetaData;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;

/**
 *
 * @author alumno
 */
public class MetaDataImplHibernate implements MetaData {

    Class entityType;
    Map<String,MetaData> metaDatas;
    ClientValidations clientValidations=new ClientValidations();
    ClassMetadata classMetadata;
    
    protected MetaDataImplHibernate(Class entityType,SessionFactory sessionFactory) {
        
        try {
            this.entityType=entityType;
            classMetadata=sessionFactory.getClassMetadata(entityType);
            
            PropertyDescriptor[] arrayPopertyDescriptors=Introspector.getBeanInfo(entityType).getPropertyDescriptors();
            metaDatas=new HashMap<>();
            for (PropertyDescriptor propertyDescriptor:arrayPopertyDescriptors) {
                MetaData metaData=new MetaDataImplHibernate(propertyDescriptor.getClass(),sessionFactory);
                metaDatas.put(propertyDescriptor.getName(),metaData);
            }           
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    @Override
    public Class getType() {
        return entityType;
    }

    @Override
    public Map<String,MetaData> getPropertiesMetaData() {
        return metaDatas;
    }

    @Override
    public String getCaption() {
        return entityType.getName();
    }

    @Override
    public boolean isComponent() {
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public List<String> getNaturalKeyPropertiesName() {
        List<String> naturalKeyPropertiesName=new ArrayList<>();
        
        if (classMetadata.hasNaturalIdentifier()) {
            
            int[] positions=classMetadata.getNaturalIdentifierProperties();
            String[] propertyNames=classMetadata.getPropertyNames();
            
            for(int i=0;i<positions.length;i++) {
                int position=positions[i];
                String naturalKeyPropertyName=propertyNames[position];
                
                naturalKeyPropertiesName.add(naturalKeyPropertyName);
            }
            
            
        } else {
            //Si no hay clave natural, usamos la clave primaria como clave natural
            naturalKeyPropertiesName.add(getPrimaryKeyPropertyName());
        }
        
        return naturalKeyPropertiesName;
    }

    @Override
    public ClientValidations getClientValidations() {
        return clientValidations;
    }

    @Override
    public String getPrimaryKeyPropertyName() {
        if (classMetadata.hasIdentifierProperty()==true) {
            return classMetadata.getIdentifierPropertyName();
        } else {
            return null;
        }
    }


    
}
