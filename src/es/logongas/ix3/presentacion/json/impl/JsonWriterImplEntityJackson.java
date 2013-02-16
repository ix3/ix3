/*
 * Copyright 2013 Lorenzo González.
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
package es.logongas.ix3.presentacion.json.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.logongas.ix3.persistencia.services.metadata.MetaData;
import es.logongas.ix3.persistencia.services.metadata.MetaDataFactory;
import es.logongas.ix3.presentacion.json.JsonWriter;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Lorenzo González
 */
public class JsonWriterImplEntityJackson implements JsonWriter {

    @Autowired
    private MetaDataFactory metaDataFactory;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String toJson(Object obj) {
        try {
            if (obj != null) {
                MetaData metaData = metaDataFactory.getMetaData(obj.getClass());

                if (metaData == null) {
                    throw new RuntimeException("El objeto debe ser una clase de negocio:" + obj.getClass().getName());
                }

                Map<String, Object> values = getMapFromEntity(obj, metaData);

                return objectMapper.writeValueAsString(values);
            } else {
                return objectMapper.writeValueAsString(null);
            }
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }

    }

    private Map<String, Object> getMapFromEntity(Object obj, MetaData metaData) {
        Map<String, Object> values = new LinkedHashMap<>();

        if (obj == null) {
            throw new IllegalArgumentException("El argumento 'obj' no puede ser null");
        }
        if (metaData == null) {
            throw new IllegalArgumentException("El argumento 'metaData' no puede ser null");
        }

        for (String propertyName : metaData.getPropertiesMetaData().keySet()) {
            MetaData propertyMetaData = metaData.getPropertiesMetaData().get(propertyName);

            Object value;

            if (propertyMetaData.isCollection() == false) {
                //No es una colección

                if (isPropertyScalar(propertyMetaData) == true) {
                    value = getValue(obj, propertyName);
                } else if (isPropertyForeingEntity(propertyMetaData)) {
                    Object rawValue = getValue(obj, propertyName);
                    if (rawValue != null) {
                        value = getMapFromForeingEntity(rawValue, propertyMetaData);
                    } else {
                        value = null;
                    }
                } else {
                    //Es una referencia a algo que no es otra entidad ni un valor escalar
                    Object rawValue = getValue(obj, propertyName);
                    if (rawValue != null) {
                        value = getMapFromEntity(rawValue, propertyMetaData);
                    } else {
                        value = null;
                    }
                }
            } else {
                //En las colecciones se añade el array vacio.
                value = new ArrayList();
            }

            values.put(propertyName, value);

        }

        //Añadimos la representación como String del objeto
        values.put("toString", obj.toString());

        return values;
    }

    /**
     * Dado un objeto que es otra entidad distinta de la que queremos transforma
     * en JSON. La transforma pero solo añadiendo las siguientes propeidades.
     * <ul> <li>El valor de la clave primaria</li> <li>El valor de las claves de
     * naturals (si las hay)</li> <li>El valor de llamar a toString</li> </ul>
     *
     * @param obj El Objeto a transformar en un Map
     * @param metaData Sus Metadatos
     * @return El Map con los valores para transformar en formato JSON
     */
    private Map<String, Object> getMapFromForeingEntity(Object obj, MetaData metaData) {
        Map<String, Object> values = new LinkedHashMap<>();

        if (obj == null) {
            throw new IllegalArgumentException("El argumento 'obj' no puede ser null");
        }
        if (obj == null) {
            throw new IllegalArgumentException("El argumento 'metaData' no puede ser null");
        }

        //Añadimos la clave primaria
        //No hace falta comprobar si es compuesta pq NO debería ni tener ciclos ni cosas raras.
        values.put(metaData.getPrimaryKeyPropertyName(), getValue(obj, metaData.getPrimaryKeyPropertyName()));

        //Añadimos las claves natural o tambien llamadas de negocio
        for (String naturalKeyPropertyName : metaData.getNaturalKeyPropertiesName()) {
            //No hace falta comprobar si es compuesta pq NO debería ni tener ciclos ni cosas raras.
            values.put(naturalKeyPropertyName, getValue(obj, naturalKeyPropertyName));
        }

        //Añadimos la representación como String del objeto
        values.put("toString", obj.toString());

        return values;
    }

    private boolean isPropertyForeingEntity(MetaData metaData) {
        if (metaData.getPrimaryKeyPropertyName() != null) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isPropertyScalar(MetaData metaData) {
        if ((metaData.getPropertiesMetaData() == null) || (metaData.getPropertiesMetaData().size() == 0)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Obtiene el valor de la propiedad de un Bean
     *
     * @param obj El objeto Bean
     * @param propertyName El nombre de la propiedad
     * @return El valor de la propiedad
     */
    private Object getValue(Object obj, String propertyName) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            Method readMethod = null;
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getName().equals(propertyName)) {
                    readMethod = propertyDescriptor.getReadMethod();
                }
            }

            if (readMethod == null) {
                throw new RuntimeException("No existe la propiedad:" + propertyName);
            }

            return readMethod.invoke(obj);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
