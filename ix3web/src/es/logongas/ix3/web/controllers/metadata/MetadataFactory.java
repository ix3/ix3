/*
 * Copyright 2013 Lorenzo.
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
package es.logongas.ix3.web.controllers.metadata;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.GenericDAO;
import es.logongas.ix3.dao.metadata.MetaData;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.dao.metadata.ValuesList;
import es.logongas.ix3.service.CRUDService;
import es.logongas.ix3.service.CRUDServiceFactory;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lorenzo
 */
public class MetadataFactory {

    public Metadata getMetadata(es.logongas.ix3.dao.metadata.MetaData metaData, MetaDataFactory metaDataFactory, CRUDServiceFactory crudServiceFactory, String basePath,List<String> expand) throws BusinessException {
        Metadata metadata = new Metadata();
        String propertyPath="";
        
        metadata.setClassName(metaData.getType().getSimpleName());
        metadata.setDescription(metadata.getClassName());
        metadata.setTitle(metadata.getClassName());
        metadata.setPrimaryKeyPropertyName(metaData.getPrimaryKeyPropertyName());
        metadata.setNaturalKeyPropertiesName(metaData.getNaturalKeyPropertiesName());
        

        for (String propertyName : metaData.getPropertiesMetaData().keySet()) {
            MetaData propertyMetaData = metaData.getPropertiesMetaData().get(propertyName);

            if ((propertyMetaData.isCollection() == false) || (expandMath(expand, propertyPath+"."+propertyName))) {
                Property property = getPropertyFromMetaData(propertyMetaData, metaDataFactory, crudServiceFactory, basePath,expand,propertyPath+"."+propertyName);

                metadata.getProperties().put(propertyName, property);
            }
        }

        return metadata;
    }

    private Property getPropertyFromMetaData(MetaData metaData, MetaDataFactory metaDataFactory, CRUDServiceFactory crudServiceFactory, String basePath,List<String> expand,String propertyPath) throws BusinessException {
        Property property = new Property();
        property.setType(Type.getTypeFromClass(metaData.getType()));

        if (property.getType() == Type.OBJECT) {
            property.setClassName(metaData.getType().getSimpleName());
            property.setPrimaryKeyPropertyName(metaData.getPrimaryKeyPropertyName());
            property.setNaturalKeyPropertiesName(metaData.getNaturalKeyPropertiesName());
            
            for (String propertyName : metaData.getPropertiesMetaData().keySet()) {
                MetaData propertyMetaData = metaData.getPropertiesMetaData().get(propertyName);

                if ((propertyMetaData.isCollection() == false) || (expandMath(expand, propertyPath+"."+propertyName))) {
                    Property subproperty = getPropertyFromMetaData(propertyMetaData, metaDataFactory, crudServiceFactory, basePath,expand,propertyPath+"."+propertyName);

                    property.getProperties().put(propertyName, subproperty);
                }
            }

        } else {
            property.setClassName("");
        }

        //Inicialmente no hay Values luego se pondrán si finalmente hay
        property.setValues(null);
        if (metaData.getType().isEnum() == true) {
            property.setValues(getValuesFromEnum(metaData.getType()));
        } else if (metaData.getConstraints().getValuesList() != null) {
            ValuesList valuesList = metaData.getConstraints().getValuesList();

            if (valuesList.shortLength() == true) {
                CRUDService crudServiceEntityValuesList = crudServiceFactory.getService(metaData.getType());
                MetaData metaDataEntityValuesList = metaDataFactory.getMetaData(metaData.getType());
                String primaryKeyName = metaDataEntityValuesList.getPrimaryKeyPropertyName();
                List<Object> data = crudServiceEntityValuesList.search(null);
                property.setValues(getValuesFromData(data, primaryKeyName));
            } else {
                property.setValues(null);
            }

        } else {
            property.setValues(null);
        }

        property.setRequired(metaData.getConstraints().isRequired());
        
        if (metaData.getConstraints().getMinimum()==Long.MIN_VALUE) {
            property.setMinimum(null);
        } else {
            property.setMinimum(metaData.getConstraints().getMinimum());
        }
        
        if (metaData.getConstraints().getMaximum()==Long.MAX_VALUE) {
            property.setMaximum(null);
        } else {
            property.setMaximum(metaData.getConstraints().getMaximum());
        }
        
        if (metaData.getConstraints().getMinLength()==0) {
            property.setMinLength(null);
        } else {
            property.setMinLength(metaData.getConstraints().getMinLength());
        }
        if (metaData.getConstraints().getMaxLength()==Integer.MAX_VALUE) {
            property.setMaxLength(null);
        } else {
            property.setMaxLength(metaData.getConstraints().getMaxLength());
        }
        
        property.setPattern(metaData.getConstraints().getPattern());
        property.setFormat(metaData.getConstraints().getFormat());

        property.setLabel(metaData.getLabel());
        property.setDescription(metaData.getLabel());

        return property;
    }

    private List<Object> getValuesFromEnum(Class clazz) {
        List<Object> values = new ArrayList<Object>();

        if (clazz.isEnum() == false) {
            throw new RuntimeException("El argumento clazz debe ser de un enumerado");
        }

        Enum[] enumConstants = (Enum[]) clazz.getEnumConstants();

        for (int i = 0; i < enumConstants.length; i++) {
            Enum enumConstant = enumConstants[i];
            Value value=new Value(enumConstant.name(),enumConstant.toString());
            values.add(value);
        }

        return values;
    }

    private List<Object> getValuesFromData(List<Object> data, String primaryKeyName) {
        List<Object> values;

        if (data == null) {
            throw new RuntimeException("El argumento data no puede ser null");
        }
        if ((primaryKeyName == null) || (primaryKeyName.trim().length() == 0)) {
            throw new RuntimeException("El argumento primaryKeyName no puede estar vacio");
        }

        values=data;

        return values;
    }
    
    private boolean expandMath(List<String> expands,String propertyPath) {
        for(String expandProperty:expands) {
            if (("."+expandProperty).startsWith(propertyPath)) {
                return true;
            }
        }
        
        return false;
        
    }
}
