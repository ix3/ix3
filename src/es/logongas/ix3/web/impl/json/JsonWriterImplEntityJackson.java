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
package es.logongas.ix3.web.impl.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import es.logongas.ix3.persistence.services.metadata.MetaData;
import es.logongas.ix3.persistence.services.metadata.MetaDataFactory;
import es.logongas.ix3.persistence.services.metadata.MetaType;
import es.logongas.ix3.web.services.json.JsonWriter;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Lorenzo González
 */
public class JsonWriterImplEntityJackson implements JsonWriter {

    @Autowired
    private MetaDataFactory metaDataFactory;
    private ObjectMapper objectMapper;

    public JsonWriterImplEntityJackson() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public String toJson(Object obj) {
        return toJson(obj,null);
    }
    
    @Override
    public String toJson(Object obj,List<String> expand) {
        try {
            if (expand==null) {
                expand=new ArrayList<String>();;
            }
            Object jsonValue = getJsonObjectFromObject(obj,expand,"");
            return objectMapper.writeValueAsString(jsonValue);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }

    }    
    
    private Object getJsonObjectFromObject(Object obj,List<String> expand,String path) {
        if (obj == null) {
            return null;
        } else if (obj instanceof Collection) {
            Collection collection = (Collection) obj;
            List jsonList = new ArrayList();
            for (Object element : collection) {
                jsonList.add(getJsonObjectFromObject(element,expand,path));
            }

            return jsonList;
        } else if (obj instanceof Map) {
            Map map = (Map) obj;
            Map jsonMap = new LinkedHashMap();
            for (Object key : map.keySet()) {
                Object value = map.get(key);

                jsonMap.put(key, getJsonObjectFromObject(value,expand,path));
            }

            return jsonMap;
        } else {
            //Es simplemente un objeto "simple"
            MetaData metaData = metaDataFactory.getMetaData(obj);

            if (metaData != null) {
                Map<String, Object> jsonMap = getMapFromEntity(obj, metaData,expand,path);

                return jsonMap;
            } else {
                Object jsonValue;

                //Como no es un objeto de negocio obtenemos los metadatos mediante reflection
                metaData = new MetaDataImplBean(obj.getClass(), null, true, false, metaDataFactory,null);
                if (metaData.getMetaType() == MetaType.Scalar) {
                    jsonValue = obj;
                } else {
                    jsonValue = getMapFromEntity(obj, metaData,expand,path);
                }

                return jsonValue;
            }
        }
    }

