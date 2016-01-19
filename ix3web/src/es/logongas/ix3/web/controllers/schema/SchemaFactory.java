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
package es.logongas.ix3.web.controllers.schema;

import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.DataSession;
import es.logongas.ix3.dao.metadata.MetaData;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.dao.metadata.ValuesList;
import es.logongas.ix3.service.CRUDService;
import es.logongas.ix3.service.CRUDServiceFactory;
import es.logongas.ix3.web.json.beanmapper.Expands;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Lorenzo
 */
public class SchemaFactory {

    public Schema getSchema(MetaData metaData, MetaDataFactory metaDataFactory, CRUDServiceFactory crudServiceFactory, Expands expands, DataSession dataSession) throws BusinessException {
        Schema schema = new Schema();
        String propertyPath = "";

        schema.setClassName(metaData.getType().getSimpleName());
        schema.setDescription(schema.getClassName());
        schema.setTitle(schema.getClassName());
        schema.setPrimaryKeyPropertyName(metaData.getPrimaryKeyPropertyName());
        schema.setNaturalKeyPropertiesName(metaData.getNaturalKeyPropertiesName());

        for (String propertyName : metaData.getPropertiesMetaData().keySet()) {
            MetaData propertyMetaData = metaData.getPropertiesMetaData().get(propertyName);

            String fullPropertyName;
            if ((propertyPath == null) || (propertyPath.trim().length() == 0)) {
                fullPropertyName = propertyName;
            } else {
                fullPropertyName = propertyPath + "." + propertyName;
            }

            if ((propertyMetaData.isCollection() == false) || (expands.isExpandProperty(fullPropertyName))) {
                Property property = getPropertyFromMetaData(propertyMetaData, metaDataFactory, crudServiceFactory, expands, fullPropertyName, dataSession);

                schema.getProperties().put(propertyName, property);
            }
        }

        return schema;
    }

    private Property getPropertyFromMetaData(MetaData metaData, MetaDataFactory metaDataFactory, CRUDServiceFactory crudServiceFactory, Expands expands, String propertyPath, DataSession dataSession) throws BusinessException {
        Property property = new Property();
        property.setType(Type.getTypeFromClass(metaData.getType()));

        if (property.getType() == Type.OBJECT) {
            property.setClassName(metaData.getType().getSimpleName());
            property.setPrimaryKeyPropertyName(metaData.getPrimaryKeyPropertyName());
            property.setNaturalKeyPropertiesName(metaData.getNaturalKeyPropertiesName());

            for (String propertyName : metaData.getPropertiesMetaData().keySet()) {
                MetaData propertyMetaData = metaData.getPropertiesMetaData().get(propertyName);

                String fullPropertyName;
                if ((propertyPath == null) || (propertyPath.trim().length() == 0)) {
                    fullPropertyName = propertyName;
                } else {
                    fullPropertyName = propertyPath + "." + propertyName;
                }

                if ((propertyMetaData.isCollection() == false) || (expands.isExpandProperty(fullPropertyName))) {
                    Property subproperty = getPropertyFromMetaData(propertyMetaData, metaDataFactory, crudServiceFactory, expands, fullPropertyName, dataSession);

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
                List<Object> data = crudServiceEntityValuesList.search(dataSession, null, null, null);
                property.setValues(getValuesFromData(data, primaryKeyName));
            } else {
                property.setValues(null);
            }

        } else {
            property.setValues(null);
        }

        property.setRequired(metaData.getConstraints().isRequired());

        if (metaData.getConstraints().getMinimum() == Long.MIN_VALUE) {
            property.setMinimum(null);
        } else {
            property.setMinimum(metaData.getConstraints().getMinimum());
        }

        if (metaData.getConstraints().getMaximum() == Long.MAX_VALUE) {
            property.setMaximum(null);
        } else {
            property.setMaximum(metaData.getConstraints().getMaximum());
        }

        if (metaData.getConstraints().getMinLength() == 0) {
            property.setMinLength(null);
        } else {
            property.setMinLength(metaData.getConstraints().getMinLength());
        }
        if (metaData.getConstraints().getMaxLength() == Integer.MAX_VALUE) {
            property.setMaxLength(null);
        } else {
            property.setMaxLength(metaData.getConstraints().getMaxLength());
        }

        property.setPattern(metaData.getConstraints().getPattern());
        if (metaData.getConstraints().getFormat() != null) {
            property.setFormat(Format.valueOf(metaData.getConstraints().getFormat().name())); //Transformamos de es.logongas.ix3.dao.metadata a es.logongas.ix3.web.controllers.schema.Format
        } else {
            property.setFormat(null);
        }

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
            Value value = new Value(enumConstant.name(), enumConstant.toString());
            values.add(value);
        }

        Collections.sort(values, new Comparator<Object>() {

            @Override
            public int compare(Object value1, Object value2) {
                return value1.toString().compareTo(value2.toString());
            }
        });

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

        values = data;

        Collections.sort(values, new Comparator<Object>() {

            @Override
            public int compare(Object value1, Object value2) {
                return value1.toString().compareTo(value2.toString());
            }
        });

        return values;
    }

}
