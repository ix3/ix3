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
package es.logongas.ix3.web.json.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.module.SimpleModule;
import es.logongas.ix3.core.BusinessException;
import es.logongas.ix3.dao.DAOFactory;
import es.logongas.ix3.dao.GenericDAO;
import es.logongas.ix3.dao.metadata.CollectionType;
import es.logongas.ix3.dao.metadata.MetaData;
import es.logongas.ix3.dao.metadata.MetaDataFactory;
import es.logongas.ix3.web.json.JsonReader;
import es.logongas.ix3.web.json.beanmapper.BeanMapper;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Se usa para una entidad de negocio y usa Jackson
 *
 * @author Lorenzo González
 */
public class JsonReaderImplEntityJackson implements JsonReader {

    private final Class clazz;
    private final ObjectMapper objectMapper;
    @Autowired
    private DAOFactory daoFactory;
    @Autowired
    private MetaDataFactory metaDataFactory;

    public JsonReaderImplEntityJackson(Class clazz) {
        this.clazz = clazz;
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(java.util.Date.class, new DateDeserializer());
        objectMapper.registerModule(module);
    }

    @Override
    public Object fromJson(String json) {
        return fromJson(json, null);
    }


    @Override
    public Object fromJson(String json, BeanMapper beanMapper) {
        try {
            if (beanMapper == null) {
                beanMapper = new BeanMapper(clazz);
            }

            Object jsonObj = objectMapper.readValue(json, clazz);

            MetaData metaData = metaDataFactory.getMetaData(clazz);

            Object entity = readEntity(jsonObj, metaData, "", beanMapper);

            return entity;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }    
    

    /**
     * Crea una entidad completa en base a la clave primaria y a los datos que
     * vienen desde JSON
     *
     * @param jsonObj Los datos JSON
     * @param metaData Los metadatos de la entidad a tranformar
     * @return El Objeto Entidad
     */
    private Object readEntity(Object jsonObj, MetaData metaData, String path, BeanMapper beanMapper) {
        try {

            Object entity;

            GenericDAO genericDAO = daoFactory.getDAO(metaData.getType());

            if (emptyKey(getValueFromBean(jsonObj, metaData.getPrimaryKeyPropertyName())) == false) {
                //Si hay clave primaria es que hay que leerla entidad pq ya existe
                entity = genericDAO.read((Serializable) getValueFromBean(jsonObj, metaData.getPrimaryKeyPropertyName()));
            } else {
                //No hay clave primaria , así que creamos una nueva fila

                entity = genericDAO.create();
            }

            populateEntity(entity, jsonObj, metaData, path, beanMapper);

            return entity;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Lee una entidad de la base de datos en función de su clave primaria o de
     * alguna de sus claves naturales
     *
     * @param propertyValue
     * @param propertyMetaData
     * @return
     */
    private Object readForeingEntity(Object propertyValue, MetaData propertyMetaData) {
        try {
            if (propertyValue == null) {
                return null;
            }

            //Usamos un Set para guardar todas las entidades
            //Al ser un Set si son la misma se quedará solo una.
            Set entities = new HashSet();

            GenericDAO genericDAO = daoFactory.getDAO(propertyMetaData.getType());

            //Leer la entidad en función de su clave primaria
            String primaryKeyPropertyName = propertyMetaData.getPrimaryKeyPropertyName();
            Object primaryKey = getValueFromBean(propertyValue, primaryKeyPropertyName);
            if (primaryKey != null) {
                Object entity = genericDAO.read((Serializable) primaryKey);
                if (entity != null) {
                    entities.add(entity);
                }
            }

            //Leer la entidad en función de cada una de sus claves primarias
            for (String naturalKeyPropertyName : propertyMetaData.getNaturalKeyPropertiesName()) {
                Object naturalKey = getValueFromBean(propertyValue, naturalKeyPropertyName);
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
                sb.append(getValueFromBean(propertyValue, propertyMetaData.getPrimaryKeyPropertyName()));
                sb.append(",");

                for (String naturalKeyPropertyName : propertyMetaData.getNaturalKeyPropertiesName()) {
                    sb.append(naturalKeyPropertyName);
                    sb.append(":");
                    sb.append(getValueFromBean(propertyValue, naturalKeyPropertyName));
                    sb.append(",");
                }

                throw new RuntimeException("El objeto JSON tiene clave primarias y claves naturales que referencian distintos objetos:" + sb);
            }

            if (entities.size() == 1) {
                //Retornamos el única elemento que se ha leido de la base de datos
                return entities.iterator().next();
            } else {
                //Si no hay nada retornamos null
                return null;
            }

        } catch (BusinessException be) {
            throw new RuntimeException(be);
        }
    }

    private void populateEntity(Object entity, Object jsonObj, MetaData metaData, String path, BeanMapper beanMapper) throws BusinessException {

        if (jsonObj == null) {
            return;
        }

        for (String propertyName : metaData.getPropertiesMetaData().keySet()) {
            MetaData propertyMetaData = metaData.getPropertiesMetaData().get(propertyName);

            String fullPropertyName;
            if ((path == null) || (path.trim().length() == 0)) {
                fullPropertyName = propertyName;
            } else {
                fullPropertyName = path + "." + propertyName;
            }

            if (beanMapper.isDeleteInProperty(fullPropertyName) == true) {
                //No se puede generar esa propiedad desde el "exterior"
                continue;
            }

            if (propertyMetaData.isCollection() == false) {
                switch (propertyMetaData.getMetaType()) {
                    case Scalar: {
                        Object rawValue = getValueFromBean(jsonObj, propertyName);
                        setValueToBean(entity, rawValue, propertyName);
                        break;
                    }
                    case Entity: {
                        //Debemos leer la referencia de la base de datos
                        Object rawValue = getValueFromBean(jsonObj, propertyName);

                        Object value = readForeingEntity(rawValue, propertyMetaData);
                        setValueToBean(entity, value, propertyName);
                        break;
                    }
                    case Component: {
                        //Es un componente, así que hacemos la llamada recursiva
                        Object rawValue = getValueFromBean(jsonObj, propertyName);
                        Object component = getValueFromBean(entity, propertyName);
                        if (component == null) {
                            try {
                                component = propertyMetaData.getType().newInstance();
                                setValueToBean(entity, component, propertyName);
                            } catch (InstantiationException ex) {
                                throw new RuntimeException(ex);
                            } catch (IllegalAccessException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                        populateEntity(component, rawValue, propertyMetaData, fullPropertyName, beanMapper);
                        break;
                    }
                    default:
                        throw new RuntimeException("El MetaTypo es desconocido:" + propertyMetaData.getMetaType());
                }
            } else {
                switch (propertyMetaData.getCollectionType()) {
                    case List:
                    case Set: {
                        if (beanMapper.isExpandInProperty(fullPropertyName)) { 
                            Collection rawCollection = (Collection) getValueFromBean(jsonObj, propertyName);
                            Collection currentCollection = (Collection) getValueFromBean(entity, propertyName);

                            //Borramos todos los elementos para añadir despues los que vienen desde JSON
                            if (currentCollection != null) {
                                currentCollection.clear();
                            } else {
                                //Si no hay coleccion hay que crearla aqui
                                if (propertyMetaData.getCollectionType() == CollectionType.List) {
                                    currentCollection = new ArrayList();
                                } else if (propertyMetaData.getCollectionType() == CollectionType.Set) {
                                    currentCollection = new HashSet();
                                } else {
                                    throw new RuntimeException("El tipo coneccion no es válida:" + propertyMetaData.getCollectionType());
                                }
                                setValueToBean(entity, currentCollection, propertyName);
                            }
                            //Añadimos los elementos que vienen desde JSON
                            if (rawCollection != null) {
                                for (Object rawValue : rawCollection) {
                                    Object value = readEntity(rawValue, propertyMetaData, fullPropertyName, beanMapper);
                                    currentCollection.add(value);
                                }
                            }
                        } else {
                            //NO cargamos la coleccion desde el JSON
                        }
                        break;
                    }
                    case Map: {
                        if (beanMapper.isExpandInProperty(fullPropertyName)) { //TODO:No cargamos nunca las coleccione pq aun no sabemos si hay que hacerlo o no
                            Map rawMap = (Map) getValueFromBean(jsonObj, propertyName);
                            Map currentMap = (Map) getValueFromBean(entity, propertyName);

                            //Borramos todos los elementos para añadir despues los que vienen desde JSON
                            if (currentMap != null) {
                                currentMap.clear();
                            } else {
                                //Si no hay coleccion hay que crearla qui
                                currentMap = new HashMap();
                                setValueToBean(entity, currentMap, propertyName);
                            }

                            //Añadimos los elementos que vienen desde JSON
                            if (rawMap != null) {
                                for (Object key : rawMap.keySet()) {
                                    Object rawValue = rawMap.get(key);
                                    Object value = readEntity(rawValue, propertyMetaData, fullPropertyName, beanMapper);
                                    currentMap.put(key, value);
                                }
                            }
                        } else {
                            //NO cargamos la coleccion desde el JSON
                        }
                        break;
                    }
                    default:
                        throw new RuntimeException("El CollectionType es desconocido:" + propertyMetaData.getCollectionType());
                }
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

    /**
     * Obtiene el valor de la propiedad de un Bean
     *
     * @param obj El objeto Bean
     * @param propertyName El nombre de la propiedad
     * @return El valor de la propiedad
     */
    private Object getValueFromBean(Object obj, String propertyName) {
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
                throw new RuntimeException("No existe la propiedad:" + propertyName + " en " + obj.getClass().getName());
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
    private void setValueToBean(Object obj, Object value, String propertyName) {
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
                throw new RuntimeException("No existe la propiedad:" + propertyName + " en " + obj.getClass().getName());
            }

            writeMethod.invoke(obj, value);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
