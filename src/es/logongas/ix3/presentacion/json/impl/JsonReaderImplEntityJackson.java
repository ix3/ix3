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
import java.util.Collection;
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

                    Object value = readEntity(rawValue, propertyMetaData);
                    setValue(entity, value, propertyName);
                } else {
                    //Es una referencia a algo que no es otra entidad ni un valor escalar
                    //Será un componente, así que hacemos la llamada recursiva
                    Object rawValue = getValue(jsonObj, propertyName);
                    populateEntity(entity, rawValue, propertyMetaData);
                }
            } else {
                //Es una colección

                if (propertyMetaData.isCollectionLazy() == false) {
                    //Como es una colección "NO" perezona, los datos vendrán con el JSON
                    switch (propertyMetaData.getCollectionType()) {
                        case List:
                        case Set:
                            Collection rawCollection = (Collection) getValue(jsonObj, propertyName);
                            Collection currentCollection = (Collection) getValue(entity, propertyName);

                            //Borramos todos los elementos para añadir despues los que vienen desde JSON
                            currentCollection.clear();

                            //Añadimos los elementos que vienen desde JSON
                            for (Object rawValue : rawCollection) {
                                Object value = readEntity(rawValue, propertyMetaData);
                                currentCollection.add(value);
                            }

                            break;
                        case Map:
                            Map rawMap = (Map) getValue(jsonObj, propertyName);
                            Map currentMap = (Map) getValue(entity, propertyName);

                            //Borramos todos los elementos para añadir despues los que vienen desde JSON
                            currentMap.clear();

                            //Añadimos los elementos que vienen desde JSON
                            for (Object key : rawMap.keySet()) {
                                Object rawValue = rawMap.get(key);
                                Object value = readEntity(rawValue, propertyMetaData);
                                currentMap.put(key, value);
                            }

                            break;
                        default:
                            throw new RuntimeException("El tipo de la colección no es válida:" + propertyMetaData.getCollectionType());
                    }
                } else {
                    //Si es un colección perezosa no la cargamos pq no estará en el texto JSON
                }
            }
        }

    }

    /**
     * Lee una entidad de la base de datos en función de su clave primaria o de alguna de sus claves naturales
     * @param propertyValue
     * @param propertyMetaData
     * @return 
     */
    private Object readEntity(Object propertyValue, MetaData propertyMetaData) {
        try {
            if (propertyValue==null) {
                return null;
            }
            
            //Usamos un Set para guardar todas las entidades
            //Al ser un Set si son la misma se quedará solo una.
            Set entities = new TreeSet();

            GenericDAO genericDAO = daoFactory.getDAO(propertyMetaData.getType());

            //Leer la entidad en función de su clave primaria
            String primaryKeyPropertyName = propertyMetaData.getPrimaryKeyPropertyName();
            Object primaryKey = getValue(propertyValue, primaryKeyPropertyName);
            if (primaryKey != null) {
                Object entity = genericDAO.read((Serializable) primaryKey);
                if (entity != null) {
                    entities.add(entity);
                }
            }

            //Leer la entidad en función de cada una de sus claves primarias
            for (String naturalKeyPropertyName : propertyMetaData.getNaturalKeyPropertiesName()) {
                Object naturalKey = getValue(propertyValue, naturalKeyPropertyName);
                if (naturalKey != null) {
                    Object entity = genericDAO.readByNaturalKey((Serializable) naturalKey);
                    if (entity != null) {
                        entities.add(entity);
                    }
                }
            }

            //Si hay más de un elemento es que hay conflictos entre 
            //la clave primaria y las claves naturales pq identifican entidades distintas
            if (entities.size() > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append(propertyMetaData.getPrimaryKeyPropertyName());
                sb.append(":");
                sb.append(getValue(propertyValue, propertyMetaData.getPrimaryKeyPropertyName()));
                sb.append(",");


                for (String naturalKeyPropertyName : propertyMetaData.getNaturalKeyPropertiesName()) {
                    sb.append(naturalKeyPropertyName);
                    sb.append(":");
                    sb.append(getValue(propertyValue, naturalKeyPropertyName));
                    sb.append(",");
                }

                throw new RuntimeException("El objeto JSON tiene clave primarias y claves naturales que referencian distintos objetos:");
            }

            if (entities.size() == 1) {
                //Retornamos el única elemento que se ha leido de la base de datos
                return entities.iterator().next();
            } else {
                //Si no hay nada retornamos null
                return null;
            }


        } catch (BussinessException be) {
            throw new RuntimeException(be);
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
