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

import es.logongas.ix3.persistence.services.dao.BusinessException;
import es.logongas.ix3.persistence.services.dao.DAOFactory;
import es.logongas.ix3.persistence.services.dao.GenericDAO;
import es.logongas.ix3.persistence.services.metadata.MetaData;
import es.logongas.ix3.persistence.services.metadata.MetaDataFactory;
import es.logongas.ix3.persistence.services.metadata.ValuesList;
import es.logongas.ix3.util.ReflectionUtil;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Lorenzo
 */
public class MetadataFactory {

    public Metadata getMetadata(es.logongas.ix3.persistence.services.metadata.MetaData metaData, MetaDataFactory metaDataFactory, DAOFactory daoFactory, String basePath) throws BusinessException {
        Metadata metadata = new Metadata();

        metadata.setClassName(metaData.getType().getSimpleName());
        metadata.setDescription(metadata.getClassName());
        metadata.setTitle(metadata.getClassName());

        for (String propertyName : metaData.getPropertiesMetaData().keySet()) {
            MetaData propertyMetaData = metaData.getPropertiesMetaData().get(propertyName);

            if (propertyMetaData.isCollection() == false) {
                Property property = getPropertyFromMetaData(propertyMetaData, metaDataFactory, daoFactory, basePath);

                metadata.getProperties().put(propertyName, property);
            }
        }

        return metadata;
    }

    private Property getPropertyFromMetaData(MetaData metaData, MetaDataFactory metaDataFactory, DAOFactory daoFactory, String basePath) throws BusinessException {
        Property property = new Property();
        property.setType(Type.getTypeFromClass(metaData.getType()));

        if (property.getType() == Type.OBJECT) {
            property.setClassName(metaData.getType().getSimpleName());

            for (String propertyName : metaData.getPropertiesMetaData().keySet()) {
                MetaData propertyMetaData = metaData.getPropertiesMetaData().get(propertyName);

                if (propertyMetaData.isCollection() == false) {
                    Property subproperty = getPropertyFromMetaData(propertyMetaData, metaDataFactory, daoFactory, basePath);

                    property.getProperties().put(propertyName, subproperty);
                }
            }

        } else {
            property.setClassName("");
        }

        if (metaData.getType().isEnum() == true) {
            property.setValues(getValuesFromEnum(metaData.getType()));
        } else if (metaData.getConstraints().getValuesList() != null) {
            ValuesList valuesList = metaData.getConstraints().getValuesList();

            property.setDependProperties(Arrays.asList(valuesList.dependProperties()));
            if ((valuesList.shortLength() == true) && ((valuesList.dependProperties() == null) || (valuesList.dependProperties().length == 0))) {
                //Los valores no dependen de nada , así que podemos leer los valores directamente
                GenericDAO genericDAOEntityValuesList = daoFactory.getDAO(valuesList.entity());
                MetaData metaDataEntityValuesList = metaDataFactory.getMetaData(valuesList.entity());
                String primaryKeyName = metaDataEntityValuesList.getPrimaryKeyPropertyName();
                List<Object> data;
                if ((valuesList.namedSearch() != null) && (valuesList.namedSearch().trim().length() > 0)) {
                    data = (List<Object>) genericDAOEntityValuesList.namedSearch(valuesList.namedSearch(), null);
                } else {
                    data = genericDAOEntityValuesList.search(null);
                }
                property.setValues(getValuesFromData(data, primaryKeyName));
            } else {
                //Los valores dependen de otra/s columnas o son muy "grandes" para poder leerlos
                if ((valuesList.namedSearch() != null) && (valuesList.namedSearch().trim().length() > 0)) {
                    property.setUrlValues(basePath + "/" + valuesList.entity().getSimpleName() + "/namedsearch/" + valuesList.namedSearch());
                } else {
                    property.setUrlValues(basePath + "/" + valuesList.entity().getSimpleName());
                }
            }

        }

        property.setRequired(metaData.getConstraints().isRequired());
        property.setMinimum(metaData.getConstraints().getMinimum());
        property.setMaximum(metaData.getConstraints().getMaximum());
        property.setMinLength(metaData.getConstraints().getMinLength());
        property.setMaxLength(metaData.getConstraints().getMaxLength());
        property.setPattern(metaData.getConstraints().getPattern());
        property.setFormat(metaData.getConstraints().getFormat());
        //property.setKey(metadata.);
        //property.setNaturalKey(metadata.);

        property.setLabel(metaData.getCaption());
        property.setDescription(metaData.getCaption());

        return property;
    }

    private Map<Object, String> getValuesFromEnum(Class clazz) {
        Map<Object, String> values = new LinkedHashMap<Object, String>();

        if (clazz.isEnum() == false) {
            throw new RuntimeException("El argumento clazz debe ser de un enumerado");
        }

        Enum[] enumConstants = (Enum[]) clazz.getEnumConstants();

        for (int i = 0; i < enumConstants.length; i++) {
            Enum enumConstant = enumConstants[i];

            values.put(enumConstant.name(), enumConstant.toString());
        }

        return values;
    }

    private Map<Object, String> getValuesFromData(List<Object> data, String primaryKeyName) {
        Map<Object, String> values = new LinkedHashMap<Object, String>();

        if (data == null) {
            throw new RuntimeException("El argumento data no puede ser null");
        }
        if ((primaryKeyName == null) || (primaryKeyName.trim().length() == 0)) {
            throw new RuntimeException("El argumento primaryKeyName no puede estar vacio");
        }

        for (Object obj : data) {
            values.put(ReflectionUtil.getValueFromBean(obj, primaryKeyName), obj.toString());
        }

        return values;
    }

}
