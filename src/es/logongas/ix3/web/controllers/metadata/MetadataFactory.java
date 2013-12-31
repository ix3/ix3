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

import es.logongas.ix3.persistence.services.metadata.MetaData;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Lorenzo
 */
public class MetadataFactory {

    public Metadata getMetadata(es.logongas.ix3.persistence.services.metadata.MetaData metaData) {
        Metadata metadata = new Metadata();

        metadata.setClassName(metaData.getType().getSimpleName());
        metadata.setDescription(metadata.getClassName());
        metadata.setTitle(metadata.getClassName());

        for (String propertyName : metaData.getPropertiesMetaData().keySet()) {
            MetaData propertyMetaData = metaData.getPropertiesMetaData().get(propertyName);

            if (propertyMetaData.isCollection() == false) {
                Property property = getPropertyFromMetaData(propertyMetaData);

                metadata.getProperties().put(propertyName, property);
            }
        }

        return metadata;
    }

    private Property getPropertyFromMetaData(MetaData metaData) {
        Property property = new Property();
        property.setType(Type.getTypeFromClass(metaData.getType()));

        if (property.getType() == Type.OBJECT) {
            property.setClassName(metaData.getType().getSimpleName());

            for (String propertyName : metaData.getPropertiesMetaData().keySet()) {
                MetaData propertyMetaData = metaData.getPropertiesMetaData().get(propertyName);

                if (propertyMetaData.isCollection() == false) {
                    Property subproperty = getPropertyFromMetaData(propertyMetaData);

                    property.getProperties().put(propertyName, subproperty);
                }
            }

        } else {
            property.setClassName("");
        }

        if (metaData.getType().isEnum() == true) {
            property.setValues(getValuesFromEnum(metaData.getType()));
        }
        

        property.setRequired(metaData.isRequired());
        property.setMinimum(metaData.getMinimum());
        property.setMaximum(metaData.getMaximum());
        property.setMinLength(metaData.getMinLength());
        property.setMaxLength(metaData.getMaxLength());
        property.setPattern(metaData.getPattern());
        property.setFormat(metaData.getFormat());
        //property.urlValues; //las propiedades values o urlValues son excluyentes
        //property.dependProperties = new ArrayList<String>(); //Solo está este valor si urlValues!=null
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

}
