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

import com.fasterxml.jackson.databind.ObjectMapper;
import es.logongas.ix3.persistencia.services.dao.BussinessException;
import es.logongas.ix3.persistencia.services.dao.DAOFactory;
import es.logongas.ix3.persistencia.services.dao.GenericDAO;
import es.logongas.ix3.persistencia.services.metadata.MetaData;
import es.logongas.ix3.persistencia.services.metadata.MetaDataFactory;
import es.logongas.ix3.presentacion.json.JsonReader;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Se usa para una entidad de negocio y usa Jackson
 *
 * @author Lorenzo González
 */
public class JsonReaderImplEntityJackson implements JsonReader {

    private Class clazz;
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    DAOFactory daoFactory;
    @Autowired
    private MetaDataFactory metaDataFactory;

    public JsonReaderImplEntityJackson(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object fromJson(String json) {
        try {
            Object jsonObj = objectMapper.readValue(json, clazz);

            GenericDAO genericDAO = daoFactory.getDAO(clazz);
            MetaData metaData = metaDataFactory.getMetaData(clazz);


            Object entity;
            if (emptyKey(getValue(jsonObj, metaData.getPrimaryKeyPropertyName())) == false) {
                //Si hay clave primaria es que hay que leerla entidad pq ya existe
                entity = genericDAO.read((Serializable) getValue(jsonObj, metaData.getPrimaryKeyPropertyName()));
            } else {
                //No hay clave primaria , así que creamos una nueva fila

                entity = genericDAO.create();
            }

            populateEntity(entity, jsonObj, metaData);

            return entity;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void populateEntity(Object entity, Object jsonObj, MetaData metaData) throws BussinessException {

        for (String propertyName : metaData.getPropertiesMetaData().keySet()) {
            MetaData propertyMetaData = metaData.getPropertiesMetaData().get(propertyName);

            if (propertyMetaData.isCollection() == false) {
                //No es una colección

                if (isPropertyScalar(propertyMetaData) == true) {
                    Object rawValue = getValue(jsonObj, propertyName);
                    setValue(entity, rawValue, propertyName);
                } else if (isPropertyForeingEntity(propertyMetaData)) {
                    //Debemos leer la referencia de la base de datos
                    Object rawValue = getValue(jsonObj, propertyName);

                    Set entities = new TreeSet();

                    GenericDAO genericDAO = daoFactory.getDAO(propertyMetaData.getType());

                    String primaryKeyPropertyName=propertyMetaData.getPrimaryKeyPropertyName();
                    Object primaryKey = getValue(rawValue,primaryKeyPropertyName);
                    if (primaryKey!=null) {
                        entities.add(genericDAO.read((Serializable) primaryKey));
                    }
                    
                    for (String naturalKeyPropertyName : propertyMetaData.getNaturalKeyPropertiesName()) {
                        Object naturalKey = getValue(rawValue, naturalKeyPropertyName);
                        if (naturalKey!=null) {
                            entities.add(genericDAO.readByNaturalKey((Serializable) naturalKey));
                        }
                    }

                    if (entities.size()>0) {
                        StringBuilder sb=new StringBuilder();
                        sb.append(propertyMetaData.getPrimaryKeyPropertyName());
                        sb.append(":");
                        sb.append(getValue(rawValue,propertyMetaData.getPrimaryKeyPropertyName()));
                        sb.append(",");


                        for (String naturalKeyPropertyName : propertyMetaData.getNaturalKeyPropertiesName()) {
                            sb.append(naturalKeyPropertyName);
                            sb.append(":");
                            sb.append(getValue(rawValue, naturalKeyPropertyName));
                            sb.append(",");
                        }
                        
                        throw new RuntimeException("El objeto JSON tiene clave primarias y claves naturales que referencian distintos objetos:");
                    }
                } else {
                    //Es una referencia a algo que no es otra entidad ni un valor escalar
                    //Será un componente, así que hacemos la llamada recursiva
                    Object rawValue = getValue(jsonObj, propertyName);
                    populateEntity(entity, rawValue, propertyMetaData);
                }
            } else {
                //En las colecciones no hacemos nada pq no deberían venir desde JSON
            }
        }

    }

    private boolean emptyKey(Object primaryKey) {
        if (primaryKey == null) {
            return true;
        }
        if (primaryKey instanceof Number) {
            Number number = (Number) primaryKey;
            if (number.longValue() == 0) {
                return true;
            }
        }

        if (primaryKey instanceof String) {
            String s = (String) primaryKey;
            if (s.trim().equals("")) {
                return true;
            }
        }


        return false;
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

    /**
     * Establece el valor de la propiedad de un Bean
     *
     * @param obj El objeto Bean
     * @param value El valor de la propiedad
     * @param propertyName El nombre de la propiedad
     */
    private void setValue(Object obj, Object value, String propertyName) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            Method writeMethod = null;
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getName().equals(propertyName)) {
                    writeMethod = propertyDescriptor.getWriteMethod();
                }
            }

            if (writeMethod == null) {
                throw new RuntimeException("No existe la propiedad:" + propertyName);
            }

            writeMethod.invoke(obj, value);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