    private Map<String, Object> getMapFromEntity(Object obj, MetaData metaData,List<String> expand,String path) {
        Map<String, Object> values = new LinkedHashMap<String, Object>();

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
                switch (propertyMetaData.getMetaType()) {
                    case Scalar: {
                        value = getValueFromBean(obj, propertyName);
                        break;
                    }
                    case Entity: {
                        Object rawValue = getValueFromBean(obj, propertyName);
                        if (rawValue != null) {
                            if (expandMath(expand,path+"."+propertyName)==true) {
                                //En vez de poner solo la clave primaria , expandimos la entidad
                                value = getMapFromEntity(rawValue, propertyMetaData,expand,path+"."+propertyName);
                            } else {
                                value = getMapFromForeingEntity(rawValue, propertyMetaData);
                            }
                        } else {
                            value = null;
                        }
                        break;
                    }
                    case Component: {
                        Object rawValue = getValueFromBean(obj, propertyName);
                        if (rawValue != null) {
                            value = getMapFromEntity(rawValue, propertyMetaData,expand,path+"."+propertyName);
                        } else {
                            value = null;
                        }

                        break;

                    }
                    default:
                        throw new RuntimeException("El MetaType es desconocido:" + propertyMetaData.getMetaType());
                }
            } else {
                switch (propertyMetaData.getCollectionType()) {
                    case List: {
                        if ((propertyMetaData.isCollectionLazy() == false) || (expandMath(expand,path+"."+propertyName))) {
                            Object rawValue = getValueFromBean(obj, propertyName);
                            List list = (List) rawValue;
                            List jsonList = new ArrayList();
                            for (Object element : list) {
                                jsonList.add(getJsonObjectFromObjectFromCollection(element,propertyMetaData,expand,path+"."+propertyName));
                            }

                            value = jsonList;
                        } else {
                            //Es una colección y Lazy así que añadimos un array vacio
                            value = new ArrayList();
                        }
                        break;
                    }
                    case Set: {
                        if ((propertyMetaData.isCollectionLazy() == false) || (expandMath(expand,path+"."+propertyName))) {
                            Object rawValue = getValueFromBean(obj, propertyName);
                            Set set = (Set) rawValue;
                            Set jsonSet = new HashSet();
                            for (Object element : set) {
                                jsonSet.add(getJsonObjectFromObjectFromCollection(element,propertyMetaData,expand,path+"."+propertyName));
                            }

                            value = jsonSet;
                        } else {
                            //Es una colección y Lazy así que añadimos un array vacio
                            value = new ArrayList();
                        }
                        break;
                    }
                    case Map: {
                        if ((propertyMetaData.isCollectionLazy() == false) || (expandMath(expand,path+"."+propertyName))) {
                            Object rawValue = getValueFromBean(obj, propertyName);

                            Map map = (Map) rawValue;
                            Map jsonMap = new LinkedHashMap();
                            for (Object key : map.keySet()) {
                                Object valueMap = map.get(key);

                                jsonMap.put(key, getJsonObjectFromObjectFromCollection(valueMap,propertyMetaData,expand,path+"."+propertyName));
                            }

                            value = jsonMap;
                        } else {
                            //Es una colección y Lazy así que añadimos un array vacio
                            value = new ArrayList();
                        }
                        break;
                    }
                    default:
                        throw new RuntimeException("El CollectionType es desconocido:" + propertyMetaData.getCollectionType());
                }
            }
            values.put(propertyName, value);

        }

        //Añadimos la representación como String del objeto
        values.put("toString", obj.toString());

        return values;
    }

    private Object getJsonObjectFromObjectFromCollection(Object obj, MetaData metaData,List<String> expand,String path) {
        if (obj == null) {
            return null;
        }

        if (metaData.isCollection() == false) {
            throw new RuntimeException("Debe ser una colección");
        }


        switch (metaData.getMetaType()) {
            case Scalar:
                return obj;
            case Entity:
            case Component:
                Map<String, Object> jsonMap = getMapFromEntity(obj, metaData,expand,path);

                return jsonMap;
            default:
                throw new RuntimeException("El MetaType es desconocido:" + metaData.getMetaType());
        }

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
        Map<String, Object> values = new LinkedHashMap<String, Object>();

        if (obj == null) {
            throw new IllegalArgumentException("El argumento 'obj' no puede ser null");
        }
        if (obj == null) {
            throw new IllegalArgumentException("El argumento 'metaData' no puede ser null");
        }

        //Añadimos la clave primaria
        //No hace falta comprobar si es compuesta pq NO debería ni tener ciclos ni cosas raras.
        values.put(metaData.getPrimaryKeyPropertyName(), getValueFromBean(obj, metaData.getPrimaryKeyPropertyName()));

        //Añadimos las claves natural o tambien llamadas de negocio
        for (String naturalKeyPropertyName : metaData.getNaturalKeyPropertiesName()) {
            //No hace falta comprobar si es compuesta pq NO debería ni tener ciclos ni cosas raras.
            values.put(naturalKeyPropertyName, getValueFromBean(obj, naturalKeyPropertyName));
        }

        //Añadimos la representación como String del objeto
        values.put("toString", obj.toString());

        return values;
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
                    break;
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
    
    private boolean expandMath(List<String> expands,String propertyPath) {
        for(String expandProperty:expands) {
            if (("."+expandProperty).startsWith(propertyPath)) {
                return true;
            }
        }
        
        return false;
        
    }
    
}
